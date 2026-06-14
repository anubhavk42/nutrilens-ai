package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        Triple("🍱", "Scan Any Food Instantly", "Point your camera at any meal and get instant nutrition analysis powered by Gemini AI."),
        Triple("📊", "Track Your Daily Nutrition", "Monitor calories, protein, carbs and fats against your personal targets."),
        Triple("🤖", "Your Personal NutriBot", "Ask NutriBot anything about South Asian nutrition and get personalized advice.")
    )
    var currentPage by remember { mutableStateOf(0) }
    val page = pages[currentPage]

    Box(
        modifier = Modifier.fillMaxSize().background(OrganicBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onFinished) {
                    Text("Skip", style = MaterialTheme.typography.labelMedium.copy(color = BentoTextGrey))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(160.dp).clip(CircleShape).background(ForestGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(page.first, fontSize = 72.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(page.second, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1C221E), textAlign = TextAlign.Center), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text(page.third, style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF6F7A72), textAlign = TextAlign.Center), textAlign = TextAlign.Center)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pages.forEachIndexed { i, _ ->
                        Box(modifier = Modifier.size(if (i == currentPage) 24.dp else 8.dp, 8.dp).clip(CircleShape)
                            .background(if (i == currentPage) ForestGreen else ForestGreen.copy(alpha = 0.3f)))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { if (currentPage < pages.size - 1) currentPage++ else onFinished() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text(
                        text = if (currentPage < pages.size - 1) "Next →" else "Get Started 🚀",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
