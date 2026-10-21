package com.example.finanzasapp.ui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("amount") val amount: Double,
    @SerialName("month") val month: String,
    @SerialName("created_at") val createdAt: String = ""
)