package com.example.finanzasapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.finanzasapp.data.repository.CategoryRepository
import com.example.finanzasapp.data.repository.ExpenseRepository
import com.example.finanzasapp.ui.budgets.BudgetsScreen
import com.example.finanzasapp.ui.categories.CategoriesScreen
import com.example.finanzasapp.ui.home.HomeScreen
import com.example.finanzasapp.ui.home.HomeViewModel
import com.example.finanzasapp.ui.home.HomeViewModelFactory
import com.example.finanzasapp.ui.statistics.StatisticsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val expenseRepository = remember { ExpenseRepository() }
    val categoryRepository = remember { CategoryRepository() }
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(expenseRepository, categoryRepository)
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                expenseRepository = expenseRepository,
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(expenseRepository = expenseRepository)
        }

        composable(Screen.Categories.route) {
            CategoriesScreen()
        }

        composable(Screen.Budgets.route) {
            BudgetsScreen()
        }
    }
}