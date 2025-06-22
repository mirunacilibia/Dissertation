package com.example.dermacheck.ui.history


import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermacheck.R

import com.example.dermacheck.databinding.FragmentHistoryBinding
import com.example.dermacheck.data.models.Lesion
import com.example.dermacheck.adapter.LesionAdapter
import com.example.dermacheck.utils.FirebaseManager
import com.google.android.material.bottomsheet.BottomSheetDialog


class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var adapter: LesionAdapter
    private var allLesions: List<Lesion> = emptyList()
    private var isDataReady = false

    private var selectedSort = "Newest first"
    private var selectedLocation = "All"
    private var selectedDiagnosis = "All"

    private val bodyLocationMap = mutableMapOf<String, String>()
    private val bodyLocationReverseMap = mutableMapOf<String, String>()

    private lateinit var firebaseManager: FirebaseManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        adapter = LesionAdapter(requireContext())

        firebaseManager = FirebaseManager(requireContext())

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.loadingSpinner.visibility = View.VISIBLE
        setupFilters()
        fetchLesions()

        return binding.root
    }

    private fun showBottomSheet(title: String, options: List<String>, onSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.modal_bottom_sheet, null)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group)
        val titleView = view.findViewById<TextView>(R.id.bottom_sheet_title)
        val closeButton = view.findViewById<ImageButton>(R.id.close_button)

        titleView.text = title

        for (option in options) {
            val radioButton = RadioButton(ContextThemeWrapper(requireContext(), R.style.CustomRadioButton), null).apply {
                text = option
                textSize = 16f
                setPadding(8, 16, 8, 16)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary)) // or R.color.black
            }

            radioGroup.addView(radioButton)
            if (
                (title == "Sort" && option == selectedSort) ||
                (title == "Location" && option == selectedLocation) ||
                (title == "Diagnosis" && option == selectedDiagnosis)
            ) {
                radioButton.isChecked = true
            }
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selected = view.findViewById<RadioButton>(checkedId).text.toString()
            onSelected(selected)
            dialog.dismiss()
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }



    private fun setupFilters() {
        binding.sortButton.setOnClickListener {
            showBottomSheet("Sort", listOf("Newest first", "Oldest first")) { selection ->
                selectedSort = selection
                filterAndSortLesions()
            }
        }

        binding.locationButton.setOnClickListener {
            val locationLabels = listOf("All") + bodyLocationMap.values
            showBottomSheet("Location", locationLabels) { selectionLabel ->
                selectedLocation = if (selectionLabel == "All") {
                    "All"
                } else {
                    bodyLocationReverseMap[selectionLabel] ?: "All"
                }
                filterAndSortLesions()
            }
        }


        binding.diagnosisButton.setOnClickListener {
            firebaseManager.getAllSkinConditions(
                onSuccess = { conditions ->
                    val diagnoses = listOf("All") + conditions.map { it.name }
                    showBottomSheet("Diagnosis", diagnoses) { selection ->
                        selectedDiagnosis = selection
                        filterAndSortLesions()
                    }
                },
                onFailure = {
                    Toast.makeText(requireContext(), "Failed to load skin conditions", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun fetchLesions() {
        firebaseManager.getBodyLocations(
            onSuccess = { map, reverseMap ->
                bodyLocationMap.putAll(map)
                bodyLocationReverseMap.putAll(reverseMap)

                firebaseManager.getUserLesions(
                    onSuccess = { lesions ->
                        allLesions = lesions.map { lesion ->
                            lesion.copy(bodyLocationValue = bodyLocationMap[lesion.bodyLocationId] ?: lesion.bodyLocationId)
                        }
                        isDataReady = true
                        binding.loadingSpinner.visibility = View.GONE
                        filterAndSortLesions()
                    },
                    onFailure = {
                        binding.loadingSpinner.visibility = View.GONE
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onFailure = {
                binding.loadingSpinner.visibility = View.GONE
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun filterAndSortLesions() {
        if (!isDataReady) return

        var filtered = allLesions

        if (selectedLocation != "All") {
            filtered = filtered.filter { it.bodyLocationId == selectedLocation }
        }
        if (selectedDiagnosis != "All") {
            filtered = filtered.filter { it.currentDiagnosis == selectedDiagnosis }
        }
        filtered = if (selectedSort == "Newest first") {
            filtered.sortedByDescending { it.createdAt }
        } else {
            filtered.sortedBy { it.createdAt }
        }

        adapter.submitList(filtered)
    }

}
