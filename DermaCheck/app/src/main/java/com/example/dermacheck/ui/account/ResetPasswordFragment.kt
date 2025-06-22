package com.example.dermacheck.ui.account

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.dermacheck.R
import com.example.dermacheck.utils.FirebaseManager

class ResetPasswordFragment : Fragment() {

    private lateinit var emailInput: EditText
    private lateinit var sendResetLinkButton: Button
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reset_password, container, false)

        firebaseManager = FirebaseManager(requireContext())
        emailInput = view.findViewById(R.id.resetEmailInput)
        sendResetLinkButton = view.findViewById(R.id.sendResetLinkButton)

        sendResetLinkButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            firebaseManager.sendPasswordResetEmail(
                email,
                onSuccess = {
                    Toast.makeText(requireContext(), "Reset email sent to $email", Toast.LENGTH_LONG).show()
                },
                onFailure = { message ->
                    Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                }
            )
        }

        return view
    }
}
