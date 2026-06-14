package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreen

@Composable
fun AppLockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val prefs = context.getSharedPreferences("nutrilens_prefs", android.content.Context.MODE_PRIVATE)
    val correctPin = prefs.getString("app_pin", "1234") ?: "1234"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1A14)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ForestGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "N",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Text(
                text = "NutriLens AI",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = "Enter your PIN to continue",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF9BB8A8)),
                textAlign = TextAlign.Center
            )

            // PIN dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until 4) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < pin.length) ForestGreen
                                else Color(0xFF2E3D35)
                            )
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Red)
                )
            }

            // Number pad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫")
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                keys.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (key.isEmpty()) Color.Transparent
                                        else Color(0xFF1A2620)
                                    )
                                    .border(
                                        1.dp,
                                        if (key.isEmpty()) Color.Transparent
                                        else Color(0xFF2E3D35),
                                        CircleShape
                                    )
                                    .clickable(enabled = key.isNotEmpty()) {
                                        when (key) {
                                            "⌫" -> {
                                                if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                errorMessage = ""
                                            }
                                            else -> {
                                                if (pin.length < 4) {
                                                    pin += key
                                                    if (pin.length == 4) {
                                                        if (pin == correctPin) {
                                                            onUnlocked()
                                                        } else {
                                                            errorMessage = "Incorrect PIN. Try again."
                                                            pin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key.isNotEmpty()) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "Default PIN: 1234",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF4A6B5A),
                    fontSize = 11.sp
                )
            )
        }
    }
}
