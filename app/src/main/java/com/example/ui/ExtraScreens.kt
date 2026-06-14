package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.api.AlternativeFood
import com.example.ui.theme.ForestGreen
import com.example.viewmodel.NutriViewModel

/**
 * simulated Barcode scanning screen matching slide specifications
 */
@Composable
fun BarcodeScanScreen(
    viewModel: NutriViewModel,
    onNavigateToResult: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "LaserSwipe")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserY"
    )

    Scaffold(
        topBar = {
            NutriTopAppBar(
                title = "Grocery Barcode Look",
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("barcode_scan_page")
        ) {
            // Simulated camera scanner viewport
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF141916))
            ) {
                if (hasCameraPermission) {
                    CameraPreview(previewView = previewView, modifier = Modifier.fillMaxSize())
                }
                if (!hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                            border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color(0xFFE53935).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Camera Permission Required",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                Text(
                                    text = "Camera Permission Required",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                
                                Text(
                                    text = "Grocery Barcode Scanner requires camera permission to capture upc code markings in real time.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.7f),
                                        lineHeight = 20.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("request_barcode_camera_permission_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ForestGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text("Grant Camera Access", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Centered barcode targeting box
                    Box(
                        modifier = Modifier
                            .size(width = 300.dp, height = 180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search, // Guaranteed search
                                contentDescription = "Simulated Laser target",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "CENTER BARCODE",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.4f))
                            )
                        }

                        // Red laser line animated
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = (-90 + (185 * laserY)).toInt().dp)
                                .background(Color.Red)
                        )
                    }

                    // Dynamic detected barcode tag tooltip
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 120.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "8902801402284 (Product detected)",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                        )
                    }
                }
            }

            // Controls & CTAs overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Scan packaged crackers bar to spot NutriScores", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                }

                Button(
                    onClick = {
                        if (!hasCameraPermission) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            // Triggers Oats scan directly to show cracker comparisons
                            viewModel.performFoodScan("NutriChoice Oats", null) {
                                onNavigateToResult()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_barcode_simulate"),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Scan icon", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Grocery Cracker Search", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Substitutes list overview
 */
@Composable
fun ExtraAlternativesScreen(
    onNavigateBack: () -> Unit
) {
    val itemsList = listOf(
        Pair("Dal Tadka & 2 Roti", listOf(
            AlternativeFood("Palak Dal (Spinach Lentils)", 92, "+14 points", "Slices fat from tempering while adding iron rich spinach fibre.", 14, 26, 9),
            AlternativeFood("Sprouted Moong Salad", 85, "+7 points", "A crisp, fiber powerhouse with 0 gluten carbs.", 12, 18, 7)
        )),
        Pair("NutriChoice Cracker Oats", listOf(
            AlternativeFood("True Elements Rolled Oats", 94, "+12 points", "100% natural, whole oat hulls without sodium preservatives.", 14, 62, 11),
            AlternativeFood("Yoga Bar Seed Muesli", 88, "+6 points", "Super seed toppings adding zinc and good polyunsaturated lipids.", 12, 54, 8)
        )),
        Pair("Masala Sweet Chai", listOf(
            AlternativeFood("Turmeric Ginger Herbal Infusion", 90, "+25 points", "Sugar-free warming tea that mitigates inflammatory indicators.", 1, 4, 1)
        ))
    )

    Scaffold(
        topBar = {
            NutriTopAppBar(
                title = "Smart Plate Alternatives",
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = Color(0xFFFAF7F2)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("alternatives_browser_page")
        ) {
            Text(
                text = "Premium Substitutions Browser",
                style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Swap these South Asian items to maintain blood sugar and calorie budgets.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6F7A72))
            )

            Spacer(modifier = Modifier.height(20.dp))

            itemsList.forEach { (category, list) ->
                Text(
                    text = "SWAPPING: $category".uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    list.forEach { alt ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEBEFE9), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = alt.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFEBF9F0))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "${alt.score} Score", style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen, fontSize = 9.sp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = alt.pointsText, style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 12.sp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = alt.description, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = Color(0xFF615E58)))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "P: ${alt.protein}g · C: ${alt.carbs}g · F: ${alt.fiber}g (Fib)",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Custom Permission screen asking "Stay on track, gently."
 */
@Composable
fun ExtraPermissionScreen(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141916).copy(alpha = 0.95f))
            .testTag("permission_modal_page"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEBF9F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Gentle tracker",
                        tint = ForestGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Stay on track, gently.",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enable gentle push notification nudges. We will remind you to log key meals without annoying pings.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF615E58)),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Not Now", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text("Allow Nudges", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                    }
                }
            }
        }
    }
}
