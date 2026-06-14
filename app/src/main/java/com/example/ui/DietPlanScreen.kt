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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.NutriViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DietPlanScreen(
    viewModel: NutriViewModel,
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val dietPlan = viewModel.dietPlan
    val isLoading = viewModel.isGeneratingDietPlan
    val error = viewModel.dietPlanError

    // Rotation animation for loading
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "rotation"
    )

    Scaffold(
        topBar = { NutriTopAppBar(title = "AI Diet Plan", showBack = true, onBackClick = onNavigateBack) },
        containerColor = OrganicBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ForestGreen)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🥗", fontSize = 36.sp)
                        Column {
                            Text("Weekly Meal Plan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            Text("Personalized for ${profile?.goal ?: "your goal"}", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${profile?.caloriesBaseline ?: 1840}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            Text("kcal/day", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${profile?.proteinTarget ?: 92}g", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            Text("protein/day", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("7", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            Text("days", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f)))
                        }
                    }
                }
            }

            // Generate button
            Button(
                onClick = { viewModel.generateDietPlan() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (dietPlan != null) Color.White else ForestGreen),
                border = if (dietPlan != null) BorderStroke(1.dp, BentoBorderGrey) else null,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.rotate(rotation).size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating your plan...", style = MaterialTheme.typography.labelMedium.copy(color = Color.White))
                } else {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (dietPlan != null) ForestGreen else Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (dietPlan != null) "Regenerate Plan" else "Generate My Diet Plan ✨",
                        style = MaterialTheme.typography.labelMedium.copy(color = if (dietPlan != null) ForestGreen else Color.White, fontWeight = FontWeight.Bold))
                }
            }

            // Error state
            error?.let {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("⚠️", fontSize = 20.sp)
                        Text(it, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE53935)))
                    }
                }
            }

            // Loading state
            if (isLoading) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BentoBorderGrey)) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🤖", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("NutriBot is crafting your personalized 7-day Indian meal plan...", style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey, textAlign = TextAlign.Center), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("This may take 10-15 seconds", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                    }
                }
            }

            // Diet plan content
            dietPlan?.let { plan ->
                // Parse and display each day
                val days = plan.split("DAY ").filter { it.isNotBlank() }
                days.forEach { dayContent ->
                    val lines = dayContent.trim().split("\n").filter { it.isNotBlank() }
                    if (lines.isEmpty()) return@forEach
                    val dayTitle = "DAY " + lines[0].trim()
                    val meals = lines.drop(1)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BentoBorderGrey)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Day header
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(ForestGreen.copy(alpha = 0.1f)).padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📅", fontSize = 18.sp)
                                Text(dayTitle, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Meals
                            meals.forEach { mealLine ->
                                val mealEmoji = when {
                                    mealLine.startsWith("BREAKFAST") -> "🌅"
                                    mealLine.startsWith("LUNCH") -> "☀️"
                                    mealLine.startsWith("SNACK") -> "🍎"
                                    mealLine.startsWith("DINNER") -> "🌙"
                                    mealLine.startsWith("TOTAL") -> "📊"
                                    else -> "🍽️"
                                }
                                val isTotal = mealLine.startsWith("TOTAL")
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(mealEmoji, fontSize = 16.sp)
                                    Text(
                                        text = mealLine,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isTotal) ForestGreen else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (!isTotal) HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }

                // Share button
                val context = androidx.compose.ui.platform.LocalContext.current
                Button(
                    onClick = {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, "My NutriLens AI Weekly Diet Plan:\n\n$plan")
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "My Weekly Diet Plan")
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Diet Plan"))
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Diet Plan", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }
            }

            // Empty state
            if (dietPlan == null && !isLoading && error == null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BentoBorderGrey)) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🥗", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Generate Your Personalized Diet Plan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Our AI will create a complete 7-day Indian meal plan tailored to your ${profile?.goal ?: "goal"} with ${profile?.caloriesBaseline ?: 1840} kcal daily target.", style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey, textAlign = TextAlign.Center), textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
