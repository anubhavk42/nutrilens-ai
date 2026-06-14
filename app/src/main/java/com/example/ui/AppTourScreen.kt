package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class TourStep(
    val emoji: String,
    val screen: String,
    val title: String,
    val description: String,
    val tip: String
)

@Composable
fun AppTourScreen(onFinished: () -> Unit) {
    val steps = listOf(
        TourStep("🏠", "Home", "Your Nutrition Dashboard", "See your daily calorie ring, macros, water intake and step counter all in one place.", "💡 Scroll down to see Quick Add and Share card!"),
        TourStep("📷", "Scan", "AI Food Scanner", "Tap the green scan button to capture any food. A 3-2-1 countdown gives you time to steady your hand.", "💡 Choose meal type after scanning — Breakfast, Lunch, Snack or Dinner!"),
        TourStep("📔", "Journal", "Meal Log Ledger", "See all your logged meals organized by meal type. Tap any meal to see details and share.", "💡 Use the calendar to browse different days!"),
        TourStep("⭐", "Insights", "Nutrition Analytics", "See your weekly quality score, macro split donut chart, and meal frequency breakdown.", "💡 Export your weekly report and share with your nutritionist!"),
        TourStep("🤖", "NutriBot", "AI Nutrition Guide", "Ask NutriBot anything about South Asian food, macros, or get personalized meal suggestions.", "💡 Tap the quick suggestion cards to get started fast!"),
        TourStep("👤", "Profile", "Your Health Profile", "Manage goals, track weight, set reminders, and access FAQ, Terms and Privacy settings.", "💡 Tap Weight Tracker to log and visualize your weight journey!")
    )

    var currentStep by remember { mutableStateOf(0) }
    val step = steps[currentStep]

    val scaleAnim = remember { Animatable(0.8f) }
    LaunchedEffect(currentStep) {
        scaleAnim.snapTo(0.8f)
        scaleAnim.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Box(
        modifier = Modifier.fillMaxSize().background(OrganicBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Skip button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onFinished) {
                    Text("Skip Tour", style = MaterialTheme.typography.labelMedium.copy(color = BentoTextGrey))
                }
            }

            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(scaleAnim.value)
            ) {
                // Step indicator
                Text(
                    text = "${currentStep + 1} of ${steps.size}",
                    style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Big emoji
                Box(
                    modifier = Modifier.size(140.dp).clip(CircleShape)
                        .background(ForestGreen.copy(alpha = 0.1f))
                        .border(2.dp, ForestGreen.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(step.emoji, fontSize = 64.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = step.screen,
                    style = MaterialTheme.typography.labelMedium.copy(color = ForestGreen, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1C221E), textAlign = TextAlign.Center)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF6F7A72), textAlign = TextAlign.Center, lineHeight = 26.sp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tip card
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(ForestGreen.copy(alpha = 0.08f))
                        .border(1.dp, ForestGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(step.tip, style = MaterialTheme.typography.bodyMedium.copy(color = ForestGreen, textAlign = TextAlign.Center), textAlign = TextAlign.Center)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Dot indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (i == currentStep) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (i == currentStep) ForestGreen else ForestGreen.copy(alpha = 0.3f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Next button
                Button(
                    onClick = {
                        if (currentStep < steps.size - 1) currentStep++
                        else onFinished()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text(
                        text = if (currentStep < steps.size - 1) "Next →" else "Start Using NutriLens AI 🚀",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
