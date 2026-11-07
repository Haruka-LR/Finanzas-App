package com.example.finanzasapp.data.repository

import com.example.finanzasapp.data.model.Expense
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class ExpenseRepository @Inject constructor() {

    private val _expenses = MutableStateFlow(
        listOf(
            Expense(1, "Nómina Quincenal", 18500.0, "Salario", "2026-11-06", isIncome = true),
            Expense(2, "Supermercado Semanal", 1450.0, "Comida", "2026-11-06", isIncome = false),
            Expense(3, "Gasolina Shell V-Power", 550.0, "Transporte", "2026-11-05", isIncome = false),
            Expense(4, "Proyecto Freelance UI", 4200.0, "Freelance", "2026-11-04", isIncome = true),
            Expense(5, "Cena Restaurante Japonés", 480.0, "Comida", "2026-11-04", isIncome = false),
            Expense(6, "Suscripción Netflix & Spotify", 349.0, "Servicios", "2026-11-02", isIncome = false),
            Expense(7, "Internet Fibra Óptica 500MB", 599.0, "Servicios", "2026-11-01", isIncome = false),
            Expense(8, "Farmacia y Vitaminas", 220.0, "Salud", "2026-10-30", isIncome = false),
            Expense(9, "Viaje Uber Aeropuerto", 185.0, "Transporte", "2026-10-28", isIncome = false),
            Expense(10, "Cine IMAX y Palomitas", 390.0, "Entretenimiento", "2026-10-24", isIncome = false)
        )
    )

    fun getExpenses(): Flow<List<Expense>> = _expenses.asStateFlow()

    fun addTransaction(title: String, amount: Double, category: String, date: String, isIncome: Boolean = false) {
        _expenses.update { current ->
            val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            listOf(Expense(nextId, title, amount, category, date, isIncome)) + current
        }
    }

    fun addExpense(title: String, amount: Double, category: String, date: String) {
        addTransaction(title, amount, category, date, isIncome = false)
    }

    fun deleteExpense(id: Int) {
        _expenses.update { current ->
            current.filterNot { it.id == id }
        }
    }
}