package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.viewmodel.NutriViewModel

data class NutriNotification(
    val emoji: String,
    val title: String,
    val message: String,
    val time: String,
    val color: Color
)

@Composable
fun NotificationCenterScreen(
    viewModel: NutriViewModel,
    onNavigateBack: () -> Unit
) {
    val meals by viewModel.loggedMeals.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    val totalCal = meals.sumOf { it.calories }
    val targetCal = profile?.caloriesBaseline ?: 1840
    val totalPro = meals.sumOf { it.protein }
    val targetPro = profile?.proteinTarget ?: 92

    val notifications = buildList {
        if (meals.isEmpty()) {
            add(NutriNotification("🍽️", "Start Logging!", "You have not logged any meals today. Tap the scan button to get started!", "Just now", Color(0xFF1565C0)))
        }
        if (totalCal > targetCal) {
            add(NutriNotification("⚠️", "Calorie Goal Exceeded", "You have consumed ${totalCal - targetCal} kcal over your daily target of $targetCal kcal.", "Today", Color(0xFFE53935)))
        } else if (totalCal > (targetCal * 0.8).toInt()) {
            add(NutriNotification("🔔", "80% of Calorie Goal Reached", "You have consumed $totalCal kcal — ${targetCal - totalCal} kcal remaining.", "Today", Color(0xFFE65100)))
        }
        if (totalPro < targetPro / 2 && meals.isNotEmpty()) {
            add(NutriNotification("💪", "Protein Intake Low", "Only ${totalPro}g protein so far. Try paneer, eggs or moong dal to boost intake.", "Today", Color(0xFF7B1FA2)))
        }
        if (meals.size >= 3) {
            add(NutriNotification("🎉", "Great Logging Streak!", "You have logged ${meals.size} meals today. Keep it up!", "Today", ForestGreen))
        }
        add(NutriNotification("💧", "Water Reminder", "Stay hydrated! Aim for 8 glasses of water throughout the day.", "Today", Color(0xFF1565C0)))
        add(NutriNotification("🌙", "Evening Check-in", "Don't forget to log your dinner to complete today's nutrition tracking.", "6:00 PM", Color(0xFF1A237E)))
        add(NutriNotification("📊", "Weekly Report Ready", "Your weekly nutrition summary is ready. Check Insights for your progress!", "Yesterday", ForestGreen))
        add(NutriNotification("🎯", "Goal Reminder", "Your goal is ${profile?.goal ?: "Manage Weight"}. Every meal logged brings you closer!", "Yesterday", Color(0xFFE65100)))
        add(NutriNotification("🤖", "NutriBot Tip", "Did you know? Eating protein with every meal helps maintain muscle while losing fat!", "2 days ago", ForestGreen))
    }

    Scaffold(
        topBar = { NutriTopAppBar(title = "Notifications", showBack = true, onBackClick = onNavigateBack) },
        containerColor = OrganicBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${notifications.size} Notifications", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(ForestGreen.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("All", style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen, fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(notifications) { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoBorderGrey)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(notif.color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(notif.emoji, fontSize = 22.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(notif.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface), modifier = Modifier.weight(1f))
                                Text(notif.time, style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 10.sp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(notif.message, style = MaterialTheme.typography.bodySmall.copy(color = BentoTextGrey))
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
