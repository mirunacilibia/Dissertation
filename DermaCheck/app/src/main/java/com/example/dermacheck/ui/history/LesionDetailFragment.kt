package com.example.dermacheck.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermacheck.R
import com.example.dermacheck.data.models.Lesion
import com.google.gson.Gson
import com.example.dermacheck.databinding.FragmentLesionDetailBinding
import com.example.dermacheck.ui.add.TakeImageActivity
import com.example.dermacheck.utils.FirebaseManager

class LesionDetailFragment : Fragment() {

    private lateinit var binding: FragmentLesionDetailBinding
    private lateinit var lesion: Lesion
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLesionDetailBinding.inflate(inflater, container, false)

        firebaseManager = FirebaseManager(requireContext())

        val json = arguments?.getString("lesionJson") ?: ""
        lesion = Gson().fromJson(json, Lesion::class.java)

        binding.diagnosisText.text = lesion.currentDiagnosis
        binding.locationText.text = lesion.bodyLocationValue
        binding.statusPill.text = lesion.status.toString().uppercase()

        // Update pill status color based on diagnosis
        updateStatusPill(lesion.status)

        populateHistory()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deleteBtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Lesion")
                .setMessage("Are you sure you want to delete this lesion?")
                .setPositiveButton("Confirm") { _, _ -> deleteLesion() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.addBtn.setOnClickListener {
            val intent = Intent(requireContext(), TakeImageActivity::class.java)
            intent.putExtra("existingLesionId", lesion.id)
            intent.putExtra("regionId", lesion.bodyLocationId)
            startActivity(intent)
        }

        binding.learnMoreBtn.setOnClickListener {
            findNavController().navigate(R.id.navigation_information)
        }
    }

    private fun deleteLesion() {
        firebaseManager.deleteLesion(
            userEmail = lesion.userEmail,
            createdAt = lesion.createdAt,
            onSuccess = {
                Toast.makeText(requireContext(), "Lesion deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            },
            onFailure = {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        )

    }

    private fun updateStatusPill(status: String) {
        val statusPill = binding.statusPill
        when (status) {
            "safe" -> {
                statusPill.text = "SAFE"
                statusPill.setBackgroundResource(R.drawable.status_pill_safe) // Change the pill background color
            }
            "danger" -> {
                statusPill.text = "DANGER"
                statusPill.setBackgroundResource(R.drawable.status_pill_danger) // Change the pill background color
            }
            else -> {
                statusPill.text = "CHECK"
                statusPill.setBackgroundResource(R.drawable.status_pill_check) // Change the pill background color
            }
        }
    }

    private fun populateHistory() {
        val historyContainer = binding.historyContainer
        historyContainer.removeAllViews() // Clear any previous history entries

        for (historyItem in lesion.history) {
            val historyView = layoutInflater.inflate(R.layout.item_history_image, historyContainer, false)

            val diagnosisText = historyView.findViewById<TextView>(R.id.historyDiagnosis)
            val dateText = historyView.findViewById<TextView>(R.id.historyDate)
            val imageView = historyView.findViewById<ImageView>(R.id.historyImage)

            diagnosisText.text = historyItem.diagnosis
            dateText.text = historyItem.date?.toDate()?.toString()?.substring(0, 16) // Format the date

            // Load image into ImageView using Glide
            Glide.with(imageView.context)
                .load(historyItem.imageUrl)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .into(imageView)

            // Add the history item view to the container
            historyContainer.addView(historyView)
        }
    }
}
