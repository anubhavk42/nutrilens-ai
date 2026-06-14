package com.example.ui

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.viewmodel.NutriViewModel

@Composable
fun ProfileScreen(
    viewModel: NutriViewModel,
    onNavigateToSetup: () -> Unit,
    onNavigateToWeightTracker: () -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("nutrilens_prefs", android.content.Context.MODE_PRIVATE)
    var toggleNotif by remember { mutableStateOf(prefs.getBoolean("meal_reminders_enabled", false)) }
    var toggleHaptic by remember { mutableStateOf(false) }
    var toggleAppLock by remember { mutableStateOf(prefs.getBoolean("app_lock_enabled", false)) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // PROFILE CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorderGrey)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape).background(BentoSecondaryContainer).border(3.dp, ForestGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (profile?.name?.firstOrNull() ?: "A").toString().uppercase(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(profile?.name ?: "Anubhav", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                Text("Active Health Profile Since June 2026", style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey, fontSize = 12.sp))
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(ForestGreen.copy(alpha = 0.1f)).padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text(profile?.goal ?: "Manage Weight", style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen, fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf(Triple("${profile?.weight ?: 75} kg", "WEIGHT", "⚖️"),
                           Triple("${profile?.height ?: 175} cm", "HEIGHT", "📏"),
                           Triple("${profile?.age ?: 29} yrs", "AGE", "🎂")).forEach { (value, label, emoji) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(value, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold))
                            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 10.sp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // INVITE & REFER CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ForestGreen),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🎁 Invite a Friend", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    Text("Share NutriLens AI with friends and family!", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)))
                }
                Button(
                    onClick = {
                        val shareText = "Hey! I am using NutriLens AI to track my nutrition with AI. It scans food and gives instant nutrition analysis. Try it out! #NutriLensAI"
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, "Invite via"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Invite", style = MaterialTheme.typography.labelMedium.copy(color = ForestGreen, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // HEALTH SETTINGS
        SectionTitle("⚙️ Health Settings")
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BentoBorderGrey)) {
            Column {
                SettingsToggleRow(title = "Meal Reminders", subtitle = "Daily alerts at 8am, 1pm, 4pm & 7pm", checked = toggleNotif, onCheck = {
                    toggleNotif = it
                    prefs.edit().putBoolean("meal_reminders_enabled", it).apply()
                    if (it) com.example.ReminderScheduler.scheduleAllReminders(context)
                    else com.example.ReminderScheduler.cancelAllReminders(context)
                })
                HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                SettingsToggleRow(title = "App Lock (PIN)", subtitle = "Secure your health data on launch", checked = toggleAppLock, onCheck = {
                    toggleAppLock = it
                    prefs.edit().putBoolean("app_lock_enabled", it).apply()
                })
                HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                SettingsToggleRow(title = "Dietary Target Alerts", subtitle = "Notify when macro budgets exceeded", checked = toggleHaptic, onCheck = { toggleHaptic = it })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // MY DATA
        SectionTitle("📊 My Data")
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BentoBorderGrey)) {
            Column {
                SettingsNavRow(emoji = "⚖️", title = "Weight Tracker", subtitle = "Log and track your weight progress", onClick = onNavigateToWeightTracker)
                HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                SettingsNavRow(emoji = "🎯", title = "Reprogram Goals", subtitle = "Update your dietary targets", onClick = onNavigateToSetup)
                HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                SettingsNavRow(emoji = "🗑️", title = "Clear All Meals", subtitle = "Reset today's meal log", color = Color(0xFFE53935), onClick = { viewModel.clearAllMeals() })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SUPPORT
        SectionTitle("💬 Support")
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BentoBorderGrey)) {
            Column {
                SettingsNavRow(emoji = "❓", title = "FAQ", subtitle = "Frequently asked questions", onClick = { showFaqDialog = true })
                HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                SettingsNavRow(emoji = "📧", title = "Contact Us", subtitle = "Get help from our team", onClick = { showContactDialog = true })
                HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                SettingsNavRow(emoji = "🎬", title = "App Demo", subtitle = "Watch how NutriLens AI works", onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com"))
                    context.startActivity(intent)
                })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LEGAL
        SectionTitle("📋 Legal")
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, BentoBorderGrey)) {
            Column {
                SettingsNavRow(emoji = "📄", title = "Terms & Conditions", subtitle = "Our terms of service", onClick = { showTermsDialog = true })
                HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                SettingsNavRow(emoji = "🔒", title = "Privacy Policy", subtitle = "How we handle your data", onClick = { showPrivacyDialog = true })
                HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                SettingsNavRow(emoji = "⭐", title = "Rate Us", subtitle = "Rate NutriLens AI on Play Store", onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=com.example"))
                    try { context.startActivity(intent) } catch (e: Exception) {}
                })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // APP INFO
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BentoSecondaryContainer), border = BorderStroke(1.dp, BentoBorderGrey)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NutriLens AI", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                Text("Version 1.0.0", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                Text("Made with ❤️ for healthier India", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = { viewModel.logOut() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color.Red),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = Color.Red)
        ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log Out", tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Secure Log Out", style = MaterialTheme.typography.labelSmall.copy(color = Color.Red, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // FAQ Dialog
    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = { Text("❓ FAQ", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
                        Pair("How does food scanning work?", "Point your camera at any food item. Our AI powered by Google Gemini analyzes the image and provides instant nutrition information."),
                        Pair("Is my data stored online?", "All your meal logs are stored locally on your device. No data is sent to external servers except for AI food analysis."),
                        Pair("How accurate is the nutrition data?", "Our AI provides estimates based on visual analysis. For packaged foods, accuracy is higher. We recommend verifying critical nutrition data."),
                        Pair("Can I use the app offline?", "Basic features work offline, but food scanning requires an internet connection to reach our AI servers."),
                        Pair("How do I reset my goals?", "Go to Profile → Reprogram Goals to update your dietary targets anytime.")
                    ).forEach { (q, a) ->
                        Column {
                            Text(q, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(a, style = MaterialTheme.typography.bodySmall.copy(color = BentoTextGrey))
                        }
                        HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showFaqDialog = false }) { Text("Close", color = ForestGreen) } }
        )
    }

    // Terms Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = { Text("📄 Terms & Conditions", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Last updated: June 2026", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf(
                        Pair("1. Acceptance", "By using NutriLens AI, you agree to these terms. If you disagree, please do not use the app."),
                        Pair("2. Medical Disclaimer", "NutriLens AI is not a medical device. The nutrition information provided is for informational purposes only and should not replace professional medical advice."),
                        Pair("3. Data Accuracy", "While we strive for accuracy, nutrition data from AI analysis may vary. Always consult a nutritionist for medical dietary needs."),
                        Pair("4. User Responsibility", "Users are responsible for the accuracy of manually entered data and for making their own health decisions."),
                        Pair("5. Intellectual Property", "All content, features, and functionality of NutriLens AI are owned by NutriLens and protected by intellectual property laws."),
                        Pair("6. Limitation of Liability", "NutriLens AI is not liable for any health decisions made based on information provided by the app."),
                        Pair("7. Changes", "We reserve the right to modify these terms at any time. Continued use constitutes acceptance of changes.")
                    ).forEach { (title, content) ->
                        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                        Text(content, style = MaterialTheme.typography.bodySmall.copy(color = BentoTextGrey))
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTermsDialog = false }) { Text("I Understand", color = ForestGreen) } }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = { Text("🔒 Privacy Policy", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Last updated: June 2026", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf(
                        Pair("Data We Collect", "We collect meal logs, nutrition goals, and app usage data stored locally on your device."),
                        Pair("How We Use Data", "Your data is used solely to provide nutrition tracking and AI analysis features within the app."),
                        Pair("Data Sharing", "We do not sell or share your personal data with third parties. Food images are sent to Google Gemini AI for analysis only."),
                        Pair("Data Storage", "All personal data is stored locally on your device. We do not maintain cloud servers with your health data."),
                        Pair("Camera Access", "Camera access is used solely for food scanning. Images are not stored or shared beyond the analysis request."),
                        Pair("Your Rights", "You can delete all your data at any time through the app settings or by uninstalling the app."),
                        Pair("Contact", "For privacy concerns, contact us at nutrilensai@gmail.com")
                    ).forEach { (title, content) ->
                        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                        Text(content, style = MaterialTheme.typography.bodySmall.copy(color = BentoTextGrey))
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text("Got it", color = ForestGreen) } }
        )
    }

    // Contact Dialog
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = { Text("📧 Contact Us", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("We would love to hear from you!", style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey))
                    listOf(
                        Pair("📧 Email", "nutrilensai@gmail.com"),
                        Pair("🐦 Twitter", "@NutriLensAI"),
                        Pair("📸 Instagram", "@nutrilens.ai"),
                        Pair("⏰ Support Hours", "Mon-Fri, 9am-6pm IST")
                    ).forEach { (label, value) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                            Text(value, style = MaterialTheme.typography.labelMedium.copy(color = ForestGreen))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:nutrilensai@gmail.com")
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "NutriLens AI Support")
                        }
                        try { context.startActivity(intent) } catch (e: Exception) {}
                        showContactDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) { Text("Send Email", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showContactDialog = false }) { Text("Close", color = ForestGreen) } }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
}

@Composable
private fun SettingsToggleRow(title: String, subtitle: String, checked: Boolean, onCheck: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface))
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = BentoTextGrey))
        }
        Switch(checked = checked, onCheckedChange = onCheck, colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = ForestGreen.copy(alpha = 0.3f)))
    }
}

@Composable
private fun SettingsNavRow(emoji: String, title: String, subtitle: String, color: Color = Color.Unspecified, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(emoji, fontSize = 20.sp)
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color))
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = BentoTextGrey))
            }
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = BentoTextGrey, modifier = Modifier.size(20.dp))
    }
}
