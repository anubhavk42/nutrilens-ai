package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.items
import android.content.Intent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LoggedMeal
import com.example.ui.theme.*
import com.example.viewmodel.NutriViewModel

@Composable
fun DashboardScreen(
    viewModel: NutriViewModel,
    onScanTrigger: () -> Unit,
    onOpenBarcode: () -> Unit,
    onNavigateToChat: () -> Unit
)  {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var selectedMeal by remember { mutableStateOf<com.example.data.LoggedMeal?>(null) }
    var waterGlasses by remember { mutableStateOf(0) }
    val waterTarget = 8
    var stepCount by remember { mutableStateOf(0) }
    val stepTarget = 10000
    val caloriesFromSteps = (stepCount * 0.04).toInt()

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val stepSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER)
        var initialSteps = -1
        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent) {
                if (initialSteps == -1) initialSteps = event.values[0].toInt()
                stepCount = event.values[0].toInt() - initialSteps
            }
            override fun onAccuracyChanged(sensor: android.hardware.Sensor, accuracy: Int) {}
        }
        if (stepSensor != null) {
            sensorManager.registerListener(listener, stepSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val meals by viewModel.loggedMeals.collectAsStateWithLifecycle()

    val targetCal = profile?.caloriesBaseline ?: 1840
    val targetPro = profile?.proteinTarget ?: 92
    val targetCarbs = profile?.carbsTarget ?: 230
    val targetFat = profile?.fatTarget ?: 61

    val currentCal = meals.sumOf { it.calories }
    val currentPro = meals.sumOf { it.protein }
    val currentCarbs = meals.sumOf { it.carbs }
    val currentFat = meals.sumOf { it.fat }

    val calRemaining = (targetCal - currentCal).coerceAtLeast(0)
    val profileName = profile?.name ?: "Anubhav"
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning,"
        hour < 17 -> "Good afternoon,"
        else -> "Good evening,"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF615E58))
                )
                Text(
                    text = "$profileName!",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    ),
                    modifier = Modifier.testTag("greeting_user_name")
                )
            }


        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TODAY'S CALORIES",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                )
                Spacer(modifier = Modifier.height(20.dp))

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                    val sweepProgress = if (targetCal > 0) (currentCal.toFloat() / targetCal.toFloat()) else 0f
                    Canvas(modifier = Modifier.size(180.dp)) {
                        drawArc(
                            color = BentoPurpleLight.copy(alpha = 0.3f),
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        drawArc(
                            color = BentoPurpleDark,
                            startAngle = -220f,
                            sweepAngle = sweepProgress.coerceIn(0f, 1f) * 260f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%,d", currentCal),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "of %,d kcal consumed".format(targetCal),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(BentoPurpleMedium)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "%,d kcal left".format(calRemaining),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MacroBar(
                        label = "CARBS",
                        consumed = currentCarbs,
                        target = targetCarbs,
                        color = BentoPurpleMedium,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        subTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        trackColor = BentoPurpleLight.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MacroBar(
                        label = "PROTEIN",
                        consumed = currentPro,
                        target = targetPro,
                        color = Color(0xFF31111D),
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        subTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        trackColor = BentoPurpleLight.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MacroBar(
                        label = "FATS (Ghee)",
                        consumed = currentFat,
                        target = targetFat,
                        color = Color(0xFFE65100),
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        subTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        trackColor = BentoPurpleLight.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val showProteinAlert = currentPro < (targetPro * 0.7)
        AnimatedVisibility(visible = showProteinAlert) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFF5E6))
                    .padding(16.dp)
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Protein Alert / Recommendation",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You are currently ${targetPro - currentPro}g short from daily target. Topping up dinner with Paneer or 1 katori of Moong sprouts will support muscle regeneration aligned with ${profile?.goal}!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = Color(0xFF67645E)
                            )
                        )
                    }
                }
            }
        }

        // Deficit Tracker - Full Functional Version
        val deficit = targetCal - currentCal
        val isDeficit = deficit > 0
        val progressPercent = if (targetCal > 0) (currentCal.toFloat() / targetCal.toFloat()).coerceIn(0f, 1f) else 0f
        val targetDeficit = 500 // Standard 500 kcal/day deficit for weight loss
        val deficitProgress = if (targetDeficit > 0) (deficit.toFloat() / targetDeficit.toFloat()).coerceIn(0f, 1f) else 0f

        // Smart meal suggestions
        val mealSuggestions = if (isDeficit) {
            when {
                deficit > 800 -> listOf("🥗 Paneer Bhurji", "🍗 Grilled Chicken", "🥜 Moong Dal Chilla")
                deficit > 400 -> listOf("🍌 Banana + Peanut Butter", "🥛 Protein Shake", "🧀 Paneer Tikka")
                else -> listOf("🍎 Apple", "🥜 Handful of Nuts", "🍵 Green Tea")
            }
        } else {
            listOf("🥗 Light Salad", "🍵 Green Tea", "🚶 30 min Walk")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorderGrey)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                                .background(if (isDeficit) ForestGreen.copy(alpha = 0.1f) else Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isDeficit) "📉" else "📈", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = if (isDeficit) "${deficit} kcal deficit" else "${-deficit} kcal surplus",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDeficit) ForestGreen else Color(0xFFE53935)
                                )
                            )
                            Text(
                                text = if (isDeficit) "Daily target: $targetCal kcal" else "You exceeded your goal!",
                                style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(if (isDeficit) ForestGreen.copy(alpha = 0.1f) else Color(0xFFFFEBEE))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isDeficit) "On Track" else "Over Goal",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isDeficit) ForestGreen else Color(0xFFE53935),
                                fontWeight = FontWeight.Bold, fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calorie progress bar
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Consumed: ${currentCal} kcal", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 11.sp))
                        Text("Goal: ${targetCal} kcal", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 11.sp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(ForestGreen.copy(alpha = 0.1f))) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressPercent)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (progressPercent > 1f) Color(0xFFE53935) else ForestGreen)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(progressPercent * 100).toInt()}% of daily goal consumed",
                        style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 10.sp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weight loss projection
                if (isDeficit) {
                    val weeklyLoss = (deficit * 7) / 7700f
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(ForestGreen.copy(alpha = 0.05f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⚖️", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = "At this rate: ~${String.format("%.2f", weeklyLoss)}kg loss/week",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
                                )
                                Text(
                                    text = "7700 kcal deficit = 1kg weight loss",
                                    style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Smart suggestions
                HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isDeficit) "💡 Suggested to eat more:" else "💡 Suggested to balance:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    mealSuggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(BentoSecondaryContainer)
                                .border(1.dp, BentoBorderGrey, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(suggestion, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Step Counter - Premium Green Design
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorderGrey)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                    val stepProgress = (stepCount.toFloat() / stepTarget).coerceIn(0f, 1f)
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(72.dp)) {
                        drawArc(color = ForestGreen.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                        if (stepProgress > 0f) drawArc(color = androidx.compose.ui.graphics.Color(0xFF1A6B47), startAngle = -90f, sweepAngle = stepProgress * 360f, useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (stepCount >= 1000) "${stepCount/1000}k" else "$stepCount",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreen, fontSize = 14.sp))
                        Text("steps", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 9.sp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Steps", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("$stepCount / $stepTarget steps", style = MaterialTheme.typography.bodySmall.copy(color = BentoTextGrey))
                    Text("${caloriesFromSteps} kcal burned", style = MaterialTheme.typography.bodySmall.copy(color = ForestGreen))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(ForestGreen.copy(alpha = 0.15f))) {
                        Box(modifier = Modifier.fillMaxWidth((stepCount.toFloat() / stepTarget).coerceIn(0f, 1f)).fillMaxHeight().clip(CircleShape).background(ForestGreen))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (stepCount >= stepTarget) "🎉 Goal achieved!" else "${stepTarget - stepCount} steps remaining",
                        style = MaterialTheme.typography.labelSmall.copy(color = if (stepCount >= stepTarget) ForestGreen else BentoTextGrey, fontSize = 10.sp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Water Tracker - Premium Green Design
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorderGrey)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(ForestGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Text("💧", fontSize = 22.sp)
                        }
                        Column {
                            Text("Water Intake", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                            Text("$waterGlasses of $waterTarget glasses", style = MaterialTheme.typography.bodySmall.copy(color = BentoTextGrey))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(BentoSecondaryContainer).border(1.dp, BentoBorderGrey, CircleShape)
                            .clickable { if (waterGlasses > 0) waterGlasses-- }, contentAlignment = Alignment.Center) {
                            Text("-", style = MaterialTheme.typography.titleMedium.copy(color = ForestGreen, fontWeight = FontWeight.Bold))
                        }
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(ForestGreen)
                            .clickable { if (waterGlasses < waterTarget) waterGlasses++ }, contentAlignment = Alignment.Center) {
                            Text("+", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (i in 1..waterTarget) {
                        Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape)
                            .background(if (i <= waterGlasses) ForestGreen else ForestGreen.copy(alpha = 0.15f)))
                    }
                }
                if (waterGlasses >= waterTarget) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🎉 Daily water goal achieved!", style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen, fontWeight = FontWeight.Bold))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Quick Add
        val frequentMeals = meals.groupBy { it.name }.map { (_, list) -> list.first() }.take(4)
        if (frequentMeals.isNotEmpty()) {
            Text("⚡ Quick Add", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
            Text("Tap to re-log your frequent meals", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
            Spacer(modifier = Modifier.height(10.dp))
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(frequentMeals) { meal ->
                    Box(modifier = Modifier.width(140.dp).clip(RoundedCornerShape(16.dp)).background(Color.White)
                        .border(1.dp, Color(0xFFEBEFE9), RoundedCornerShape(16.dp))
                        .clickable {
                            val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                            val mealType = when { h < 11 -> "Breakfast"; h < 15 -> "Lunch"; h < 18 -> "Snack"; else -> "Dinner" }
                            viewModel.logCustomMeal(name = meal.name, calories = meal.calories, protein = meal.protein, carbs = meal.carbs, fat = meal.fat, mealType = mealType, score = meal.score)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }.padding(12.dp)) {
                        Column {
                            Text(meal.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${meal.calories} kcal", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72), fontSize = 11.sp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(ForestGreen).padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center) {
                                Text("+ Add", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Log",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            TextButton(onClick = onNavigateToChat) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "NutriBot AI Chat", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ask NutriBot", style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen))
                }
            }
        }

        if (meals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No meals logged today. Use custom scan to start!",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF615E58)),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                meals.forEach { meal ->
                    MealRowItem(
                        meal = meal,
                        onMealClick = { selectedMeal = meal },
                        onDeleteClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteMeal(meal)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Meal detail dialog
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
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${meal.calories}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = com.example.ui.theme.ForestGreen))
                            Text(text = "CALORIES", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${meal.protein}g", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF31111D)))
                            Text(text = "PROTEIN", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${meal.carbs}g", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)))
                            Text(text = "CARBS", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${meal.fat}g", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE65100)))
                            Text(text = "FATS", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Meal Type", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                            Text(text = meal.mealType, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Health Score", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72)))
                            Text(text = "${meal.score}/100", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = com.example.ui.theme.ForestGreen))
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
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.ForestGreen)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = androidx.compose.ui.Modifier.size(16.dp))
                    Spacer(modifier = androidx.compose.ui.Modifier.width(6.dp))
                    Text("Share", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMeal = null }) {
                    Text("Close", style = MaterialTheme.typography.labelSmall.copy(color = com.example.ui.theme.ForestGreen))
                }
            }
        )
    }
}

