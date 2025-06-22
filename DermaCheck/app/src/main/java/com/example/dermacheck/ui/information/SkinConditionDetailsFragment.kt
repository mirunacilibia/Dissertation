package com.example.dermacheck.ui.information

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dermacheck.R
import com.example.dermacheck.data.models.SkinCondition
import com.example.dermacheck.databinding.FragmentSkinDiseaseDetailsBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson

class SkinConditionDetailsFragment : Fragment() {

    private lateinit var condition: SkinCondition
    private lateinit var binding: FragmentSkinDiseaseDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSkinDiseaseDetailsBinding.inflate(inflater, container, false)

        val json = arguments?.getString("itemJson") ?: ""
        condition = Gson().fromJson(json, SkinCondition::class.java)

        binding.conditionName.title = condition.name
        binding.conditionType.text = condition.type
        binding.conditionLocations.text = condition.locations.joinToString(", ")
        binding.conditionDescription.text = condition.description
        binding.conditionAppearance.text = condition.appearance
        binding.conditionTreatment.text = condition.treatment
        binding.loadingSpinner.visibility = View.VISIBLE

        addImagesToLayout(condition.images)

        return binding.root
    }


    private fun addImagesToLayout(imageUrls: List<String>) {
        val imageContainer = binding.imageContainer
        imageContainer.removeAllViews()

        // Show spinner and hide content
        binding.loadingSpinner.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE

        if (imageUrls.isEmpty()) {
            // If there are no images, hide spinner immediately
            binding.loadingSpinner.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
            return
        }

        var imagesLoaded = 0

        for (url in imageUrls) {
            val imageView = ImageView(context)
            val imageSize = resources.getDimensionPixelSize(R.dimen.image_size)

            val params = ViewGroup.LayoutParams(imageSize, imageSize)
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setBackgroundResource(R.drawable.bordered_image)
            imageView.clipToOutline = true

            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)

            gsReference.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                Glide.with(this)
                    .load(bitmap)
                    .centerCrop()
                    .into(imageView)

                imageContainer.addView(imageView)

                val space = Space(context)
                val spaceParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    resources.getDimensionPixelSize(R.dimen.margin_24)
                )
                space.layoutParams = spaceParams
                imageContainer.addView(space)

                imagesLoaded++
                if (imagesLoaded == imageUrls.size) {
                    // All images loaded
                    binding.loadingSpinner.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()

                imagesLoaded++
                if (imagesLoaded == imageUrls.size) {
                    binding.loadingSpinner.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
                }
            }
        }
    }


}
