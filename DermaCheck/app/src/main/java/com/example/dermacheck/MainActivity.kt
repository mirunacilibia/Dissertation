package com.example.dermacheck

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var emailText: TextView
    private lateinit var ageText: TextView
    private lateinit var genderText: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        emailText = findViewById(R.id.emailText)

        logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            finish()
        }

        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        emailText.text = "Email: ${document.getString("email") ?: "-"}"
                        ageText.text = "Age: ${document.getLong("age")?.toInt() ?: "-"}"
                        genderText.text = "Gender: ${document.getString("gender") ?: "-"}"
                    }
                }
                .addOnFailureListener {
                    emailText.text = "Failed to load user data"
                }
        }
    }
}
