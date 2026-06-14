package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LoggedMeal
import com.example.ui.theme.*
import com.example.viewmodel.NutriViewModel

@Composable
fun JournalScreen(
    viewModel: NutriViewModel,
    onMealsChange: () -> Unit,
    onOpenScan: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedMeal by remember { mutableStateOf<com.example.data.LoggedMeal?>(null) }
    val meals by viewModel.loggedMeals.collectAsStateWithLifecycle()

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableStateOf(6) } // Default to Sunday (today)

    // Fire animation
    val infiniteTransition = rememberInfiniteTransition(label = "fire")
    val fireScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "fireScale"
    )
    val fireAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "fireAlpha"
    )

    // Dialog state controllers
    var customName by remember { mutableStateOf("") }
    var customCal by remember { mutableStateOf("") }
    var customPro by remember { mutableStateOf("") }
    var customCarb by remember { mutableStateOf("") }
    var customFat by remember { mutableStateOf("") }
    var customSlot by remember { mutableStateOf("Breakfast") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("journal_page")
    ) {
        // Streak indicator card matching visual styles
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(BentoPurpleContainer, BentoSecondaryContainer)
                    )
                )
                .border(1.dp, BentoBorderGrey, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Flame Fire", tint = BentoPurpleDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STREAK LEVEL",
                            style = MaterialTheme.typography.labelSmall.copy(color = BentoPurpleDark, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val uniqueDays = meals.map {
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = it.timestamp
                        "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
                    }.toSet().size
                    val streakText = if (uniqueDays == 0) "Start logging today!" else if (uniqueDays == 1) "1 Day Logged — Keep going!" else "$uniqueDays Days Consistently Logged"
                    Text(
                        text = streakText,
                        style = MaterialTheme.typography.titleMedium.copy(color = BentoPurpleDark, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (uniqueDays >= 7) "You are in the top 5% of healthy eaters!" else "Log meals daily to build your streak!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = BentoPurpleDark.copy(alpha = 0.8f), fontSize = 12.sp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .graphicsLayer {
                            scaleX = fireScale
                            scaleY = fireScale
                            alpha = fireAlpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔥",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 24.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Week Calendar Widget Slider (Matches mock slide spec)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "June 2026",
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            )

            Icon(imageVector = Icons.Default.List, contentDescription = "Calendar Picker", tint = BentoPurpleMedium)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val weekDays = listOf("Mon\n1", "Tue\n2", "Wed\n3", "Thu\n4", "Fri\n5", "Sat\n6", "Sun\n7")
            weekDays.forEachIndexed { index, text ->
                val parts = text.split("\n")
                val dayName = parts[0]
                val dayNum = parts.getOrElse(1) { "" }
                val isSelected = index == selectedDayIndex
                val hasMeals = if (isSelected) meals.isNotEmpty() else false
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) ForestGreen
                            else Color.Transparent
                        )
                        .clickable { selectedDayIndex = index }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else BentoTextGrey,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayNum,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = if (isSelected) Color.White else BentoTextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Dot indicator for meals
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.6f)
                                else if (hasMeals) ForestGreen
                                else Color.Transparent
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Food logs summary header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meal Log Ledger",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAddCustomDialog = true },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoPurpleMedium)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add custom meal", tint = BentoPurpleMedium, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Meal", style = MaterialTheme.typography.labelSmall.copy(color = BentoPurpleMedium))
                    }
                }

                IconButton(onClick = { viewModel.clearAllMeals() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset journal", tint = Color.Gray)
                }
            }
        }

        // Display categorized list - only show real meals on today (index 6), empty for other days
        val mealSlots = listOf("Breakfast", "Lunch", "Dinner", "Snack")
        mealSlots.forEach { slot ->
            val slotMeals = if (selectedDayIndex == 6) meals.filter { it.mealType.equals(slot, ignoreCase = true) } else emptyList()
            val slotCalories = slotMeals.sumOf { it.calories }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBorderGrey)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = slot.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(color = BentoPurpleMedium, letterSpacing = 0.1.sp)
                        )
                        Text(
                            text = "$slotCalories kcal",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = BentoBorderGrey.copy(alpha = 0.5f))

                    if (slotMeals.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenScan() }.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nothing logged yet.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey, fontSize = 14.sp)
                            )
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Quick add", tint = BentoPurpleMedium)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            slotMeals.forEach { meal ->
                                MealRowItem(
                                    meal = meal,
                                    onMealClick = { selectedMeal = meal },
                                    onDeleteClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        viewModel.deleteMeal(meal)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Meal detail + share dialog
    selectedMeal?.let { meal ->
        AlertDialog(
            onDismissRequest = { selectedMeal = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${meal.calories}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                            Text("CALORIES", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${meal.protein}g", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF31111D)))
                            Text("PROTEIN", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${meal.carbs}g", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)))
                            Text("CARBS", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${meal.fat}g", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE65100)))
                            Text("FATS", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Meal Type", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                            Text(meal.mealType, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Health Score", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                            Text("${meal.score}/100", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareText = """
NutriLens AI - Meal Report
--------------------------
Food: ${meal.name}
Meal Type: ${meal.mealType}
Health Score: ${meal.score}/100

Nutrition:
- Calories: ${meal.calories} kcal
- Protein: ${meal.protein}g
- Carbs: ${meal.carbs}g
- Fats: ${meal.fat}g

Tracked with NutriLens AI
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share meal via"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMeal = null }) {
                    Text("Close", style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen))
                }
            }
        )
    }

        // Custom Add Dialog modal
        if (showAddCustomDialog) {
            AlertDialog(
                onDismissRequest = { showAddCustomDialog = false },
                title = {
                    Text(
                        text = "Log Custom Plate Entry",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Meal name (e.g. Besan Chilla)") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_mealName_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoPurpleMedium)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customCal,
                                onValueChange = { customCal = it },
                                label = { Text("kcal") },
                                modifier = Modifier.weight(1f).testTag("custom_calories_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoPurpleMedium)
                            )
                            OutlinedTextField(
                                value = customPro,
                                onValueChange = { customPro = it },
                                label = { Text("Protein (g)") },
                                modifier = Modifier.weight(1f).testTag("custom_protein_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoPurpleMedium)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customCarb,
                                onValueChange = { customCarb = it },
                                label = { Text("Carbs (g)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoPurpleMedium)
                            )
                            OutlinedTextField(
                                value = customFat,
                                onValueChange = { customFat = it },
                                label = { Text("Fat (g)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoPurpleMedium)
                            )
                        }

                        // Slot type picker row selection
                        Text("MEAL TYPE CHOSEN", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { slot ->
                                val isSelected = customSlot == slot
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) BentoPurpleMedium else BentoSecondaryContainer)
                                        .clickable { customSlot = slot }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = slot,
                                        style = MaterialTheme.typography.labelSmall.copy(color = if (isSelected) Color.White else BentoTextGrey)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customName.isNotBlank()) {
                                showAddCustomDialog = false
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                val calVal = customCal.toIntOrNull() ?: 240
                                val proVal = customPro.toIntOrNull() ?: 12
                                val carbVal = customCarb.toIntOrNull() ?: 45
                                val fatVal = customFat.toIntOrNull() ?: 8

                                viewModel.logCustomMeal(
                                    name = customName,
                                    calories = calVal,
                                    protein = proVal,
                                    carbs = carbVal,
                                    fat = fatVal,
                                    mealType = customSlot,
                                    score = 80
                                )

                                // clear
                                customName = ""
                                customCal = ""
                                customPro = ""
                                customCarb = ""
                                customFat = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPurpleMedium)
                    ) {
                        Text("Save Log", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCustomDialog = false }) {
                        Text("Cancel", style = MaterialTheme.typography.labelSmall.copy(color = BentoPurpleMedium))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
