package com.example.ui

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import com.example.ui.theme.*
import com.example.viewmodel.NutriViewModel

@Composable
fun InsightsScreen(
    viewModel: NutriViewModel,
    onAlternativesClick: () -> Unit
) {
    val meals by viewModel.loggedMeals.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val profile = viewModel.userProfile.collectAsStateWithLifecycle().value

    // Animated progress for gauge
    val gaugeAnim = remember { Animatable(0f) }
    val hitRatio = if (meals.isNotEmpty()) meals.count { it.score >= 80 }.toFloat() / meals.size.toFloat() else 0f
    LaunchedEffect(hitRatio) {
        gaugeAnim.animateTo(hitRatio, animationSpec = tween(1200, easing = FastOutSlowInEasing))
    }

    // Animated macro values
    val macroAnim = remember { Animatable(0f) }
    LaunchedEffect(meals.size) {
        macroAnim.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

    // Pulse animation for spotlight
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // 1. ANIMATED QUALITY GAUGE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorderGrey)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Weekly Quality Score", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val stroke = 16.dp.toPx()
                        val radius = (size.minDimension - stroke) / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        drawArc(color = ForestGreen.copy(alpha = 0.15f), startAngle = -220f, sweepAngle = 260f, useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2),
                            style = Stroke(width = stroke, cap = StrokeCap.Round))
                        drawArc(color = ForestGreen, startAngle = -220f, sweepAngle = gaugeAnim.value * 260f, useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2),
                            style = Stroke(width = stroke, cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(gaugeAnim.value * 100).toInt()}%",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp, fontWeight = FontWeight.Bold, color = ForestGreen))
                        Text("Quality", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (meals.isEmpty()) "Start logging meals to see your score!"
                           else "${(hitRatio * 100).toInt()}% of your meals meet optimal dietary guidelines.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey, textAlign = TextAlign.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. ANIMATED MACRO DONUT
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorderGrey)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Macro Split", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                Text("Protein · Carbs · Fats breakdown", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                Spacer(modifier = Modifier.height(16.dp))

                val totalPro = meals.sumOf { it.protein }.toFloat()
                val totalCarbs = meals.sumOf { it.carbs }.toFloat()
                val totalFat = meals.sumOf { it.fat }.toFloat()
                val totalMacros = totalPro + totalCarbs + totalFat

                if (totalMacros == 0f) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("No meals logged yet", style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey))
                    }
                } else {
                    val proRatio = (totalPro / totalMacros) * macroAnim.value
                    val carbsRatio = (totalCarbs / totalMacros) * macroAnim.value
                    val fatRatio = (totalFat / totalMacros) * macroAnim.value
                    val proColor = Color(0xFF1565C0)
                    val carbsColor = ForestGreen
                    val fatColor = Color(0xFFE65100)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(140.dp)) {
                                val strokeWidth = 24.dp.toPx()
                                val radius = (size.minDimension - strokeWidth) / 2
                                val center = Offset(size.width / 2, size.height / 2)
                                val proSweep = proRatio * 360f
                                val carbsSweep = carbsRatio * 360f
                                val fatSweep = fatRatio * 360f
                                drawArc(color = proColor, startAngle = -90f, sweepAngle = proSweep, useCenter = false,
                                    topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt))
                                drawArc(color = carbsColor, startAngle = -90f + proSweep, sweepAngle = carbsSweep, useCenter = false,
                                    topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt))
                                drawArc(color = fatColor, startAngle = -90f + proSweep + carbsSweep, sweepAngle = fatSweep, useCenter = false,
                                    topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${totalMacros.toInt()}g", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                                Text("Total", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            listOf(Triple("Protein", "${totalPro.toInt()}g", proColor),
                                   Triple("Carbs", "${totalCarbs.toInt()}g", carbsColor),
                                   Triple("Fats", "${totalFat.toInt()}g", fatColor)).forEach { (label, value, color) ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(color))
                                    Column {
                                        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = color))
                                        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 10.sp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. ANIMATED CALORIE BAR CHART (replaces heatmap)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorderGrey)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Meal Type Breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                Text("Calories logged per meal type", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                Spacer(modifier = Modifier.height(16.dp))

                val mealTypes = listOf("Breakfast", "Lunch", "Snack", "Dinner")
                val mealEmojis = listOf("🌅", "☀️", "🍎", "🌙")
                val mealCals = mealTypes.map { type -> meals.filter { it.mealType.equals(type, true) }.sumOf { it.calories } }
                val maxCal = mealCals.max().coerceAtLeast(1)

                mealTypes.forEachIndexed { i, type ->
                    val barProgress = (mealCals[i].toFloat() / maxCal) * macroAnim.value
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(mealEmojis[i], fontSize = 16.sp)
                                Text(type, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                            }
                            Text("${mealCals[i]} kcal", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(ForestGreen.copy(alpha = 0.1f))) {
                            Box(modifier = Modifier.fillMaxWidth(barProgress).fillMaxHeight().clip(CircleShape).background(ForestGreen))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. PULSING SPOTLIGHT CARD
        Box(modifier = Modifier.scale(pulseScale)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SoftOrangeTint),
                border = BorderStroke(1.dp, CautionOrange.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(CautionOrange.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Text("⭐", fontSize = 22.sp)
                    }
                    Column {
                        Text("TOP MEAL SPOTLIGHT", style = MaterialTheme.typography.labelSmall.copy(color = CautionOrange, fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        val bestMeal = meals.maxByOrNull { it.score }
                        if (bestMeal != null) {
                            Text("Your best meal was ${bestMeal.name} with a health score of ${bestMeal.score}/100! It provided ${bestMeal.protein}g protein, ${bestMeal.carbs}g carbs and ${bestMeal.calories} kcal.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey, fontSize = 13.sp))
                        } else {
                            Text("Start logging meals to see your nutrition spotlight here!", style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey, fontSize = 13.sp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. STAPLE FOODS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorderGrey)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Your Staple Foods", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                Spacer(modifier = Modifier.height(4.dp))
                if (meals.isEmpty()) {
                    Text("No meals logged yet. Start scanning to see your staple foods!", style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey, fontSize = 13.sp))
                } else {
                    val topMeals = meals.groupBy { it.name }.map { (name, list) -> Triple(name, list.size, list.maxOf { it.score }) }.sortedByDescending { it.second }.take(5)
                    topMeals.forEach { (name, count, score) ->
                        val label = when { score >= 85 -> "Excellent"; score >= 70 -> "Good Choice"; else -> "Moderate" }
                        val labelColor = when { score >= 85 -> ForestGreen; score >= 70 -> Color(0xFF1565C0); else -> CautionOrange }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Logged $count time${if (count > 1) "s" else ""} · Score: $score/100", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = BentoTextGrey))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(labelColor.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                Text(label, style = MaterialTheme.typography.labelSmall.copy(color = labelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                            }
                        }
                        HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. EXPORT BUTTON
        Button(
            onClick = {
                val totalCal = meals.sumOf { it.calories }
                val totalPro = meals.sumOf { it.protein }
                val totalCarbs = meals.sumOf { it.carbs }
                val totalFat = meals.sumOf { it.fat }
                val avgScore = if (meals.isNotEmpty()) meals.sumOf { it.score } / meals.size else 0
                val topMeal = meals.maxByOrNull { it.score }
                val reportText = "NutriLens AI Weekly Report | Name: " + profile?.name + " | Goal: " + profile?.goal + " | Meals: " + meals.size + " | Cal: " + totalCal + " kcal | Protein: " + totalPro + "g | Carbs: " + totalCarbs + "g | Fats: " + totalFat + "g | Avg Score: " + avgScore + "/100 | Best: " + topMeal?.name + " (" + topMeal?.score + "/100) | Tracked with NutriLens AI"
                val sendIntent = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, reportText); putExtra(Intent.EXTRA_SUBJECT, "My NutriLens AI Report"); type = "text/plain" }
                context.startActivity(Intent.createChooser(sendIntent, "Share Report"))
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "Export", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Weekly Report", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
