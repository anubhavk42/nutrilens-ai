package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import com.example.api.AlternativeFood
import com.example.ui.theme.ForestGreen
import com.example.viewmodel.NutriViewModel

var globalImageCapture: ImageCapture? = null

@Composable
fun CameraPreview(
    previewView: PreviewView,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = {
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
            previewView
        },
        modifier = modifier
    )
}

fun capturePhoto(onBitmapReady: (android.graphics.Bitmap?) -> Unit) {
    val imageCapture = globalImageCapture
    if (imageCapture == null) { onBitmapReady(null); return }
    val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val matrix = android.graphics.Matrix()
            matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
            val rotated = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            image.close()
            onBitmapReady(rotated)
        }
        override fun onError(exception: ImageCaptureException) { onBitmapReady(null) }
    })
}

enum class FoodScanMode {
    COOKED_FOOD,
    PACKAGED_FOOD
}

@Composable
fun CameraScanScreen(
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

    var manualInput by remember { mutableStateOf("") }
    var showManualDialog by remember { mutableStateOf(false) }
    
    // Cooked vs Packaged food scanner controls
    var scanMode by remember { mutableStateOf(FoodScanMode.COOKED_FOOD) }
    var simulateScanFailure by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    var showUnrecognizedDialog by remember { mutableStateOf(false) }
    var unrecognizedFoodName by remember { mutableStateOf("") }
    var unrecognizedPortionSize by remember { mutableStateOf("1 Standard Serving") }

    val isScanning = viewModel.isScanning
    val error = viewModel.scanError

    val previewView = remember { 
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        } 
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Laser")
    val laserYOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserSwipe"
    )

    Scaffold(
        topBar = {
            NutriTopAppBar(
                title = "Plate Smart Scanner",
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
                .testTag("camera_scan_screen")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C221E))
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
                                    text = "Plate Smart Scanner requires camera permission to detect barcode structures and analyze cooked food items in real time.",
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
                                        .testTag("request_camera_permission_button"),
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
                    // WIDENED SEARCH AREA VIEW FINDER OVERLAY depending on mode
                    if (scanMode == FoodScanMode.COOKED_FOOD) {
                        Box(
                            modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .fillMaxHeight(0.55f)
                                        .align(Alignment.Center)
                                        .background(Color.White.copy(alpha = 0.03f))
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                        .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Top left corner
                                    Box(modifier = Modifier.size(24.dp).drawBehind {
                                        drawLine(Color(0xFF9BE9BB), Offset(0f, 0f), Offset(this.size.width, 0f), strokeWidth = 3.dp.toPx())
                                        drawLine(Color(0xFF9BE9BB), Offset(0f, 0f), Offset(0f, this.size.height), strokeWidth = 3.dp.toPx())
                                    })
                                    // Top right corner
                                    Box(modifier = Modifier.size(24.dp).drawBehind {
                                        drawLine(Color(0xFF9BE9BB), Offset(0f, 0f), Offset(this.size.width, 0f), strokeWidth = 3.dp.toPx())
                                        drawLine(Color(0xFF9BE9BB), Offset(this.size.width, 0f), Offset(this.size.width, this.size.height), strokeWidth = 3.dp.toPx())
                                    })
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Point camera at food",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF9BE9BB),
                                            letterSpacing = 0.15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Bottom left corner
                                    Box(modifier = Modifier.size(24.dp).drawBehind {
                                        drawLine(Color(0xFF9BE9BB), Offset(0f, this.size.height), Offset(this.size.width, this.size.height), strokeWidth = 3.dp.toPx())
                                        drawLine(Color(0xFF9BE9BB), Offset(0f, 0f), Offset(0f, this.size.height), strokeWidth = 3.dp.toPx())
                                    })
                                    // Bottom right corner
                                    Box(modifier = Modifier.size(24.dp).drawBehind {
                                        drawLine(Color(0xFF9BE9BB), Offset(0f, this.size.height), Offset(this.size.width, this.size.height), strokeWidth = 3.dp.toPx())
                                        drawLine(Color(0xFF9BE9BB), Offset(this.size.width, 0f), Offset(this.size.width, this.size.height), strokeWidth = 3.dp.toPx())
                                    })
                                }
                            }
                        }
                    } else {
                        // Packaged food horizontal barcode / nutrition list scanner box layout
                        Box(
                            modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(200.dp)
                                        .align(Alignment.Center)
                                        .background(Color.White.copy(alpha = 0.03f))
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Top left corner (Turmeric Orange theme for barcode scanner)
                                    Box(modifier = Modifier.size(20.dp).drawBehind {
                                        drawLine(Color(0xFFFFB300), Offset(0f, 0f), Offset(this.size.width, 0f), strokeWidth = 3.dp.toPx())
                                        drawLine(Color(0xFFFFB300), Offset(0f, 0f), Offset(0f, this.size.height), strokeWidth = 3.dp.toPx())
                                    })
                                    // Top right corner
                                    Box(modifier = Modifier.size(20.dp).drawBehind {
                                        drawLine(Color(0xFFFFB300), Offset(0f, 0f), Offset(this.size.width, 0f), strokeWidth = 3.dp.toPx())
                                        drawLine(Color(0xFFFFB300), Offset(this.size.width, 0f), Offset(this.size.width, this.size.height), strokeWidth = 3.dp.toPx())
                                    })
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Packaged Target",
                                        tint = Color(0xFFFFB300).copy(alpha = 0.5f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ALIGN BARCODE / NUTRITION LABELS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFFB300),
                                            letterSpacing = 0.15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Bottom left corner
                                    Box(modifier = Modifier.size(20.dp).drawBehind {
                                        drawLine(Color(0xFFFFB300), Offset(0f, this.size.height), Offset(this.size.width, this.size.height), strokeWidth = 3.dp.toPx())
                                        drawLine(Color(0xFFFFB300), Offset(0f, 0f), Offset(0f, this.size.height), strokeWidth = 3.dp.toPx())
                                    })
                                    // Bottom right corner
                                    Box(modifier = Modifier.size(20.dp).drawBehind {
                                        drawLine(Color(0xFFFFB300), Offset(0f, this.size.height), Offset(this.size.width, this.size.height), strokeWidth = 3.dp.toPx())
                                        drawLine(Color(0xFFFFB300), Offset(this.size.width, 0f), Offset(this.size.width, this.size.height), strokeWidth = 3.dp.toPx())
                                    })
                                }
                            }
                        }
                    }

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        val maxHeightPx = with(density) { maxHeight.toPx() }
                        Box(
                            modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .offset {
                                            androidx.compose.ui.unit.IntOffset(0, (maxHeightPx * laserYOffset).toInt())
                                        }
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, Color(0xFF1A6B47), Color(0xFF9BE9BB), Color(0xFF1A6B47), Color.Transparent)
                                            )
                                        )
                        )
                    }

                    // Countdown overlay
                    if (countdownValue > 0) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = countdownValue.toString(),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 64.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // AI Active Scanning lens badge
                    Box(
                        modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(y = (-130).dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .border(1.dp, if (scanMode == FoodScanMode.COOKED_FOOD) ForestGreen else Color(0xFFFFB300), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (scanMode == FoodScanMode.COOKED_FOOD) Color(0xFF9BE9BB) else Color(0xFFFFB300))
                            )
                            Text(
                                text = if (scanMode == FoodScanMode.COOKED_FOOD) "AI Lens: Cooked Portions Active" else "AI Lens: Shelf Packaged Food Active",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top elements: Segment switcher + mode caption
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Modern Segment Tab bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (scanMode == FoodScanMode.COOKED_FOOD) ForestGreen else Color.Transparent)
                                .clickable { scanMode = FoodScanMode.COOKED_FOOD }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cooked Food",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (scanMode == FoodScanMode.COOKED_FOOD) Color.White else Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (scanMode == FoodScanMode.PACKAGED_FOOD) ForestGreen else Color.Transparent)
                                .clickable { scanMode = FoodScanMode.PACKAGED_FOOD }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Packaged Food",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (scanMode == FoodScanMode.PACKAGED_FOOD) Color.White else Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (scanMode == FoodScanMode.COOKED_FOOD) "Detects plate layouts & multi-item meals" else "Detects ingredient ratios & barcode items",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {


                    if (isScanning) {
                        CircularProgressIndicator(color = ForestGreen)
                        Text(
                            text = "Analyzing nutrition values with Gemini...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        if (error != null) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Red),
                                textAlign = TextAlign.Center
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Quick Manual Type Input
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { showManualDialog = true }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Manual Type",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Manual Input",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            // Center: Beautiful Giant Camera Shutter Capture Button with scanning detection
                            Box(
                                modifier = Modifier
                                    .size(82.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .border(4.dp, Color.White, CircleShape)
                                    .clickable {
                                        if (!hasCameraPermission) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            permissionLauncher.launch(Manifest.permission.CAMERA)
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            // 3-2-1 countdown then capture sharp photo
                                            coroutineScope.launch {
                                                countdownValue = 3
                                                delay(700)
                                                countdownValue = 2
                                                delay(700)
                                                countdownValue = 1
                                                delay(700)
                                                countdownValue = 0
                                                capturePhoto { bitmap ->
                                                    if (bitmap == null) {
                                                        showManualDialog = true
                                                    } else {
                                                        viewModel.performFoodScan("", bitmap) {
                                                            onNavigateToResult()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("btn_capture_scan"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Capture and Analyze",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Right: Glass circular action to easily scan a sample plate
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .alpha(0.5f)
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Demo Sample Scan",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Disabled",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Beautiful Custom Portion Helper & Food Name dialog when image processing fails
            if (showUnrecognizedDialog) {
                AlertDialog(
                    onDismissRequest = { showUnrecognizedDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "AI Portion Tuning",
                                tint = ForestGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Scan Unclear: AI Portion Helper",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Our automatic photo scan was unsufficiently clear due to lighting/pack wrap. Provide the name + portion details below to get a 100% accurate health score!",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF615E58))
                            )

                            OutlinedTextField(
                                value = unrecognizedFoodName,
                                onValueChange = { unrecognizedFoodName = it },
                                label = { Text("Enter Food / Packaged Item Name") },
                                placeholder = { Text("e.g. Masala Dosa, Haldirams Bhujia, Oreo Biscuits") },
                                modifier = Modifier.fillMaxWidth().testTag("unrecognized_food_name_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreen,
                                    unfocusedBorderColor = Color(0xFFBFC9C0)
                                ),
                                singleLine = true
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Select Portion Presets:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF6F7A72),
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                val portionPresetsList = listOf(
                                    "1 Plate (Medium)",
                                    "1 Bowl (150g)",
                                    "2 Pieces / Rotis",
                                    "1 Glass (200ml)",
                                    "1 Small Packet (50g)",
                                    "Half Portion"
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(portionPresetsList) { preset ->
                                        val isSelected = unrecognizedPortionSize == preset
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(if (isSelected) ForestGreen else Color(0xFFEBEFE9))
                                                .clickable { unrecognizedPortionSize = preset }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = preset,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSelected) Color.White else Color(0xFF615E58),
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = unrecognizedPortionSize,
                                    onValueChange = { unrecognizedPortionSize = it },
                                    label = { Text("Tweak / Custom Portion Description") },
                                    placeholder = { Text("e.g. 1.5 cups, pack of 4 biscuits, 250 grams") },
                                    modifier = Modifier.fillMaxWidth().testTag("unrecognized_food_portion_field"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ForestGreen,
                                        unfocusedBorderColor = Color(0xFFBFC9C0)
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (unrecognizedFoodName.isNotBlank() && unrecognizedPortionSize.isNotBlank()) {
                                    showUnrecognizedDialog = false
                                    val finalQuery = "$unrecognizedFoodName ($unrecognizedPortionSize)"
                                    viewModel.performFoodScan(finalQuery, null) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onNavigateToResult()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            enabled = unrecognizedFoodName.isNotBlank()
                        ) {
                            Text("Fast AI Analyze", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUnrecognizedDialog = false }) {
                            Text("Cancel", style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen))
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            if (showManualDialog) {
                AlertDialog(
                    onDismissRequest = { showManualDialog = false },
                    title = {
                        Text(
                            text = "Type Meal Name",
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Enter any meal (e.g. 'Poha', 'Moong Sprout Salad') and we'll analyze it using the Gemini model.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF615E58))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = manualInput,
                                onValueChange = { manualInput = it },
                                placeholder = { Text("e.g. 2 Idlis & Sambar") },
                                modifier = Modifier.fillMaxWidth().testTag("manual_meal_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreen,
                                    unfocusedBorderColor = Color(0xFFBFC9C0)
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (manualInput.isNotBlank()) {
                                    showManualDialog = false
                                    viewModel.performFoodScan(manualInput, null) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onNavigateToResult()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                        ) {
                            Text("Scan Food", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showManualDialog = false }) {
                            Text("Cancel", style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen))
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

/**
 * Detailed Meal Analysis Results Screen
 */
@Composable
fun MealAnalysisScreen(
    viewModel: NutriViewModel,
    onNavigateBack: () -> Unit,
    onLogMealSuccess: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val result = viewModel.scannedMealResult
    val isLoggingState = remember { mutableStateOf(false) }
    var showMealTypePicker by remember { mutableStateOf(false) }

    if (result == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No scanned result. Please scan first!")
        }
        return
    }

    Scaffold(
        topBar = {
            NutriTopAppBar(
                title = "AI Nutrition Analysis",
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = com.example.ui.theme.OrganicBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("meal_analysis_screen")
        ) {
            Text(
                text = result.name,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("analysed_meal_title")
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ForestGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AI VERIFIED METRIC",
                        style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen, fontSize = 9.sp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aimed at: ${result.targetGoalMatched}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF615E58))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                        val scorePct = result.score.toFloat() / 100f
                        val ringColor = if (result.score >= 85) ForestGreen
                        else if (result.score >= 70) Color(0xFFE65100)
                        else Color(0xFF763036)

                        Canvas(modifier = Modifier.size(140.dp)) {
                            drawArc(
                                color = Color(0xFFEBEFE9),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12.dp.toPx())
                            )
                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = scorePct * 360f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = result.score.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ringColor
                                )
                            )
                            Text(
                                text = "HEALTH SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72), fontSize = 10.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        AnalysisMetric(value = "${result.calories} kcal", label = "CALORIES", icon = Icons.Default.Star)
                        AnalysisMetric(value = "${result.protein}g", label = "PROTEIN", icon = Icons.Default.Star)
                        AnalysisMetric(value = "${result.carbs}g", label = "CARBS", icon = Icons.Default.Star)
                        AnalysisMetric(value = "${result.fat}g", label = "FATS", icon = Icons.Default.Star)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEBEFE9))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Summary", tint = ForestGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI INSIGHTS & TIP",
                            style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 22.sp),
                        modifier = Modifier.testTag("meal_description_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Food Additives Analysis",
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE0E3DE), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (result.additives.isEmpty()) {
                        Text(
                            text = "No synthetic food additives found! This meal is completely clean and organic.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF615E58))
                        )
                    } else {
                        result.additives.forEach { (name, concern) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Additive Info",
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (concern == "No concern") Color(0xFFEBF9F0) else Color(0xFFFFF5E6))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = concern,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (concern == "No concern") ForestGreen else Color(0xFFE65100),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Healthier Alternatives",
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Tipping your plates with these could boost wellness scores!",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF615E58), fontSize = 13.sp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(result.alternatives) { alt ->
                    AlternativeCardItem(alt = alt)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showMealTypePicker = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("btn_log_meal"),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Log", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log This Meal to Journal",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AnalysisMetric(value: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label, tint = ForestGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(0xFF6F7A72))
        )
    }
}

@Composable
fun AlternativeCardItem(alt: AlternativeFood) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEBEFE9), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alt.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEBF9F0))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${alt.score} pts",
                        style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alt.pointsText,
                style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFE65100), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alt.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = Color(0xFF615E58)),
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("P: ${alt.protein}g", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(0xFF6F7A72)))
                Text("C: ${alt.carbs}g", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(0xFF6F7A72)))
                Text("Fib: ${alt.fiber}g", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(0xFF6F7A72)))
            }
        }
    }
}
