package com.example.dermacheck

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dermacheck.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Setup gender dropdown for MaterialAutoCompleteTextView
        val genderOptions = listOf("Man", "Woman")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        binding.genderSpinner.setAdapter(adapter)


        // Navigate to Sign In
        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        // Handle Sign Up
        binding.button.setOnClickListener {
            val name = binding.name.text.toString()
            val email = binding.email.text.toString()
            val pass = binding.pass.text.toString()
            val confirmPass = binding.confirmPass.text.toString()
            val age = binding.age.text.toString()
            val gender = binding.genderSpinner.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty() && age.isNotEmpty()) {
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val uid = it.result?.user?.uid ?: return@addOnCompleteListener

                                val userData = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "age" to age.toIntOrNull(),
                                    "gender" to gender,
                                    "profileImageUrl" to ""
                                )

                                db.collection("users").document(uid).set(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "User registered!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, SignInActivity::class.java)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }

                            } else {
                                Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Passwords are not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are Not Allowed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
