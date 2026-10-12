package com.example.finanzasapp.data.repository

import com.example.finanzasapp.data.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ExpenseRepository {

    fun getExpenses(): Flow<List<Expense>> = flow {
        val data = listOf(
            Expense(1, "Supermercado", 320.0, "Hogar", "2026-09-01"),
            Expense(2, "Gasolina", 180.0, "Transporte", "2026-09-02"),
            Expense(3, "Restaurante", 240.0, "Comida", "2026-09-05"),
            Expense(4, "Netflix", 60.0, "Entretenimiento", "2026-09-08")
        )
        emit(data)
    }
}