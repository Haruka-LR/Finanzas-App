package com.example.finanzasapp.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finanzasapp.data.model.Expense
import com.example.finanzasapp.data.repository.ExpenseRepository
import com.example.finanzasapp.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    expenseRepository: ExpenseRepository,
    onNavigateToAddExpense: () -> Unit = {}
) {
    val allTransactions by expenseRepository.getExpenses().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogIsIncome by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") } // "Todos", "Gastos", "Ingresos"
    var syncMessage by remember { mutableStateOf<String?>(null) }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply {
            currency = java.util.Currency.getInstance("MXN")
        }
    }

    // Cálculos en tiempo real
    val totalIncome = remember(allTransactions) {
        allTransactions.filter { it.isIncome }.sumOf { it.amount }
    }
    val totalExpense = remember(allTransactions) {
        allTransactions.filter { !it.isIncome }.sumOf { it.amount }
    }
    val netBalance = totalIncome - totalExpense

    val filteredTransactions = remember(allTransactions, selectedFilter) {
        when (selectedFilter) {
            "Gastos" -> allTransactions.filter { !it.isIncome }
            "Ingresos" -> allTransactions.filter { it.isIncome }
            else -> allTransactions
        }
    }

    val categoryBreakdown = remember(allTransactions) {
        allTransactions.filter { !it.isIncome }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { exp -> exp.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    Scaffold(
        containerColor = RyokuBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TerracottaAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("力", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Finanzas",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = InkWhite
                                )
                            )
                            Text(
                                text = "Amaterasu Engine · Wear OS Linked",
                                style = MaterialTheme.typography.labelSmall.copy(color = EmeraldAccent)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            syncMessage = "⟳ Reloj Wear OS sincronizado con MySQL"
                        }
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = "Sincronizar", tint = EmeraldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RyokuBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dialogIsIncome = false
                    showAddDialog = true
                },
                containerColor = EmeraldAccent,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Registrar", modifier = Modifier.size(28.dp))
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
            // Toast de sincronización
            if (syncMessage != null) {
                item {
                    LaunchedEffect(syncMessage) {
                        kotlinx.coroutines.delay(2500)
                        syncMessage = null
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldAccent.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(syncMessage!!, color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Hero Balance Card (Estilo Fintech Glassmorphism)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RyokuCardBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = RyokuCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF161616),
                                        Color(0xFF0D0D0D)
                                    )
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldAccent)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("BALANCE NETO DISPONIBLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkMuted, letterSpacing = 1.sp)
                                }
                                Text("MXN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkBone)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = currencyFormat.format(netBalance),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = InkWhite
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Income vs Expense Summary Strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF080808))
                                    .border(1.dp, RyokuCardBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Ingresos", fontSize = 11.sp, color = InkMuted)
                                    Text(
                                        text = "+${currencyFormat.format(totalIncome)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldAccent
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(34.dp)
                                        .background(RyokuCardBorder)
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Gastos", fontSize = 11.sp, color = InkMuted)
                                    Text(
                                        text = "-${currencyFormat.format(totalExpense)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralExpense
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions Buttons Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            dialogIsIncome = false
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralExpense.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralExpense.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = CoralExpense, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gasto", color = CoralExpense, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            dialogIsIncome = true
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ingreso", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Category Spending Progress Bars
            if (categoryBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = "DISTRIBUCIÓN DE GASTOS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, RyokuCardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = RyokuCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            categoryBreakdown.take(4).forEach { (category, amount) ->
                                val pct = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = getCategoryIcon(category),
                                                contentDescription = null,
                                                tint = getCategoryColor(category),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(category, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkBone)
                                        }
                                        Text(
                                            currencyFormat.format(amount),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkWhite
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { pct.coerceIn(0f, 1f) },
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
                    }
                }
            }

            // Transaction Filter Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MOVIMIENTOS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkMuted,
                        letterSpacing = 1.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Todos", "Gastos", "Ingresos").forEach { filter ->
                            val isSel = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) EmeraldAccent else Color(0xFF181818))
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.Black else InkBone
                                )
                            }
                        }
                    }
                }
            }

            // Transactions Feed
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay movimientos en esta categoría", color = InkMuted, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { item ->
                    TransactionRowItem(
                        expense = item,
                        currencyFormat = currencyFormat,
                        onDelete = {
                            expenseRepository.deleteExpense(item.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddDialog) {
        AddMovementDialog(
            isIncomeInitial = dialogIsIncome,
            onDismiss = { showAddDialog = false },
            onSave = { title, amount, category, isIncome ->
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
                expenseRepository.addTransaction(title, amount, category, date, isIncome)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TransactionRowItem(
    expense: Expense,
    currencyFormat: NumberFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, RyokuCardBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RyokuCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(getCategoryColor(expense.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.category),
                    contentDescription = null,
                    tint = getCategoryColor(expense.category),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkWhite
                )
                Text(
                    text = "${expense.category} • ${expense.date}",
                    fontSize = 11.sp,
                    color = InkMuted
                )
            }

            Text(
                text = "${if (expense.isIncome) "+" else "-"}${currencyFormat.format(expense.amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (expense.isIncome) EmeraldAccent else CoralExpense
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Borrar",
                    tint = InkMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddMovementDialog(
    isIncomeInitial: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, category: String, isIncome: Boolean) -> Unit
) {
    var isIncome by remember { mutableStateOf(isIncomeInitial) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    val expenseCategories = listOf("Comida", "Transporte", "Servicios", "Entretenimiento", "Salud", "Educación", "Hogar", "Otros")
    val incomeCategories = listOf("Salario", "Freelance", "Inversiones", "Ventas", "Otros")

    val categories = if (isIncome) incomeCategories else expenseCategories
    var selectedCategory by remember(isIncome) { mutableStateOf(categories.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RyokuSurface,
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF000000))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isIncome) CoralExpense else Color.Transparent)
                        .clickable { isIncome = false }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Gasto", color = if (!isIncome) Color.White else InkMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isIncome) EmeraldAccent else Color.Transparent)
                        .clickable { isIncome = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ingreso", color = if (isIncome) Color.Black else InkMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto ($)", color = InkMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = InkWhite,
                        unfocusedTextColor = InkWhite,
                        focusedBorderColor = if (isIncome) EmeraldAccent else CoralExpense,
                        unfocusedBorderColor = RyokuCardBorder
                    )
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Descripción / Concepto", color = InkMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = InkWhite,
                        unfocusedTextColor = InkWhite,
                        focusedBorderColor = if (isIncome) EmeraldAccent else CoralExpense,
                        unfocusedBorderColor = RyokuCardBorder
                    )
                )

                Text("Categoría:", fontSize = 12.sp, color = InkMuted)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSel = selectedCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isIncome) EmeraldAccent else CoralExpense,
                                selectedLabelColor = if (isIncome) Color.Black else Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onSave(title, amount, selectedCategory, isIncome)
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) EmeraldAccent else CoralExpense)
            ) {
                Text("Guardar", color = if (isIncome) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = InkMuted)
            }
        }
    )
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "comida", "alimentos", "restaurante" -> CatFood
        "transporte", "gasolina", "uber" -> CatTransport
        "servicios", "netflix", "internet" -> CatServices
        "entretenimiento", "cine", "ocio" -> CatEntertainment
        "salud", "farmacia" -> CatHealth
        "educación" -> CatEducation
        "salario", "nómina" -> CatSalary
        "freelance" -> IndigoInfo
        else -> CatHousing
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "comida", "alimentos", "restaurante" -> Icons.Filled.Restaurant
        "transporte", "uber", "gasolina" -> Icons.Filled.DirectionsCar
        "servicios", "internet", "luz" -> Icons.Filled.Bolt
        "entretenimiento", "cine", "ocio" -> Icons.Filled.SportsEsports
        "salud", "farmacia" -> Icons.Filled.MedicalServices
        "educación" -> Icons.Filled.School
        "salario", "nómina" -> Icons.Filled.Payments
        "freelance" -> Icons.Filled.LaptopChromebook
        else -> Icons.Filled.Category
    }
}
