package com.example.dermacheck.ui.information

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.dermacheck.R
import com.example.dermacheck.data.models.SkinCondition
import com.example.dermacheck.databinding.FragmentInformationBinding
import com.example.dermacheck.utils.FirebaseManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class InformationFragment : Fragment() {

    private lateinit var binding: FragmentInformationBinding
    private var allConditions = listOf<SkinCondition>()

    private var selectedType = "All"
    private var selectedCondition = "All"
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInformationBinding.inflate(inflater, container, false)

        firebaseManager = FirebaseManager(requireContext())

        setupFilterButtons()
        fetchSkinConditions()

        return binding.root
    }

    private fun setupFilterButtons() {
        binding.typeFilterButton.setOnClickListener {
            val types = listOf("All", "Benign", "Malignant", "Premalignant")
            showBottomSheet("Type", types) { selection ->
                selectedType = selection
                filterConditions()
            }
        }

        binding.conditionButton.setOnClickListener {
            val conditionNames = listOf("All") + allConditions.map { it.name }
            showBottomSheet("Condition", conditionNames) { selection ->
                selectedCondition = selection
                displayConditionDetails(selection)
            }
        }
    }

    private fun fetchSkinConditions() {
        firebaseManager.getAllSkinConditions(
            onSuccess = { conditions ->
                allConditions = conditions
                filterConditions()
            },
            onFailure = {
                Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun filterConditions() {
        val typeLower = selectedType.lowercase()
        val filtered = if (typeLower == "all") {
            allConditions
        } else {
            allConditions.filter { it.type.lowercase() == typeLower }
        }
        populateConditionViews(filtered)
    }

    private fun displayConditionDetails(conditionName: String) {
        if (conditionName == "All") {
            filterConditions()
            return
        }
        val condition = allConditions.find { it.name == conditionName }
        condition?.let {
            populateConditionViews(listOf(it))
        }
    }

    private fun populateConditionViews(conditions: List<SkinCondition>) {
        val container = binding.conditionsContainer
        container.removeAllViews()

        for (condition in conditions) {
            val itemView = layoutInflater.inflate(R.layout.item_skin_condition, container, false)

            val nameText = itemView.findViewById<TextView>(R.id.condition_name)
            val typeText = itemView.findViewById<TextView>(R.id.condition_type)
            val descriptionText = itemView.findViewById<TextView>(R.id.condition_description)

            // Set name and type
            nameText.text = condition.name
            typeText.text = condition.type.replaceFirstChar { it.uppercaseChar() }

            // Truncate description if long
            descriptionText.text = if (condition.description.length > 150) {
                condition.description.take(150) + "..."
            } else {
                condition.description
            }

            // Navigate on click
            itemView.setOnClickListener {
                val json = Gson().toJson(condition)
                val action = InformationFragmentDirections
                    .actionNavigationInformationToNavigationSkinCondition(json)
                it.findNavController().navigate(action)
            }

            container.addView(itemView)
        }
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
                setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))
            }
            radioGroup.addView(radioButton)
            if ((title == "Type" && option == selectedType) ||
                (title == "Condition" && option == selectedCondition)) {
                radioButton.isChecked = true
            }
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selected = view.findViewById<RadioButton>(checkedId).text.toString()
            onSelected(selected)
            dialog.dismiss()
        }

        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }
}
