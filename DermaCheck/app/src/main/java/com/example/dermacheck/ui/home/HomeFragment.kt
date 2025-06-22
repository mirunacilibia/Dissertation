package com.example.dermacheck.ui.home

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dermacheck.R
import com.example.dermacheck.data.models.Lesion
import com.example.dermacheck.databinding.FragmentHomeBinding
import com.example.dermacheck.utils.FirebaseManager
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        firebaseManager = FirebaseManager(requireContext())

        binding.loadingSpinner.visibility = View.VISIBLE
        binding.profileContainer.visibility = View.INVISIBLE
        binding.notificationsScroll.visibility = View.INVISIBLE

        firebaseManager.getUserProfileWithImage(
                onSuccess = { name, _, _, _, imageUrl ->
                    binding.welcomeText.text = "Hi, $name"
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(binding.profileImage)
                    } else {
                        binding.profileImage.setImageResource(R.drawable.baseline_account_circle_24)
                    }
                    showMainUI()
                },
                onFailure = {
                    binding.welcomeText.text = "Failed to load user data"
                    showMainUI()
                }
            )

        firebaseManager.getUserLesions(
            onSuccess = { lesions ->
                val safeLesions = lesions.count { it.status == "safe" }
                val dangerousLesions = lesions.count { it.status == "danger" }
                val warningLesions = lesions.count { it.status == "check" }

                addNotification("Take control of your skin health — analyze lesions instantly and track changes over time, all in one place.", R.color.neutral)
                addNotification("Unsure about a mole? Our AI gives you quick, personalized insights to help you decide what needs a closer look.", R.color.neutral)
                addNotification("Your skin story matters — save, review, and learn more about your conditions while building a history that helps you and your doctor.", R.color.neutral)
                addNotification("We can classify 11 different skin conditions from your images, and you can explore detailed information about each one.", R.color.neutral)

                addNotification("$safeLesions of your saved lesions are marked safe.", R.color.green)

                if (dangerousLesions > 0) {
                    addNotification(
                        "You have $dangerousLesions lesion(s) we marked as dangerous. We recommend you speak to a certified professional about these lesions",
                        R.color.error
                    )
                } else if (warningLesions > 0) {
                    addNotification(
                        "You have $warningLesions lesion(s) we recommend checking. These lesions show signs of concern.",
                        R.color.warning
                    )
                } else {
                    addNotification("All your lesions are safe. That’s great!", R.color.green)
                }

                for (lesion in lesions) {
                    val daysAgo = lesion.lastUpdated?.toDate()?.let { date ->
                        val diff = System.currentTimeMillis() - date.time
                        (diff / (1000 * 60 * 60 * 24)).toInt()
                    } ?: 0

                    if (daysAgo >= 60) {
                        addNotification(
                            "It’s been $daysAgo days since you last updated one of your lesions diagnosed as: ${lesion.currentDiagnosis} – we recommend you check it again. Constant checks help catch dangerous developments early",
                            R.color.warning
                        )
                    }
                }
            },
            onFailure = {
                addNotification("Failed to load lesion data.", R.color.error)
            }
        )

        return binding.root
    }

    private fun showMainUI() {
        binding.loadingSpinner.visibility = View.GONE
        binding.profileContainer.visibility = View.VISIBLE
        binding.notificationsScroll.visibility = View.VISIBLE
    }

    private fun addNotification(message: String, colorRes: Int) {
        val pill = LayoutInflater.from(requireContext()).inflate(R.layout.item_pill_notification, binding.notificationsContainer, false)
        val card = pill.findViewById<androidx.cardview.widget.CardView>(R.id.pillCard)
        val text = pill.findViewById<TextView>(R.id.pillText)

        card.setCardBackgroundColor(requireContext().getColor(colorRes))
        text.text = message

        binding.notificationsContainer.addView(pill)
    }
}
