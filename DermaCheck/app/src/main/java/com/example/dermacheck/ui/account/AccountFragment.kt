package com.example.dermacheck.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.dermacheck.R
import com.example.dermacheck.databinding.FragmentAccountBinding
import com.example.dermacheck.utils.FirebaseManager

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    private lateinit var firebaseManager: FirebaseManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        firebaseManager = FirebaseManager(requireContext())
        binding.loadingSpinner.visibility = View.VISIBLE
        binding.accountScreen.visibility = View.INVISIBLE

        binding.updateProfileBtn.setOnClickListener {
            val action = AccountFragmentDirections.actionNavigationAccountToNavigationUpdateProfile()
            it.findNavController().navigate(action)
        }

        binding.changePasswordBtn.setOnClickListener {
            val action = AccountFragmentDirections.actionNavigationAccountToNavigationResetPassword()
            it.findNavController().navigate(action)
        }

        binding.logoutBtn.setOnClickListener {
            firebaseManager.logout()
            requireActivity().finish()
        }

        firebaseManager.getUserProfileWithImage(
            onSuccess = { name, email, age, gender, imageUrl ->
                binding.nameText.text = name
                binding.emailText.text = email
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(imageUrl).into(binding.profileImage)
                } else {
                    binding.profileImage.setImageResource(R.drawable.baseline_account_circle_24)
                }
                binding.loadingSpinner.visibility = View.GONE
                binding.accountScreen.visibility = View.VISIBLE
            },
            onFailure = {
                binding.emailText.text = it
                binding.loadingSpinner.visibility = View.GONE
                binding.accountScreen.visibility = View.VISIBLE
            }
        )

        firebaseManager.countUserLesions(
            onSuccess = { count ->
                binding.lesionsCount.text = count.toString()
                binding.lesionsCountText.text = if (count == 1) "lesion added" else "lesions added"
            },
            onFailure = {
                binding.lesionsCountText.text = "Error loading lesion count"
            }
        )


        return binding.root
    }
}
