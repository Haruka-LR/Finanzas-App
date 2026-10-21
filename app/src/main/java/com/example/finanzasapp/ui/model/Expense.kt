package com.example.finanzasapp.ui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("title") val title: String,
    @SerialName("amount") val amount: Double,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("date") val date: String,
    @SerialName("note") val note: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)