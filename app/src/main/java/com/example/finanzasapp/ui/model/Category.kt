package com.example.finanzasapp.ui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("name") val name: String,
    @SerialName("color") val color: String? = null,
    @SerialName("icon") val icon: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)