@Composable
fun MacroBar(
    label: String,
    consumed: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    subTextColor: Color = Color(0xFF6F7A72),
    trackColor: Color = Color(0xFFEBEFE9)
) {
    val progress = if (target > 0) (consumed.toFloat() / target.toFloat()) else 0f
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = subTextColor)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${consumed}g / ${target}g",
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, color = textColor)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MealRowItem(
    meal: LoggedMeal,
    onMealClick: () -> Unit = {},
    onDeleteClick: () -> Unit
) {
    var expandedDelete by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .combinedClickable(
                onClick = { onMealClick() },
                onLongClick = { expandedDelete = true }
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (meal.score >= 85) Color(0xFFEBF9F0)
                            else if (meal.score >= 70) Color(0xFFFFF5E6)
                            else Color(0xFFFFCCCD)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = meal.score.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (meal.score >= 85) ForestGreen
                            else if (meal.score >= 70) Color(0xFFE65100)
                            else Color(0xFF763036)
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = meal.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 160.dp)
                        )
                        if (meal.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "AI Verified Specs",
                                tint = ForestGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = meal.mealType,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72))
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(max = 120.dp)) {
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "P:${meal.protein}g C:${meal.carbs}g F:${meal.fat}g",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFF615E58)
                        ),
                        maxLines = 1
                    )
                }

                if (expandedDelete) {
                    IconButton(onClick = { onDeleteClick(); expandedDelete = false }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete meal log", tint = Color.Red)
                    }
                }
            }
        }
    }
}
