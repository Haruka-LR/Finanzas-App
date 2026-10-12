package com.example.finanzasapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finanzasapp.data.model.Expense
import com.example.finanzasapp.data.repository.CategoryRepository
import com.example.finanzasapp.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        val period = _uiState.value.selectedPeriod
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                expenseRepository.getExpenses().collectLatest { expenses ->
                    val filteredExpenses = expenses.filter { expense ->
                        isInPeriod(expense, period)
                    }

                    val categoryTotals = categoryRepository
                        .getCategoryTotals(filteredExpenses)
                        .first()
                        .associate { total -> total.category to total.total }

                    _uiState.update {
                        it.copy(
                            totalMonth = filteredExpenses.sumOf(Expense::amount),
                            recentExpenses = filteredExpenses
                                .sortedByDescending(Expense::date)
                                .take(5),
                            categoryTotals = categoryTotals,
                            selectedPeriod = period,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar datos"
                    )
                }
            }
        }
    }

    fun onPeriodChanged(period: Period) {
        if (period == _uiState.value.selectedPeriod) return

        _uiState.update { it.copy(selectedPeriod = period) }
        loadData()
    }

    private fun isInPeriod(expense: Expense, period: Period): Boolean {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
            isLenient = false
        }
        val expenseDate = parser.parse(expense.date)
            ?: throw IllegalArgumentException("Fecha inválida: ${expense.date}")

        val start = Calendar.getInstance().apply {
            when (period) {
                Period.WEEK -> {
                    val daysFromStartOfWeek =
                        (get(Calendar.DAY_OF_WEEK) - firstDayOfWeek + 7) % 7
                    add(Calendar.DAY_OF_YEAR, -daysFromStartOfWeek)
                }

                Period.MONTH -> set(Calendar.DAY_OF_MONTH, 1)
                Period.YEAR -> {
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
            }

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return expenseDate.time >= start.timeInMillis &&
            expenseDate.time <= System.currentTimeMillis()
    }
}