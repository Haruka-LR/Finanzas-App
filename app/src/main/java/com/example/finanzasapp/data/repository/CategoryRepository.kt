package com.example.finanzasapp.data.repository

import com.example.finanzasapp.data.model.CategoryTotal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CategoryRepository {

    fun getCategoryTotals(expenses: List<com.example.finanzasapp.data.model.Expense>): Flow<List<CategoryTotal>> = flow {
        val totals = expenses
            .groupBy { it.category }
            .map { (category, items) ->
                CategoryTotal(
                    category = category,
                    total = items.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.total }

        emit(totals)
    }
}