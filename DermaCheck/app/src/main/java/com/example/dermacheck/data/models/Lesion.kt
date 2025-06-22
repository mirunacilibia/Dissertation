package com.example.dermacheck.data.models

data class Lesion(
    val id: String = "",
    val bodyLocationId: String = "",
    val bodyLocationValue: String = "",
    val createdAt: com.google.firebase.Timestamp? = null,
    val lastUpdated: com.google.firebase.Timestamp? = null,
    val currentDiagnosis: String = "",
    val history: List<LesionHistory> = emptyList(),
    val status: String = "",
    val userEmail: String = ""
)
