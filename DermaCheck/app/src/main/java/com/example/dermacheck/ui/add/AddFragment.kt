package com.example.dermacheck.ui.add

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dermacheck.R

class AddFragment : Fragment() {

    private lateinit var bodyMapView: BodyMapView
    private lateinit var selectedPartTextView: TextView
    private lateinit var instructionsTextView: TextView
    private lateinit var takePhotoButton: Button

    private var selectedRegionId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        bodyMapView = view.findViewById(R.id.bodyMapView)
        selectedPartTextView = view.findViewById(R.id.selectedPartTextView)
        instructionsTextView = view.findViewById(R.id.instructionText)
        takePhotoButton = view.findViewById(R.id.takePhotoButton)

        takePhotoButton.isEnabled = false
        takePhotoButton.alpha = 0.5f // optional: visually faded

        bodyMapView.onRegionSelectedListener = { region ->
            instructionsTextView.visibility = View.VISIBLE

            takePhotoButton.isEnabled = true
            takePhotoButton.alpha = 1.0f // restore visibility

            selectedRegionId = regionToBodyLocationMap[region]
        }

        takePhotoButton.setOnClickListener {
            selectedRegionId?.let {
                val intent = Intent(requireContext(), TakeImageActivity::class.java)
                intent.putExtra("regionId", it)
                startActivity(intent)
            }
        }

        return view
    }

    private val regionToBodyLocationMap = mapOf(
        "left_hand" to "palms-soles",
        "right_hand" to "palms-soles",
        "left_foot" to "palms-soles",
        "right_foot" to "palms-soles",
        "anterior_torso" to "anterior-torso",
        "genital" to "oral_genital",
        "oral" to "oral_genital",
        "left_arm" to "upper-extremity",
        "right_arm" to "upper-extremity",
        "left_leg" to "lower-extremity",
        "right_leg" to "lower-extremity",
        "head" to "head/neck"
    )


}
