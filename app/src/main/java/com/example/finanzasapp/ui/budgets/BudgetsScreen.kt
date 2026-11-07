package com.example.finanzasapp.ui.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finanzasapp.ui.home.getCategoryColor
import com.example.finanzasapp.ui.home.getCategoryIcon
import com.example.finanzasapp.ui.theme.*
import java.text.NumberFormat
import java.util.*

data class BudgetItem(
    val id: Int,
    val category: String,
    val limit: Double,
    val spent: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen() {
    var budgets by remember {
        mutableStateOf(
            listOf(
                BudgetItem(1, "Comida", 4500.0, 3120.0),
                BudgetItem(2, "Transporte", 1800.0, 1650.0),
                BudgetItem(3, "Servicios", 2200.0, 1548.0),
                BudgetItem(4, "Entretenimiento", 1500.0, 780.0)
            )
        )
    }

    var showDialog by remember { mutableStateOf(false) }

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
                        "Presupuestos",
                        fontWeight = FontWeight.Bold,
                        color = InkWhite,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RyokuBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = EmeraldAccent,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo Presupuesto")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "LÍMITES Y TOPES MENSUALES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkMuted,
                    letterSpacing = 1.sp
                )
            }

            items(budgets, key = { it.id }) { budget ->
                val progress = (budget.spent / budget.limit).toFloat()
                val isNearLimit = progress >= 0.8f
                val remaining = budget.limit - budget.spent

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (isNearLimit) CoralExpense.copy(alpha = 0.5f) else RyokuCardBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RyokuCard)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(getCategoryColor(budget.category).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(budget.category),
                                        contentDescription = null,
                                        tint = getCategoryColor(budget.category),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = budget.category,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkWhite
                                )
                            }

                            if (isNearLimit) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CoralExpense.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = CoralExpense, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Alerta ${(progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoralExpense)
                                }
                            } else {
                                Text(
                                    text = "${(progress * 100).toInt()}% usado",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isNearLimit) CoralExpense else EmeraldAccent,
                            trackColor = Color(0xFF1E1E1E)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Gastado: ${currencyFormat.format(budget.spent)}",
                                fontSize = 12.sp,
                                color = InkMuted
                            )
                            Text(
                                text = "Restante: ${currencyFormat.format(remaining)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remaining > 0) InkBone else CoralExpense
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showDialog) {
        var catName by remember { mutableStateOf("") }
        var limitAmount by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = RyokuSurface,
            title = { Text("Fijar Nuevo Presupuesto", color = InkWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Categoría / Concepto", color = InkMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = InkWhite,
                            unfocusedTextColor = InkWhite,
                            focusedBorderColor = EmeraldAccent,
                            unfocusedBorderColor = RyokuCardBorder
                        )
                    )

                    OutlinedTextField(
                        value = limitAmount,
                        onValueChange = { limitAmount = it },
                        label = { Text("Monto Límite ($)", color = InkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = InkWhite,
                            unfocusedTextColor = InkWhite,
                            focusedBorderColor = EmeraldAccent,
                            unfocusedBorderColor = RyokuCardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = limitAmount.toDoubleOrNull() ?: 0.0
                        if (catName.isNotBlank() && amt > 0) {
                            budgets = budgets + BudgetItem(budgets.size + 1, catName.trim(), amt, 0.0)
                            showDialog = false
                        }
                    },
                    enabled = catName.isNotBlank() && (limitAmount.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("Crear", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = InkMuted)
                }
            }
        )
    }
}
