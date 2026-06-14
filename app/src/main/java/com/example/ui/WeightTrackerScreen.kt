package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WeightLog
import com.example.ui.theme.*
import com.example.viewmodel.NutriViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeightTrackerScreen(
    viewModel: NutriViewModel,
    onNavigateBack: () -> Unit
) {
    val weightLogs by viewModel.weightLogs.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    val latestWeight = weightLogs.lastOrNull()?.weightKg
    val startWeight = weightLogs.firstOrNull()?.weightKg
    val change = if (latestWeight != null && startWeight != null) latestWeight - startWeight else 0f

    // Graph animation
    val graphAnim = remember { Animatable(0f) }
    LaunchedEffect(weightLogs.size) {
        graphAnim.snapTo(0f)
        graphAnim.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
    }

    Scaffold(
        topBar = {
            NutriTopAppBar(title = "Weight Tracker", showBack = true, onBackClick = onNavigateBack)
        },
        containerColor = OrganicBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ForestGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log weight")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current weight card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBorderGrey)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Current Weight", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (latestWeight != null) "${latestWeight}kg" else "--",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 52.sp, fontWeight = FontWeight.Bold, color = ForestGreen
                        )
                    )
                    if (change != 0f) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val changeColor = if (change < 0) ForestGreen else Color(0xFFE53935)
                        val changeText = if (change < 0) "↓ ${String.format("%.1f", -change)}kg lost" else "↑ ${String.format("%.1f", change)}kg gained"
                        Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(changeColor.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Text(changeText, style = MaterialTheme.typography.labelMedium.copy(color = changeColor, fontWeight = FontWeight.Bold))
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf(
                            Triple(if (startWeight != null) "${startWeight}kg" else "--", "Starting", "🏁"),
                            Triple("${weightLogs.size}", "Entries", "📝"),
                            Triple("${profile?.weight ?: "--"}kg", "Profile", "👤")
                        ).forEachIndexed { i, (value, label, emoji) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(label, style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 10.sp))
                            }
                            if (i < 2) HorizontalDivider(modifier = Modifier.width(1.dp).height(40.dp), color = BentoBorderGrey)
                        }
                    }
                }
            }

            // Interactive graph
            if (weightLogs.size >= 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoBorderGrey)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val recentLogs = weightLogs.takeLast(10)
                        val weights = recentLogs.map { it.weightKg }
                        val minW = (weights.min() - 1f).coerceAtLeast(0f)
                        val maxW = weights.max() + 1f
                        val range = (maxW - minW).coerceAtLeast(1f)
                        val trend = weights.last() - weights.first()

                        // Header
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Progress Graph", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Tap any point to see details", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(if (trend <= 0f) ForestGreen.copy(alpha = 0.1f) else Color(0xFFFFEBEE))
                                .padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = if (trend <= 0f) "↓ ${String.format("%.1f", -trend)}kg" else "↑ ${String.format("%.1f", trend)}kg",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (trend <= 0f) ForestGreen else Color(0xFFE53935),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selected point info
                        selectedPointIndex?.let { idx ->
                            if (idx < recentLogs.size) {
                                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .background(ForestGreen.copy(alpha = 0.08f)).padding(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("⚖️ ${recentLogs[idx].weightKg}kg", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                                        Text(dateFormat.format(Date(recentLogs[idx].timestamp)), style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Graph
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Y axis
                            Column(modifier = Modifier.width(36.dp).height(180.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                listOf(maxW, (maxW + minW) / 2f, minW).forEach { v ->
                                    Text("${v.toInt()}", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 9.sp), textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))

                            androidx.compose.foundation.Canvas(
                                modifier = Modifier.weight(1f).height(180.dp).pointerInput(recentLogs) {
                                    detectTapGestures { offset ->
                                        val step = size.width.toFloat() / (weights.size - 1).coerceAtLeast(1)
                                        val idx = (offset.x / step).toInt().coerceIn(0, weights.size - 1)
                                        selectedPointIndex = if (selectedPointIndex == idx) null else idx
                                    }
                                }
                            ) {
                                val w = size.width
                                val h = size.height
                                val step = if (weights.size > 1) w / (weights.size - 1) else w
                                val animProgress = graphAnim.value
                                val drawCount = (weights.size * animProgress).toInt().coerceAtLeast(1)

                                // Grid lines
                                for (i in 0..4) {
                                    drawLine(Color(0xFFEBEFE9), Offset(0f, h * i / 4f), Offset(w, h * i / 4f), 1.dp.toPx())
                                }

                                // Gradient fill
                                val fillPath = Path()
                                fillPath.moveTo(0f, h)
                                weights.take(drawCount).forEachIndexed { i, wt ->
                                    val x = i * step
                                    val y = h - ((wt - minW) / range * h * 0.85f + h * 0.05f)
                                    if (i == 0) fillPath.lineTo(x, y) else fillPath.lineTo(x, y)
                                }
                                fillPath.lineTo((drawCount - 1) * step, h)
                                fillPath.close()
                                drawPath(fillPath, Brush.verticalGradient(
                                    listOf(Color(0xFF1A6B47).copy(alpha = 0.25f), Color(0xFF1A6B47).copy(alpha = 0f)),
                                    startY = 0f, endY = h
                                ))

                                // Line
                                val linePath = Path()
                                weights.take(drawCount).forEachIndexed { i, wt ->
                                    val x = i * step
                                    val y = h - ((wt - minW) / range * h * 0.85f + h * 0.05f)
                                    if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                                }
                                drawPath(linePath, Color(0xFF1A6B47), style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                                // Dots
                                weights.take(drawCount).forEachIndexed { i, wt ->
                                    val x = i * step
                                    val y = h - ((wt - minW) / range * h * 0.85f + h * 0.05f)
                                    val isSelected = selectedPointIndex == i
                                    drawCircle(Color.White, if (isSelected) 9.dp.toPx() else 5.dp.toPx(), Offset(x, y))
                                    drawCircle(Color(0xFF1A6B47), if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(), Offset(x, y))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 40.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (recentLogs.isNotEmpty()) {
                                Text(dateFormat.format(Date(recentLogs.first().timestamp)), style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 9.sp))
                                if (recentLogs.size > 2) Text(dateFormat.format(Date(recentLogs[recentLogs.size / 2].timestamp)), style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 9.sp))
                                Text(dateFormat.format(Date(recentLogs.last().timestamp)), style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 9.sp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BentoBorderGrey.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${weights.min()}kg", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                                Text("Lowest", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 10.sp))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${weights.max()}kg", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE53935)))
                                Text("Highest", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 10.sp))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${String.format("%.1f", weights.average())}kg", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Average", style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 10.sp))
                            }
                        }
                    }
                }
            }

            // Log history
            Text("LOG HISTORY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BentoTextGrey))

            if (weightLogs.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚖️", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No weight logged yet. Tap + to log your first entry!", style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey, textAlign = TextAlign.Center), textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    weightLogs.reversed().forEachIndexed { index, log ->
                        val prevLog = if (index < weightLogs.size - 1) weightLogs.reversed()[index + 1] else null
                        val diff = if (prevLog != null) log.weightKg - prevLog.weightKg else 0f
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BentoBorderGrey)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(ForestGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Text("⚖️", fontSize = 22.sp)
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("${log.weightKg}kg", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen))
                                            if (prevLog != null) {
                                                Text(
                                                    text = if (diff < 0) "↓${String.format("%.1f", -diff)}" else if (diff > 0) "↑${String.format("%.1f", diff)}" else "→",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (diff < 0) ForestGreen else if (diff > 0) Color(0xFFE53935) else BentoTextGrey,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                        Text(dateFormat.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey))
                                        if (log.note.isNotBlank()) Text(log.note, style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 11.sp))
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteWeightLog(log) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Log Today's Weight", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weightInput, onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") }, placeholder = { Text("e.g. 72.5") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ForestGreen),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true
                    )
                    OutlinedTextField(
                        value = noteInput, onValueChange = { noteInput = it },
                        label = { Text("Note (optional)") }, placeholder = { Text("e.g. After morning walk") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ForestGreen), singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = weightInput.toFloatOrNull()
                        if (w != null && w > 0) {
                            viewModel.logWeight(w, noteInput)
                            weightInput = ""; noteInput = ""; showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    enabled = weightInput.toFloatOrNull() != null
                ) { Text("Save", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = ForestGreen) } }
        )
    }
}
