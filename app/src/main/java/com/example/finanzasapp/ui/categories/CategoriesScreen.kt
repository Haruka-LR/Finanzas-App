package com.example.finanzasapp.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finanzasapp.ui.home.getCategoryColor
import com.example.finanzasapp.ui.home.getCategoryIcon
import com.example.finanzasapp.ui.theme.*

data class CategoryItem(val name: String, val type: String = "Gasto", val count: Int = 0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen() {
    var categories by remember {
        mutableStateOf(
            listOf(
                CategoryItem("Comida", "Gasto", 24),
                CategoryItem("Transporte", "Gasto", 18),
                CategoryItem("Servicios", "Gasto", 8),
                CategoryItem("Entretenimiento", "Gasto", 12),
                CategoryItem("Salud", "Gasto", 5),
                CategoryItem("Educación", "Gasto", 3),
                CategoryItem("Hogar", "Gasto", 7),
                CategoryItem("Salario", "Ingreso", 4),
                CategoryItem("Freelance", "Ingreso", 6)
            )
        )
    }

    var showDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = RyokuBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Categorías Activas",
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
                Icon(Icons.Filled.Add, contentDescription = "Nueva categoría")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "CATÁLOGO DE ETIQUETAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = InkMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { cat ->
                    val color = getCategoryColor(cat.name)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, RyokuCardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = RyokuCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(cat.name),
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = cat.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkWhite
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${cat.count} movimientos",
                                fontSize = 11.sp,
                                color = InkMuted
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = RyokuSurface,
            title = { Text("Nueva Categoría", color = InkWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Nombre de la etiqueta", color = InkMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = InkWhite,
                        unfocusedTextColor = InkWhite,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedBorderColor = RyokuCardBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            categories = categories + CategoryItem(newCatName.trim(), "Gasto", 0)
                            newCatName = ""
                            showDialog = false
                        }
                    },
                    enabled = newCatName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("Agregar", color = Color.Black, fontWeight = FontWeight.Bold)
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
