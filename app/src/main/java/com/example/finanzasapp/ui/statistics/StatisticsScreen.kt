package com.example.finanzasapp.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finanzasapp.data.repository.ExpenseRepository
import com.example.finanzasapp.ui.home.getCategoryColor
import com.example.finanzasapp.ui.home.getCategoryIcon
import com.example.finanzasapp.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(expenseRepository: ExpenseRepository) {
    val allTransactions by expenseRepository.getExpenses().collectAsState(initial = emptyList())

    val totalIncome = remember(allTransactions) {
        allTransactions.filter { it.isIncome }.sumOf { it.amount }
    }
    val totalExpense = remember(allTransactions) {
        allTransactions.filter { !it.isIncome }.sumOf { it.amount }
    }
    val savingsRate = remember(totalIncome, totalExpense) {
        if (totalIncome > 0) (((totalIncome - totalExpense) / totalIncome) * 100).coerceAtLeast(0.0) else 0.0
    }

    val categoryBreakdown = remember(allTransactions) {
        allTransactions.filter { !it.isIncome }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply {
            currency = java.util.Currency.getInstance("MXN")
        }
    }

    Scaffold(
        containerColor = RyokuBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Análisis y Métricas",
                        fontWeight = FontWeight.Bold,
                        color = InkWhite,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RyokuBlack)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Savings Rate Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RyokuCardBorder, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = RyokuCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TASA DE AHORRO Y EFICIENCIA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkMuted, letterSpacing = 1.sp)
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${String.format(Locale.ROOT, "%.1f", savingsRate)}%",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "del ingreso retenido",
                                fontSize = 13.sp,
                                color = InkMuted,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { (savingsRate / 100f).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldAccent,
                            trackColor = Color(0xFF1F1F1F)
                        )
                    }
                }
            }

            // Summary 2-Grid Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, RyokuCardBorder, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = RyokuCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Flujo Entrante", fontSize = 11.sp, color = InkMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currencyFormat.format(totalIncome), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, RyokuCardBorder, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = RyokuCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Gastos Totales", fontSize = 11.sp, color = InkMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currencyFormat.format(totalExpense), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CoralExpense)
                        }
                    }
                }
            }

            // Breakdown Title
            item {
                Text(
                    text = "DESGLOSE PORCENTUAL POR RUBRO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(categoryBreakdown) { (category, amount) ->
                val percentage = if (totalExpense > 0) (amount / totalExpense) * 100 else 0.0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RyokuCardBorder, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = RyokuCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(getCategoryColor(category).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(category),
                                    contentDescription = null,
                                    tint = getCategoryColor(category),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = InkWhite
                                )
                                Text(
                                    text = currencyFormat.format(amount),
                                    fontSize = 12.sp,
                                    color = InkMuted
                                )
                            }
                            Text(
                                text = "${String.format(Locale.ROOT, "%.1f", percentage)}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = getCategoryColor(category)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (percentage / 100f).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = getCategoryColor(category),
                            trackColor = Color(0xFF1E1E1E)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
