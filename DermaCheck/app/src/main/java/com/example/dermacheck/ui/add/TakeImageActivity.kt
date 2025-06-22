package com.example.dermacheck.ui.add

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.dermacheck.Dashboard
import com.example.dermacheck.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.channels.FileChannel
import androidx.core.graphics.scale
import androidx.core.graphics.get
import java.util.Locale
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import androidx.core.graphics.createBitmap
import com.example.dermacheck.utils.FirebaseManager
import com.example.dermacheck.utils.ModelHandler
import com.google.firebase.storage.FirebaseStorage

class TakeImageActivity : AppCompatActivity() {

    private lateinit var captureIV: ImageView
    private lateinit var ignoreOutlineCheckbox: CheckBox
    private lateinit var imageUrl: Uri
    private var hasLaunchedCamera = false
    private var croppedImageUri: Uri? = null

    private var modelBitmap: Bitmap? = null

    private val modelFilenames = listOf(
        "model-224.tflite", "model-240.tflite", "model-260.tflite", "model-300.tflite",
        "fusion_model_224.tflite", "fusion_model_240.tflite", "fusion_model_260.tflite", "fusion_model_300.tflite"
    )
    private val inputSizes = listOf(224, 240, 260, 300, 224, 240, 260, 300)

    private val classLabels = listOf(
        "Nevus", "Melanoma", "Other", "Squamous Cell Carcinoma", "Solar Lentigo", "Basal Cell Carcinoma",
        "Melanoma Metastasis", "Seborrheic Keratosis", "Actinic Keratosis", "Dermatofibroma", "Scar", "Vascular Lesion"
    )

    private lateinit var firebaseManager: FirebaseManager
    private var userAge: Int = -5
    private var userGender: String = ""
    private var selectedBodyRegionId: String? = null
    private var existingLesionId: String? = null
    private var userEmail: String = ""

