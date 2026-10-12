package com.example.finanzasapp.data.model

data class Expense(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String
)