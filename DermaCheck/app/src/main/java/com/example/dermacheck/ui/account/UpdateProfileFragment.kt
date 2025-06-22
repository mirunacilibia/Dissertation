package com.example.dermacheck.ui.account

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermacheck.R
import com.example.dermacheck.databinding.FragmentUpdateProfileBinding
import com.example.dermacheck.utils.FirebaseManager

class UpdateProfileFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 1

    private var selectedImageUri: Uri? = null
    private var selectedGender: String = "Man"
    private lateinit var binding: FragmentUpdateProfileBinding
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var loadingSpinner: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpdateProfileBinding.inflate(inflater, container, false)

        firebaseManager = FirebaseManager(requireContext())
        loadingSpinner = binding.loadingSpinner

        val genderOptions = arrayOf("Man", "Woman")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.genderSpinner.adapter = adapter
        binding.genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedGender = genderOptions[position]
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        }

        firebaseManager.getUserProfileWithImage(
            onSuccess = { name, email, age, gender, imageUrl ->
                binding.nameInput.setText(name)
                binding.ageInput.setText(age.toString())
                val genderPosition = if (gender == "Man") 0 else 1
                binding.genderSpinner.setSelection(genderPosition)
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .into(binding.profileImage)
                } else {
                    binding.profileImage.setImageResource(R.drawable.baseline_account_circle_24)
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        )

        // Handle profile image selection from gallery
        binding.addImageButton.setOnClickListener {
            openGallery()
        }


        binding.saveButton.setOnClickListener {
            val updatedName = binding.nameInput.text.toString()
            val updatedAge = binding.ageInput.text.toString()
            val updatedGender = binding.genderSpinner.selectedItem.toString()
            if (updatedAge.isEmpty() || updatedAge.toIntOrNull() == null) {
                binding.ageInput.error = "Please enter a valid age"
                return@setOnClickListener
            }

            val updatedFields = hashMapOf<String, Any>(
                "name" to updatedName,
                "age" to updatedAge.toInt(),
                "gender" to updatedGender
            )

            showLoading(true)
            if (selectedImageUri != null) {
                firebaseManager.uploadProfileImage(selectedImageUri!!) { imageUrl ->
                    if (imageUrl != null) {
                        updatedFields["profileImage"] = imageUrl
                    }
                    updateUser(updatedFields)
                    showLoading(false)
                }
            } else {
                updateUser(updatedFields)
                showLoading(false)
            }
        }
        return binding.root
    }

    private fun updateUser(fields: Map<String, Any>) {
        firebaseManager.updateUserProfile(fields) { success ->
            val message = if (success) "Profile updated successfully" else "Failed to update profile"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            if (success) findNavController().navigate(R.id.navigation_account)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.profileImage.setImageURI(selectedImageUri)
        }
    }

    fun showLoading(show: Boolean) {
        loadingSpinner.visibility = if (show) View.VISIBLE else View.GONE
    }
}
