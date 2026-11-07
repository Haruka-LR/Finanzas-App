package com.example.finanzasapp.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class WearScreenState {
    BALANCE, QUICK_EXPENSE, RECENT, SUCCESS
}

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF10B981),
                    background = Color(0xFF000000),
                    surface = Color(0xFF111111),
                    onPrimary = Color.Black,
                    onBackground = Color(0xFFF5F5F5)
                )
            ) {
                WearFinanzasApp()
            }
        }
    }
}

@Composable
fun WearFinanzasApp() {
    var screenState by remember { mutableStateOf(WearScreenState.BALANCE) }
    var balance by remember { mutableStateOf(14850.00) }
    var todaySpent by remember { mutableStateOf(350.00) }
    var lastAddedExpense by remember { mutableStateOf("") }

    val recentItems = remember {
        mutableStateListOf(
            Pair("Comida", 150.00),
            Pair("Uber", 89.00),
            Pair("Café", 45.00)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(targetState = screenState, label = "WearNav") { state ->
            when (state) {
                WearScreenState.BALANCE -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⌚ FINANZAS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%,.0f", balance)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Hoy: -$${String.format("%,.0f", todaySpent)}",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { screenState = WearScreenState.QUICK_EXPENSE },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Gasto Rápido", tint = Color.Black)
                            }
                            Button(
                                onClick = { screenState = WearScreenState.RECENT },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)),
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Filled.List, contentDescription = "Movimientos", tint = Color.White)
                            }
                        }
                    }
                }

                WearScreenState.QUICK_EXPENSE -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Gasto Rápido",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        val presets = listOf(
                            Pair("Café", 45.0),
                            Pair("Comida", 150.0),
                            Pair("Transporte", 80.0),
                            Pair("Oxxo", 60.0)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            presets.forEach { (cat, amt) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1A1A1A))
                                        .clickable {
                                            balance -= amt
                                            todaySpent += amt
                                            recentItems.add(0, Pair(cat, amt))
                                            lastAddedExpense = "-$${amt.toInt()} en $cat"
                                            screenState = WearScreenState.SUCCESS
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = cat, fontSize = 12.sp, color = Color.White)
                                    Text(text = "$${amt.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                            }
                        }
                        TextButton(
                            onClick = { screenState = WearScreenState.BALANCE },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Atrás", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }

                WearScreenState.RECENT -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Últimos Movimientos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(recentItems.take(5)) { (cat, amt) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF181818))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, fontSize = 11.sp, color = Color.LightGray)
                                    Text("-$${amt.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }
                        }
                        IconButton(
                            onClick = { screenState = WearScreenState.BALANCE },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Gray)
                        }
                    }
                }

                WearScreenState.SUCCESS -> {
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1400)
                        screenState = WearScreenState.BALANCE
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("¡Guardado!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(lastAddedExpense, fontSize = 11.sp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}
