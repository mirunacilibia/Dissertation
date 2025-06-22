package com.example.dermacheck.data.models

data class LesionHistory(
    val date: com.google.firebase.Timestamp? = null,
    val diagnosis: String = "",
    val imageUrl: String = ""
)
