package com.example.dermacheck.data.models

data class SkinCondition(
    val name: String = "",
    val type: String = "",
    val locations: List<String> = emptyList(),
    val appearance: String = "",
    val treatment: String = "",
    val images: List<String> = emptyList(),
    val description: String = ""
)