    private val cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let {
                val inputStream = contentResolver.openInputStream(it)
                val initialCroppedImage = BitmapFactory.decodeStream(inputStream)
                croppedImageUri = it

                if (ignoreOutlineCheckbox.isChecked) {
                    modelBitmap = initialCroppedImage
                    captureIV.setImageBitmap(initialCroppedImage)
                } else {
                    modelBitmap = isolateAndDrawBoundingBox(initialCroppedImage)
                }
            }
        }
    }

    private fun isolateAndDrawBoundingBox(initialBitmap: Bitmap): Bitmap {
        val originalMat = Mat()
        Utils.bitmapToMat(initialBitmap, originalMat)
        val gray = Mat()
        Imgproc.cvtColor(originalMat, gray, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)
        val binary = Mat()
        Imgproc.threshold(gray, binary, 0.0, 255.0, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        if (contours.isEmpty()) {
            Toast.makeText(this, "No contours found", Toast.LENGTH_SHORT).show()
            return initialBitmap
        }

        val largestContour = contours.maxByOrNull { Imgproc.contourArea(it) }!!
        val rect = Imgproc.boundingRect(largestContour)

        val extraPadding = 10
        val size = maxOf(rect.width, rect.height)
        val centerX = rect.x + rect.width / 2
        val centerY = rect.y + rect.height / 2
        val squareLeft = (centerX - size / 2 - extraPadding).coerceAtLeast(0)
        val squareTop = (centerY - size / 2 - extraPadding).coerceAtLeast(0)
        val squareRight = (squareLeft + size + 2 * extraPadding).coerceAtMost(originalMat.width())
        val squareBottom = (squareTop + size + 2 * extraPadding).coerceAtMost(originalMat.height())

        val squareRect = Rect(squareLeft, squareTop, squareRight - squareLeft, squareBottom - squareTop)

        Imgproc.rectangle(originalMat, squareRect, Scalar(255.0, 0.0, 0.0), 5)
        val annotatedBitmap = createBitmap(originalMat.cols(), originalMat.rows())
        Utils.matToBitmap(originalMat, annotatedBitmap)

        captureIV.setImageBitmap(annotatedBitmap)
        val croppedMat = Mat(originalMat, squareRect)
        val croppedBitmap = createBitmap(croppedMat.cols(), croppedMat.rows())
        Utils.matToBitmap(croppedMat, croppedBitmap)
        return croppedBitmap
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) startCrop(imageUrl)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_image)

        if (!org.opencv.android.OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Initialization failed")
        } else {
            Log.d("OpenCV", "Initialization successful")
        }

        firebaseManager = FirebaseManager(this)
        userEmail = firebaseManager.getUserEmail().toString()

        captureIV = findViewById(R.id.imagePreview)
        selectedBodyRegionId = intent.getStringExtra("regionId")
        existingLesionId = intent.getStringExtra("existingLesionId")

        firebaseManager.getUserProfileWithImage(
            onSuccess = { _, _, age, gender, _ ->
                userAge = age
                userGender = gender
            },
            onFailure = {}
        )

        ignoreOutlineCheckbox = findViewById<CheckBox>(R.id.ignoreOutlineCheckbox)
        ignoreOutlineCheckbox.setOnCheckedChangeListener { _, isChecked ->
            croppedImageUri?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                val initialCroppedImage = BitmapFactory.decodeStream(inputStream)

                if (isChecked) {
                    modelBitmap = initialCroppedImage
                    captureIV.setImageBitmap(initialCroppedImage)
                } else {
                    modelBitmap = isolateAndDrawBoundingBox(initialCroppedImage)
                }
            } ?: run {
                Toast.makeText(this, "Image is not available", Toast.LENGTH_SHORT).show()
            }
        }



        val retakeButton = findViewById<Button>(R.id.btnRetake)
        if (!hasLaunchedCamera) {
            hasLaunchedCamera = true
            imageUrl = createImageUri()
            cameraLauncher.launch(imageUrl)
        }

        retakeButton.setOnClickListener {
            hasLaunchedCamera = true
            imageUrl = createImageUri()
            cameraLauncher.launch(imageUrl)
        }

        val detectBtn = findViewById<Button>(R.id.btnDetect)
        detectBtn.setOnClickListener {
            modelBitmap?.let { bmp ->
                val progressDialog = AlertDialog.Builder(this)
                    .setView(layoutInflater.inflate(R.layout.dialog_loading, null))
                    .setCancelable(false)
                    .create()

                progressDialog.show()

                Thread {
                    val modelHandler = ModelHandler(
                        context = this,
                        modelFilenames = modelFilenames,
                        inputSizes = inputSizes
                    )
                    val prediction = modelHandler.runInference(bmp, userAge, userGender, selectedBodyRegionId ?: "unknown", classLabels)
                    modelHandler.close()


                    runOnUiThread {
                        progressDialog.dismiss()
                        showPredictionDialog(prediction)
                    }
                }.start()
            } ?: Toast.makeText(this, "Image not ready for detection", Toast.LENGTH_SHORT).show()
        }

    }

    private fun startCrop(sourceUri: Uri) {
        val safeEmail = userEmail.replace("[^A-Za-z0-9]".toRegex(), "_")
        val filename = "cropped_${safeEmail}_${System.currentTimeMillis()}.jpg"
        val croppedFile = File(cacheDir, filename)
        val destinationUri = Uri.fromFile(croppedFile)

        val options = UCrop.Options().apply {
            setCompressionQuality(90)
            setFreeStyleCropEnabled(false)
            setHideBottomControls(true)
            setToolbarTitle("Adjust to Mole Area")
        }

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1080, 1080)
            .withOptions(options)

        cropLauncher.launch(uCrop.getIntent(this))
    }

    private fun createImageUri(): Uri {
        val filename = "camera_${System.currentTimeMillis()}.jpg"
        val image = File(filesDir, filename)
        return FileProvider.getUriForFile(this, "${applicationContext.packageName}.ui.add.FileProvider", image)
    }

    private fun showPredictionDialog(predictedClass: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_prediction, null)
        val predictionText = dialogView.findViewById<TextView>(R.id.predictionText)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnHome = dialogView.findViewById<Button>(R.id.btnHome)

        predictionText.text = "The predicted skin condition for this is: $predictedClass"
        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create()

        btnSave.setOnClickListener {
            if (selectedBodyRegionId == null) {
                Toast.makeText(this, "Please select a body region first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            croppedImageUri?.let { uri ->
                val loadingView = layoutInflater.inflate(R.layout.dialog_loading, null)
                val loadingTextView = loadingView.findViewById<TextView>(R.id.loadingText)
                loadingTextView.text = "Saving lesion..."

                val uploadDialog = AlertDialog.Builder(this)
                    .setView(loadingView)
                    .setCancelable(false)
                    .create()

                uploadDialog.show()

                firebaseManager.saveLesion(
                    imageUri = uri,
                    predictedClass = predictedClass,
                    selectedBodyRegionId = selectedBodyRegionId!!,
                    existingLesionId = existingLesionId,
                    onSuccess = {
                        Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Dashboard::class.java)); finish()
                    },
                    onFailure = {
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                    }
                )
            } ?: Toast.makeText(this, "Image not available.", Toast.LENGTH_SHORT).show()
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }

        dialog.show()
    }
}
