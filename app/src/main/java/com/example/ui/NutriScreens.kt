package com.example.ui

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.NutriViewModel
import kotlinx.coroutines.delay

// Helper to determine if we are on a tablet device (Expanded Layout)
@Composable
fun isTabletLayout(): Boolean {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}

/**
 * Main Onboarding / Splash view. Displays centered branding with linear loading bar.
 */
@Composable
fun SplashScreen(onContinue: () -> Unit) {
    val logoScale = remember { androidx.compose.animation.core.Animatable(0f) }
    val logoAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val textAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val progressAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy))
        logoAlpha.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(400))
        textAlpha.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(600))
        progressAnim.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(1400))
        delay(300)
        onContinue()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F1A14)).testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier.size(110.dp).scale(logoScale.value).clip(CircleShape).background(ForestGreen)
                    .border(2.dp, Color(0xFF9BE9BB).copy(alpha = 0.4f), CircleShape)
                    .scale(logoScale.value).alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                Text("N", style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text("NutriLens AI", style = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 36.sp), modifier = Modifier.alpha(textAlpha.value))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your food. Your score.", style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF9BB8A8)), modifier = Modifier.alpha(textAlpha.value))
            Spacer(modifier = Modifier.height(48.dp))
            Box(modifier = Modifier.width(200.dp).height(3.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).alpha(textAlpha.value)) {
                Box(modifier = Modifier.fillMaxWidth(progressAnim.value).fillMaxHeight().clip(CircleShape).background(ForestGreen))
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)) {
            Text("v1.0.0 Made with love for healthier India", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4A6B5A)), modifier = Modifier.alpha(textAlpha.value))
        }
    }
}
@Composable
fun GoalSetupScreen(
    viewModel: NutriViewModel,
    onNavigateBack: () -> Unit,
    onContinue: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var selectedGoal by remember { mutableStateOf(profile?.goal ?: "Manage Weight") }
    val dietaryPrefs = remember { mutableStateListOf<String>() }

    LaunchedEffect(profile) {
        val currentPrefs = profile?.dietaryPreferences?.split(",") ?: emptyList()
        currentPrefs.forEach {
            if (it.isNotBlank() && !dietaryPrefs.contains(it)) {
                dietaryPrefs.add(it)
            }
        }
    }

    val goalsList = listOf(
        Pair("Manage Weight", "Fat loss or gain"),
        Pair("Build Muscle", "Protein focused"),
        Pair("Manage Diabetes", "Sugar & carb control"),
        Pair("PCOS", "Hormonal balance"),
        Pair("Heart Health", "Sodium & fat control"),
        Pair("General Wellness", "Balanced nutrition")
    )

    val allPrefs = listOf(
        "Vegetarian", "Vegan", "Jain", "No Onion-Garlic", "Gluten-Free", "Dairy-Free", "Halal"
    )

    Scaffold(
        topBar = {
            NutriTopAppBar(
                title = "NutriLens AI",
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = OrganicBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "What's your main goal?",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = BentoTextDark,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.testTag("question_header")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your score is calculated specifically for this.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Goal selection bento-grid
                    val cols = if (isTabletLayout()) 3 else 2
                    val rowsCount = (goalsList.size + cols - 1) / cols

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (r in 0 until rowsCount) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (c in 0 until cols) {
                                    val index = r * cols + c
                                    if (index < goalsList.size) {
                                        val (gName, gDesc) = goalsList[index]
                                        val isSelected = selectedGoal == gName

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) SoftGreenTint else Color.White)
                                                .border(
                                                    width = 2.dp,
                                                    color = if (isSelected) ForestGreen else Color.Transparent,
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                                .clickable { selectedGoal = gName }
                                                .padding(16.dp)
                                                .testTag("goal_card_$gName")
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(ForestGreen)
                                                        .align(Alignment.TopEnd),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else Color(0xFFEBEFE9)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = when (gName) {
                                                            "Manage Weight" -> Icons.Default.Settings
                                                            "Build Muscle" -> Icons.Default.Star
                                                            "Manage Diabetes" -> Icons.Default.Favorite
                                                            "PCOS" -> Icons.Default.Person
                                                            "Heart Health" -> Icons.Default.Favorite
                                                            else -> Icons.Default.Star // Guaranteed default
                                                        },
                                                        contentDescription = gName,
                                                        tint = if (isSelected) ForestGreen else Color(0xFF615E58)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))

                                                Text(
                                                    text = gName,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontSize = 16.sp,
                                                        color = BentoTextDark
                                                    )
                                                )
                                                Text(
                                                    text = gDesc,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = BentoTextGrey,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFEBEFE9))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "Any dietary preferences?",
                                style = MaterialTheme.typography.titleMedium.copy(color = BentoTextDark)
                            )
                            Text(
                                text = "Select all that apply to refine your lens.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = BentoTextGrey,
                                    fontSize = 14.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val chunkedPrefs = allPrefs.chunked(3)
                                chunkedPrefs.forEach { rowPrefs ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        rowPrefs.forEach { pref ->
                                            val isSelected = dietaryPrefs.contains(pref)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(999.dp))
                                                    .background(if (isSelected) ForestGreen else Color.White)
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) Color.Transparent else Color(0xFFBFC9C0),
                                                        RoundedCornerShape(999.dp)
                                                    )
                                                    .clickable {
                                                        if (isSelected) dietaryPrefs.remove(pref)
                                                        else dietaryPrefs.add(pref)
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                                    .testTag("diet_chip_$pref")
                                            ) {
                                                Text(
                                                    text = pref,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isSelected) Color.White else BentoTextGrey,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.saveUserProfile(
                            name = profile?.name ?: "Anubhav",
                            sex = profile?.sex ?: "Male",
                            age = profile?.age ?: 29,
                            weight = profile?.weight ?: 75,
                            height = profile?.height ?: 175,
                            activityLevel = profile?.activityLevel ?: "Lightly active",
                            goal = selectedGoal,
                            dietaryPreferences = dietaryPrefs.joinToString(",")
                        )
                        onContinue()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_continue_step1"),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Forward",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Onboarding Question 2: Quick Numbers Setup
 */
@Composable
fun ProfileNumbersScreen(
    viewModel: NutriViewModel,
    onNavigateBack: () -> Unit,
    onSetMyGoals: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var gender by remember { mutableStateOf(profile?.sex ?: "Male") }
    var age by remember { mutableStateOf(profile?.age ?: 29) }
    var weight by remember { mutableStateOf(profile?.weight ?: 75) }
    var height by remember { mutableStateOf(profile?.height ?: 175) }
    var activity by remember { mutableStateOf(profile?.activityLevel ?: "Lightly active") }

    val bmr = remember(gender, age, weight, height) {
        if (gender.equals("Male", ignoreCase = true)) {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }
    }
    val activityFactor = remember(activity) {
        when (activity) {
            "Mostly sitting" -> 1.2
            "Lightly active" -> 1.375
            "Active" -> 1.55
            "Very active" -> 1.725
            else -> 1.375
        }
    }
    val targetGoalString = profile?.goal ?: "Manage Weight"
    val targetCalories = remember(bmr, activityFactor, targetGoalString) {
        var base = (bmr * activityFactor).toInt()
        when (targetGoalString) {
            "Manage Weight" -> base -= 350
            "Build Muscle" -> base += 300
            "Manage Diabetes" -> base = (base * 0.95).toInt()
            "PCOS" -> base = (base * 0.95).toInt()
            else -> {}
        }
        base
    }

    val proteinGrams = remember(targetCalories) { ((targetCalories * 0.25) / 4).toInt() }
    val carbsGrams = remember(targetCalories) { ((targetCalories * 0.45) / 4).toInt() }
    val fatGrams = remember(targetCalories) { ((targetCalories * 0.30) / 9).toInt() }

    Scaffold(
        topBar = {
            NutriTopAppBar(
                title = "Step 2 of 3",
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = OrganicBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "A few quick numbers",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = BentoTextDark,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "This helps NutriLens tailor your baseline recommendations.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF615E58))
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // 1. Biological Sex Selector chips
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "BIOLOGICAL SEX",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("Male", "Female", "Other").forEach { sex ->
                                    val isSelected = gender == sex
                                    Button(
                                        onClick = { gender = sex },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) SoftGreenTint else Color.White,
                                            contentColor = if (isSelected) ForestGreen else Color(0xFF615E58)
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) ForestGreen else Color(0xFFBFC9C0)
                                        )
                                    ) {
                                        Text(text = sex, style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp))
                                    }
                                }
                            }
                        }
                    }

                    // 2. Interactive & Funny Animated BMI Gauge
                    val bmiValue = remember(weight, height) {
                        if (height > 0) {
                            weight.toDouble() / ((height.toDouble() / 100.0) * (height.toDouble() / 100.0))
                        } else 0.0
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEBEFE9), RoundedCornerShape(16.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "YOUR REAL-TIME BMI METRIC",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72))
                                )
                                Text(
                                    text = "Verified Weight: $weight kg",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ForestGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Monospace measured value makes numbers trustable
                                Text(
                                    text = String.format("%.1f", bmiValue),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 44.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreen
                                    )
                                )

                                val categoryText = when {
                                    bmiValue < 18.5 -> "UNDERWEIGHT"
                                    bmiValue < 25.0 -> "NORMAL"
                                    bmiValue < 30.0 -> "OVERWEIGHT"
                                    else -> "OBESE"
                                }
                                val categoryColor = when {
                                    bmiValue < 18.5 -> Color(0xFF3498DB)
                                    bmiValue < 25.0 -> ForestGreen
                                    bmiValue < 30.0 -> TurmericOrange
                                    else -> Color(0xFF763036)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(categoryColor.copy(alpha = 0.15f))
                                        .border(1.dp, categoryColor, RoundedCornerShape(999.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = categoryText,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = categoryColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Bar Gauge mapping BMI 15.0 to 35.0
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFEBEFE9))
                            ) {
                                val barWidth = maxWidth
                                val pointerFraction = remember(bmiValue) {
                                    val fraction = ((bmiValue - 15.0) / (35.0 - 15.0)).coerceIn(0.0, 1.0)
                                    fraction.toFloat()
                                }
                                val animatedPointerOffset by animateFloatAsState(
                                    targetValue = pointerFraction,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                    label = "BMIPointerOffset"
                                )

                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(modifier = Modifier.weight(0.175f).fillMaxHeight().background(Color(0xFF3498DB).copy(alpha = 0.7f)))
                                    Box(modifier = Modifier.weight(0.325f).fillMaxHeight().background(ForestGreen.copy(alpha = 0.7f)))
                                    Box(modifier = Modifier.weight(0.25f).fillMaxHeight().background(TurmericOrange.copy(alpha = 0.7f)))
                                    Box(modifier = Modifier.weight(0.25f).fillMaxHeight().background(Color(0xFF763036).copy(alpha = 0.7f)))
                                }

                                val dotSize = 16.dp
                                Box(
                                    modifier = Modifier
                                        .size(dotSize)
                                        .offset(x = (barWidth * animatedPointerOffset) - (dotSize / 2))
                                        .align(Alignment.CenterStart)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(3.dp, BentoTextDark, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Funny quote mapping
                            val (funnyQuote, emoji) = remember(bmiValue) {
                                when {
                                    bmiValue < 18.5 -> {
                                        "Featherweight status! 🕊️ A gust of wind might blow you away! Load up on paneer, healthy fats, and proteins." to "🕊️"
                                    }
                                    bmiValue < 25.0 -> {
                                        "Gravitationally Flawless! 🎯 Perfect state of balance. You're physically optimized, like a fine organic sourdough!" to "🎯"
                                    }
                                    bmiValue < 30.0 -> {
                                        "Comfort Cushion Zone! 🐻 Cozy insulation activated. Let's sculpt this premium energy into dynamic muscle steel!" to "🐻"
                                    }
                                    else -> {
                                        "Absolute Power Unit! ⚡ Unmatched energy reserves! Let's fine-tune the engine to burn bright, clean, and strong." to "⚡"
                                    }
                                }
                            }

                            var currentEmojiScaled by remember { mutableStateOf(1f) }
                            LaunchedEffect(funnyQuote) {
                                currentEmojiScaled = 1.4f
                                delay(150)
                                currentEmojiScaled = 1f
                            }
                            val animatedScale by animateFloatAsState(
                                targetValue = currentEmojiScaled,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
                                label = "EmojiPulse"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFCF8F2))
                                    .border(1.dp, Color(0xFFF1ECE4), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = emoji,
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 32.sp
                                        ),
                                        modifier = Modifier.scale(animatedScale)
                                    )
                                    Column {
                                        Text(
                                            text = "Coach NutriSays:",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TurmericOrange,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = funnyQuote,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = BentoTextDark,
                                                fontSize = 13.sp,
                                                fontStyle = FontStyle.Italic
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Height slider card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HEIGHT",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72))
                                )
                                Text(
                                    text = "$height cm",
                                    style = MaterialTheme.typography.titleMedium.copy(color = ForestGreen)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = height.toFloat(),
                                onValueChange = { height = it.toInt() },
                                valueRange = 100f..220f,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = ForestGreen,
                                    inactiveTrackColor = Color(0xFFE0E3DE),
                                    thumbColor = ForestGreen
                                )
                            )
                        }
                    }

                    // 4. Activity Selector Row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "ACTIVITY LEVEL",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7A72))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val activitiesList = listOf(
                                "Mostly sitting" to "Desk job, little intentional exercise.",
                                "Lightly active" to "Light exercise 1-3 days a week.",
                                "Active" to "Moderate exercise 3-5 days a week.",
                                "Very active" to "Heavy exercise 6-7 days a week."
                            )

                            activitiesList.forEach { (title, subtitle) ->
                                val isSelected = activity == title
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFEBEFE9) else Color.White)
                                        .clickable { activity = title }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontSize = 15.sp,
                                                color = BentoTextDark
                                            )
                                        )
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 12.sp,
                                                color = Color(0xFF615E58)
                                            )
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { activity = title },
                                        colors = RadioButtonDefaults.colors(selectedColor = ForestGreen)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }

                    // 5. Baseline card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFEBF9F0), Color(0xFFE6E9E4))
                                )
                            )
                            .border(1.dp, Color(0xFFE0E3DE), RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Calculated",
                                    tint = ForestGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ESTIMATED BASELINE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ForestGreen,
                                        letterSpacing = 0.15.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%,d", targetCalories),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 40.sp,
                                        color = BentoTextDark
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "kcal/day",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF615E58)),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.8f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "MAINTENANCE MACROS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = Color(0xFF6F7A72)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF93474C)))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "P: ${proteinGrams}g",
                                                style = MaterialTheme.typography.labelMedium.copy(color = BentoTextDark)
                                            )
                                        }
                                        Text("|", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFBFC9C0)))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ForestGreen))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "C: ${carbsGrams}g",
                                                style = MaterialTheme.typography.labelMedium.copy(color = BentoTextDark)
                                            )
                                        }
                                        Text("|", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFBFC9C0)))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF4A261)))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "F: ${fatGrams}g",
                                                style = MaterialTheme.typography.labelMedium.copy(color = BentoTextDark)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.saveUserProfile(
                            name = profile?.name ?: "Anubhav",
                            sex = gender,
                            age = age,
                            weight = weight,
                            height = height,
                            activityLevel = activity,
                            goal = targetGoalString,
                            dietaryPreferences = profile?.dietaryPreferences ?: ""
                        )
                        onSetMyGoals()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_set_goals"),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Set My Goals",
                            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Forward Finish Link Setup",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Common Top Bar utilizing MaterialTheme colors
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriTopAppBar(
    title: String,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    showNotif: Boolean = true,
    onNotifClick: () -> Unit = {},
    showSearch: Boolean = true,
    onSearchClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
            )
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go Back",
                        tint = ForestGreen
                    )
                }
            }
        },
        actions = {
            if (showSearch) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("app_bar_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Log and Swaps",
                        tint = ForestGreen
                    )
                }
            }
            if (showNotif) {
                IconButton(
                    onClick = onNotifClick,
                    modifier = Modifier.testTag("app_bar_notify_btn")
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = ForestGreen
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = OrganicBackground
        )
    )
}

