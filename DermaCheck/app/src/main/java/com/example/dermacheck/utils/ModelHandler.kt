package com.example.dermacheck.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import com.example.dermacheck.R
import java.util.*

class ModelHandler(
    private val context: Context,
    private val modelFilenames: List<String>,
    private val inputSizes: List<Int>
) {

    private val interpreters: List<Interpreter> = modelFilenames.map { loadTFLiteModel(it) }

    private fun loadTFLiteModel(filename: String): Interpreter {
        val assetFileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        return Interpreter(modelBuffer)
    }

    fun runInference(
        bitmap: Bitmap,
        userAge: Int,
        userGender: String,
        location: String,
        classLabels: List<String>
    ): String {
        val outputs = mutableListOf<FloatArray>()

        for ((i, interpreter) in interpreters.withIndex()) {
            val inputImage = preprocessImage(bitmap, inputSizes[i])
            val output = Array(1) { FloatArray(12) }

            if (i <= 3) {
                interpreter.run(inputImage, output)
            } else {
                val metadataInput = preprocessMetadata(userAge, userGender, location)
                val inputs = arrayOf(inputImage, metadataInput)
                val outputsMap = hashMapOf<Int, Any>(0 to output)
                interpreter.runForMultipleInputsOutputs(inputs, outputsMap)
            }

            Log.d("Model $i Output", output[0].joinToString(prefix = "[", postfix = "]") { String.format("%.2f", it) })
            outputs.add(output[0])
    }

        val averaged = FloatArray(12) { i -> outputs.map { it[i] }.average().toFloat() }
        val predictedIndex = averaged.indices.maxByOrNull { averaged[it] } ?: -1
        Log.d("Ensemble result", averaged.joinToString(prefix = "[", postfix = "]") { String.format("%.2f", it) })
        Log.d("Ensemble result", "predictedIndex: $predictedIndex - ${classLabels[predictedIndex]}")
        return classLabels[predictedIndex]
    }

    private fun preprocessImage(bitmap: Bitmap, size: Int): Array<Array<Array<FloatArray>>> {
        val resized = bitmap.scale(size, size)
        val input = Array(1) { Array(size) { Array(size) { FloatArray(3) } } }
        for (y in 0 until size) {
            for (x in 0 until size) {
                val pixel = resized.getPixel(x, y)
                input[0][y][x][0] = ((pixel shr 16 and 0xFF) / 255.0f)
                input[0][y][x][1] = ((pixel shr 8 and 0xFF) / 255.0f)
                input[0][y][x][2] = ((pixel and 0xFF) / 255.0f)
            }
        }
        return input
    }

    private fun preprocessMetadata(age: Int, gender: String, location: String): Array<FloatArray> {
        val metaVector = FloatArray(9)
        metaVector[0] = age.toFloat()
        val locations = listOf("anterior torso", "lower extremity", "upper extremity", "head/neck", "palms/soles", "oral/genital")
        val locIndex = locations.indexOf(location.lowercase(Locale.getDefault()))
        if (locIndex != -1) metaVector[1 + locIndex] = 1f
        if (gender.lowercase(Locale.getDefault()) == "man") metaVector[7] = 1f
        else if (gender.lowercase(Locale.getDefault()) == "woman") metaVector[8] = 1f
        return arrayOf(metaVector)
    }

    fun close() {
        interpreters.forEach { it.close() }
    }
}
