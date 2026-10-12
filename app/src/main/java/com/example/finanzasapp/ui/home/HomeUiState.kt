package com.example.finanzasapp.ui.home

import com.example.finanzasapp.data.model.Expense

enum class Period {
    WEEK,
    MONTH,
    YEAR
}

data class HomeUiState(
    val totalMonth: Double = 0.0,
    val recentExpenses: List<Expense> = emptyList(),
    val categoryTotals: Map<String, Double> = emptyMap(),
    val selectedPeriod: Period = Period.MONTH,
    val isLoading: Boolean = true,
    val error: String? = null
)