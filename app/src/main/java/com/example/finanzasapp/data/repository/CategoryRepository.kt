package com.example.finanzasapp.data.repository

import com.example.finanzasapp.data.model.CategoryTotal
import com.example.finanzasapp.data.model.Expense
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class CategoryRepository @Inject constructor() {

    fun getCategoryTotals(expenses: List<Expense>): Flow<List<CategoryTotal>> = flow {
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