/**
 * Universal Bottom Navigation Layout with scan FAB
 */
@Composable
fun NutriBottomBar(
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    onScanClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .height(80.dp)
            .drawBehind {
                drawLine(
                    color = Color(0xFFEBEFE9),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedRoute == "dashboard",
                onClick = { onRouteSelected("dashboard") }
            )
            BottomNavItem(
                icon = Icons.Default.Settings, // Guaranteed standard icon
                label = "Journal",
                isSelected = selectedRoute == "journal",
                onClick = { onRouteSelected("journal") }
            )

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ForestGreen)
                    .clickable { onScanClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Scan Food",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            BottomNavItem(
                icon = Icons.Default.Star, // Guaranteed insights icon
                label = "Insights",
                isSelected = selectedRoute == "insights",
                onClick = { onRouteSelected("insights") }
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = selectedRoute == "profile",
                onClick = { onRouteSelected("profile") }
            )
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) ForestGreen else Color(0xFF615E58)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                color = if (isSelected) ForestGreen else Color(0xFF615E58)
            )
        )
    }
}

/**
 * Main App Screen Container handling standard view presentation & Tablet sidebars
 */
@Composable
fun MainScreenContainer(
    viewModel: NutriViewModel,
    initialRoute: String = "dashboard",
    onNavigateToSetup: () -> Unit,
    onNavigateToScanResult: () -> Unit,
    onNavigateToBarcodeScan: () -> Unit,
    onNavigateToAlternatives: () -> Unit,
    onNavigateToNudges: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToWeightTracker: () -> Unit = {},
    onNavigateToDietPlan: () -> Unit = {},
) {
    var activeRoute by remember { mutableStateOf(initialRoute) }

    val isTablet = isTabletLayout()

    Scaffold(
        topBar = {
            NutriTopAppBar(
                title = "NutriLens AI",
                showBack = false,
                onNotifClick = onNavigateToNudges,
                onSearchClick = onNavigateToSearch
            )
        },
        bottomBar = {
            if (!isTablet) {
                NutriBottomBar(
                    selectedRoute = activeRoute,
                    onRouteSelected = { activeRoute = it },
                    onScanClick = onNavigateToScanResult
                )
            }
        },
        containerColor = OrganicBackground
    ) { innerPadding ->
        if (isTablet) {
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                NavigationRail(
                    containerColor = Color.White,
                    header = {
                        FloatingActionButton(
                            onClick = onNavigateToScanResult,
                            containerColor = ForestGreen,
                            contentColor = Color.White
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Scan Food")
                        }
                    }
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    NavigationRailItem(
                        selected = activeRoute == "dashboard",
                        onClick = { activeRoute = "dashboard" },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationRailItem(
                        selected = activeRoute == "journal",
                        onClick = { activeRoute = "journal" },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Journal") },
                        label = { Text("Journal") }
                    )
                    NavigationRailItem(
                        selected = activeRoute == "insights",
                        onClick = { activeRoute = "insights" },
                        icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Insights") },
                        label = { Text("Insights") }
                    )
                    NavigationRailItem(
                        selected = activeRoute == "profile",
                        onClick = { activeRoute = "profile" },
                        icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }

                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFFE0E3DE)))

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ActiveRouteContent(
                        route = activeRoute,
                        viewModel = viewModel,
                        onScanTrigger = onNavigateToScanResult,
                        onNavigateToSetup = onNavigateToSetup,
                        onNavigateToBarcode = onNavigateToBarcodeScan,
                        onNavigateToAlternatives = onNavigateToAlternatives,
                        onNavigateToChat = { activeRoute = "chat" },
                    onNavigateToWeightTracker = onNavigateToWeightTracker,
                    onNavigateToDietPlan = onNavigateToDietPlan
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                ActiveRouteContent(
                    route = activeRoute,
                    viewModel = viewModel,
                    onScanTrigger = onNavigateToScanResult,
                    onNavigateToSetup = onNavigateToSetup,
                    onNavigateToBarcode = onNavigateToBarcodeScan,
                    onNavigateToAlternatives = onNavigateToAlternatives,
                    onNavigateToChat = { activeRoute = "chat" },
                    onNavigateToWeightTracker = onNavigateToWeightTracker,
                    onNavigateToDietPlan = onNavigateToDietPlan
                )
            }
        }
    }
}

/**
 * Route Router Mapper
 */
@Composable
fun ActiveRouteContent(
    route: String,
    viewModel: NutriViewModel,
    onScanTrigger: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToBarcode: () -> Unit,
    onNavigateToAlternatives: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToWeightTracker: () -> Unit = {},
    onNavigateToDietPlan: () -> Unit = {}
) {
    when (route) {
        "dashboard" -> DashboardScreen(
            viewModel = viewModel,
            onScanTrigger = onScanTrigger,
            onOpenBarcode = onNavigateToBarcode,
            onNavigateToChat = onNavigateToChat
        )
        "journal" -> JournalScreen(
            viewModel = viewModel,
            onMealsChange = {},
            onOpenScan = onScanTrigger
        )
        "insights" -> InsightsScreen(viewModel = viewModel, onAlternativesClick = onNavigateToAlternatives)
        "profile" -> ProfileScreen(viewModel = viewModel, onNavigateToSetup = onNavigateToSetup, onNavigateToWeightTracker = onNavigateToWeightTracker, onNavigateToDietPlan = onNavigateToDietPlan)
        "chat" -> ChatScreen(viewModel = viewModel)
    }
}
