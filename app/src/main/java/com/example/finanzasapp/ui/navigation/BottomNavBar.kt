package com.example.finanzasapp.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.finanzasapp.ui.theme.EmeraldAccent
import com.example.finanzasapp.ui.theme.InkMuted
import com.example.finanzasapp.ui.theme.RyokuCardBorder
import com.example.finanzasapp.ui.theme.RyokuSurface

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    val items = listOf(
        Triple(Screen.Home, "Inicio", Icons.Filled.Home),
        Triple(Screen.Statistics, "Métricas", Icons.Filled.BarChart),
        Triple(Screen.Categories, "Categorías", Icons.Filled.Category),
        Triple(Screen.Budgets, "Presupuestos", Icons.Filled.AccountBalanceWallet)
    )

    NavigationBar(
        containerColor = RyokuSurface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .height(72.dp)
            .border(width = 1.dp, color = RyokuCardBorder)
    ) {
        items.forEach { (screen, label, icon) ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) EmeraldAccent else InkMuted
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) EmeraldAccent else InkMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EmeraldAccent.copy(alpha = 0.15f),
                    selectedIconColor = EmeraldAccent,
                    unselectedIconColor = InkMuted
                )
            )
        }
    }
}