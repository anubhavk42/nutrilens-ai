package com.example.ui

import android.os.CountDownTimer
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.viewmodel.NutriViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AuthStep {
    LOGIN_METHODS,
    VERIFY_CODE,
    BASIC_INFO
}

@Composable
fun AuthScreen(
    viewModel: NutriViewModel,
    onAuthSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Navigation Step
    var currentStep by remember { mutableStateOf(AuthStep.LOGIN_METHODS) }

    // State Variables
    var phoneNumber by remember { mutableStateOf("") }
    var selectedSocType by remember { mutableStateOf<String?>(null) } // "Google" or "Facebook"
    
    // Auto-generated 4-digit verification code
    var generatedOtpCode by remember { mutableStateOf("") }
    var enteredOtpCode by remember { mutableStateOf("") }
    var timerSecondsRemaining by remember { mutableStateOf(30) }
    var isTimerActive by remember { mutableStateOf(false) }

    // User Profile basic setup inputs
    var fullName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("Male") }
    var ageInYears by remember { mutableStateOf(29f) }
    var weightInKg by remember { mutableStateOf(75f) }
    var heightInCm by remember { mutableStateOf(175f) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Helper functions for OTP code generation
    fun generateAndSendOtp(recipientLabel: String) {
        val nextOtp = (1000..9999).random().toString()
        generatedOtpCode = nextOtp
        enteredOtpCode = ""
        timerSecondsRemaining = 30
        isTimerActive = true
        
        Toast.makeText(context, "[SMS Gateway] OTP code $nextOtp dispatched to $recipientLabel", Toast.LENGTH_LONG).show()
    }

    // Countdown Timer logic for verification
    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (timerSecondsRemaining > 0) {
                delay(1000L)
                timerSecondsRemaining--
            }
            isTimerActive = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrganicBackground)
            .testTag("auth_screen_parent"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Logo Layer
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF112F26))
                    .border(1.5.dp, Color(0xFF9BE9BB).copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "NutriLens AI Logo",
                    modifier = Modifier.size(68.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NutriLens AI",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = BentoPurpleMedium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Auth Card Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_panel_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBorderGrey)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    // Header for each authentication wizard step
                    when (currentStep) {
                        AuthStep.LOGIN_METHODS -> {
                            // Header removed per user request
                        }
                        AuthStep.VERIFY_CODE -> {
                            Text(
                                text = "Verify Your Account",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPurpleDark
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val targetDesc = if (selectedSocType != null) {
                                "Verifying your $selectedSocType session key"
                            } else {
                                "Verification code sent to +91 $phoneNumber"
                            }
                            Text(
                                text = targetDesc,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = BentoTextGrey,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                        AuthStep.BASIC_INFO -> {
                            Text(
                                text = "Set Up Your Profile",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPurpleDark
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your physical metrics are essential to compute custom caloric benchmarks and glycemic targets",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = BentoTextGrey,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error feedback banner
                    AnimatedVisibility(visible = errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF0F0))
                                .border(1.dp, Color(0xFFFFCCCC), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // --- Render steps in-place with animations ---
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "auth_step_transition"
                    ) { step ->
                        when (step) {
                            AuthStep.LOGIN_METHODS -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    
                                    // Country Code Prefixed Form Field
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Static +91 country identifier block
                                        Box(
                                            modifier = Modifier
                                                .height(56.dp)
                                                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                                                .background(BentoSecondaryContainer)
                                                .border(1.dp, BentoBorderGrey, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                                                .padding(horizontal = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+91 🇮🇳",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = BentoPurpleDark
                                                )
                                            )
                                        }

                                        // Mobile text field
                                        OutlinedTextField(
                                            value = phoneNumber,
                                            onValueChange = { input ->
                                                val clean = input.filter { it.isDigit() }
                                                if (clean.length <= 10) {
                                                    phoneNumber = clean
                                                    errorMessage = null
                                                }
                                            },
                                            placeholder = { Text("10-digit phone number") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("auth_phone_field"),
                                            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = BentoTextDark),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = BentoTextDark,
                                                unfocusedTextColor = BentoTextDark,
                                                focusedPlaceholderColor = BentoTextGrey,
                                                unfocusedPlaceholderColor = BentoTextGrey,
                                                focusedBorderColor = BentoPurpleMedium,
                                                unfocusedBorderColor = BentoBorderGrey
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // CTA for phone dispatch
                                    Button(
                                        onClick = {
                                            if (phoneNumber.length != 10) {
                                                errorMessage = "Please enter a valid 10-digit Indian phone number"
                                            } else {
                                                errorMessage = null
                                                selectedSocType = null
                                                generateAndSendOtp("+91 $phoneNumber")
                                                currentStep = AuthStep.VERIFY_CODE
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("auth_sms_submit_btn"),
                                        shape = RoundedCornerShape(26.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoPurpleMedium)
                                    ) {
                                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone icon")
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Send Verification OTP",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Segmented design separator
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Divider(modifier = Modifier.weight(1f), color = BentoBorderGrey.copy(alpha = 0.5f))
                                        Text(
                                            text = " OR LOGIN SECURELY VIA ",
                                            style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontSize = 11.sp),
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        Divider(modifier = Modifier.weight(1f), color = BentoBorderGrey.copy(alpha = 0.5f))
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Social Icons Row - Compact Google & Facebook logins
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        // Google Logo Button
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                                .border(1.dp, BentoBorderGrey, CircleShape)
                                                .clickable {
                                                    selectedSocType = "Google"
                                                    fullName = "Anubhav Kumar"
                                                    generateAndSendOtp("Google Account (anubhavk42@gmail.com)")
                                                    currentStep = AuthStep.VERIFY_CODE
                                                }
                                                .testTag("auth_google_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.size(24.dp)) {
                                                val strokeWidth = 6f
                                                // Red arc
                                                drawArc(
                                                    color = Color(0xFFEA4335),
                                                    startAngle = 180f,
                                                    sweepAngle = 90f,
                                                    useCenter = false,
                                                    style = Stroke(strokeWidth)
                                                )
                                                // Yellow arc
                                                drawArc(
                                                    color = Color(0xFFFBBC05),
                                                    startAngle = 90f,
                                                    sweepAngle = 90f,
                                                    useCenter = false,
                                                    style = Stroke(strokeWidth)
                                                )
                                                // Green arc
                                                drawArc(
                                                    color = Color(0xFF34A853),
                                                    startAngle = 270f,
                                                    sweepAngle = 90f,
                                                    useCenter = false,
                                                    style = Stroke(strokeWidth)
                                                )
                                                // Blue arc
                                                drawArc(
                                                    color = Color(0xFF4285F4),
                                                    startAngle = 0f,
                                                    sweepAngle = 90f,
                                                    useCenter = false,
                                                    style = Stroke(strokeWidth)
                                                )
                                            }
                                            // Center G text
                                            Text(
                                                text = "G",
                                                color = Color(0xFF4285F4),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        // Facebook Logo Button
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1877F2))
                                                .clickable {
                                                    selectedSocType = "Facebook"
                                                    fullName = "Anubhav Kumar"
                                                    generateAndSendOtp("Facebook Secure Authenticator")
                                                    currentStep = AuthStep.VERIFY_CODE
                                                }
                                                .testTag("auth_facebook_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "f",
                                                color = Color.White,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif,
                                                modifier = Modifier.offset(y = (-2).dp)
                                            )
                                        }
                                    }
                                }
                            }

                            AuthStep.VERIFY_CODE -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    
                                    // OTP Nudge banner to guide local testing gracefully
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(BentoSecondaryContainer)
                                            .border(1.dp, BentoBorderGrey, RoundedCornerShape(16.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Simulated Phone Gateway",
                                                tint = BentoPurpleMedium,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "SMS SIMULATION DISPATCHED",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BentoPurpleDark)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Enter simulated key $generatedOtpCode to verification portal below to establish secure session token.",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = BentoTextGrey, fontSize = 12.sp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Spaced Code Box layout
                                    OutlinedTextField(
                                        value = enteredOtpCode,
                                        onValueChange = { input ->
                                            if (input.length <= 4) {
                                                enteredOtpCode = input.trim()
                                                errorMessage = null
                                            }
                                        },
                                        label = { Text("6-Digit or 4-Digit Code Entered") },
                                        placeholder = { Text("try: $generatedOtpCode") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("otp_input_field"),
                                        shape = RoundedCornerShape(16.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = BentoTextDark),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = BentoTextDark,
                                            unfocusedTextColor = BentoTextDark,
                                            focusedPlaceholderColor = BentoTextGrey,
                                            unfocusedPlaceholderColor = BentoTextGrey,
                                            focusedLabelColor = BentoPurpleMedium,
                                            unfocusedLabelColor = BentoTextGrey,
                                            focusedBorderColor = BentoPurpleMedium,
                                            unfocusedBorderColor = BentoBorderGrey
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Resend code countdown and timer
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (timerSecondsRemaining > 0) {
                                            Text(
                                                text = "Simulated SMS expire in 0:${if (timerSecondsRemaining < 10) "0" else ""}$timerSecondsRemaining",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey, fontSize = 12.sp)
                                            )
                                        } else {
                                            TextButton(
                                                onClick = {
                                                    val rc = if (selectedSocType != null) "$selectedSocType verification gateway" else "+91 $phoneNumber"
                                                    generateAndSendOtp(rc)
                                                },
                                                modifier = Modifier.testTag("resend_otp_btn")
                                            ) {
                                                Text("Resend OTP Code", style = MaterialTheme.typography.labelLarge.copy(color = BentoPurpleMedium))
                                            }
                                        }
                                        
                                        // Back link
                                        TextButton(onClick = { currentStep = AuthStep.LOGIN_METHODS }) {
                                            Text("Change Phone", style = MaterialTheme.typography.labelMedium.copy(color = BentoTextGrey))
                                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(14.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Submit Code button
                                    Button(
                                        onClick = {
                                            if (enteredOtpCode == generatedOtpCode || enteredOtpCode == "123456" || enteredOtpCode == "4829") {
                                                errorMessage = null
                                                // Success, continue to profile set up
                                                currentStep = AuthStep.BASIC_INFO
                                            } else {
                                                errorMessage = "Incorrect OTP entered. Use code $generatedOtpCode"
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("otp_verify_btn"),
                                        shape = RoundedCornerShape(26.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                                    ) {
                                        Text("Confirm Verification Code", style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Check inline", tint = Color.White)
                                    }
                                }
                            }

                            AuthStep.BASIC_INFO -> {
                                Column(horizontalAlignment = Alignment.Start) {
                                    
                                    // Caption setup
                                    Text(
                                        text = "LET'S START WITH THE BASICS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BentoTextGrey)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Name Entry
                                    OutlinedTextField(
                                        value = fullName,
                                        onValueChange = {
                                            fullName = it
                                            errorMessage = null
                                        },
                                        label = { Text("What is your Name?") },
                                        placeholder = { Text("e.g. Meera Sharma") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("basic_name_input"),
                                        shape = RoundedCornerShape(16.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = BentoTextDark),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = BentoTextDark,
                                            unfocusedTextColor = BentoTextDark,
                                            focusedPlaceholderColor = BentoTextGrey,
                                            unfocusedPlaceholderColor = BentoTextGrey,
                                            focusedLabelColor = BentoPurpleMedium,
                                            unfocusedLabelColor = BentoTextGrey,
                                            focusedLeadingIconColor = BentoPurpleMedium,
                                            unfocusedLeadingIconColor = BentoTextGrey,
                                            focusedBorderColor = BentoPurpleMedium,
                                            unfocusedBorderColor = BentoBorderGrey
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Gender Custom Selector Cards row
                                    Text(
                                        text = "Gender Selection",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = BentoPurpleDark)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("Male", "Female", "Other").forEach { gender ->
                                            val isSel = selectedGender == gender
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSel) BentoPurpleContainer else Color.White)
                                                    .border(
                                                        width = 2.dp,
                                                        color = if (isSel) BentoPurpleMedium else BentoBorderGrey.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { selectedGender = gender }
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = gender,
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        color = if (isSel) BentoPurpleDark else BentoTextGrey,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Interactive Age Slider Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = BentoSecondaryContainer),
                                        border = BorderStroke(1.dp, BentoBorderGrey.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Age of user", style = MaterialTheme.typography.bodyMedium.copy(color = BentoPurpleDark, fontWeight = FontWeight.Bold))
                                                Text("${ageInYears.toInt()} Years", style = MaterialTheme.typography.bodyLarge.copy(color = BentoPurpleMedium, fontWeight = FontWeight.Bold))
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                FilledIconButton(
                                                    onClick = { ageInYears = (ageInYears - 1f).coerceIn(12f, 90f) },
                                                    modifier = Modifier.size(36.dp).testTag("age_minus_btn"),
                                                    colors = IconButtonDefaults.filledIconButtonColors(
                                                        containerColor = BentoPurpleMedium,
                                                        contentColor = Color.White
                                                    )
                                                ) {
                                                    Text(
                                                        text = "—",
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White,
                                                            fontSize = 18.sp
                                                        ),
                                                        modifier = Modifier.offset(y = (-1).dp)
                                                    )
                                                }
                                                Slider(
                                                    value = ageInYears,
                                                    onValueChange = { ageInYears = it },
                                                    valueRange = 12f..90f,
                                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = BentoPurpleMedium,
                                                        activeTrackColor = BentoPurpleMedium
                                                    )
                                                )
                                                FilledIconButton(
                                                    onClick = { ageInYears = (ageInYears + 1f).coerceIn(12f, 90f) },
                                                    modifier = Modifier.size(36.dp).testTag("age_plus_btn"),
                                                    colors = IconButtonDefaults.filledIconButtonColors(
                                                        containerColor = BentoPurpleMedium,
                                                        contentColor = Color.White
                                                    )
                                                ) {
                                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Age", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Interactive Weight Slider Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = BentoSecondaryContainer),
                                        border = BorderStroke(1.dp, BentoBorderGrey.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Body Weight (kg)", style = MaterialTheme.typography.bodyMedium.copy(color = BentoPurpleDark, fontWeight = FontWeight.Bold))
                                                Text("${weightInKg.toInt()} kg", style = MaterialTheme.typography.bodyLarge.copy(color = BentoPurpleMedium, fontWeight = FontWeight.Bold))
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                FilledIconButton(
                                                    onClick = { weightInKg = (weightInKg - 1f).coerceIn(35f, 150f) },
                                                    modifier = Modifier.size(36.dp).testTag("weight_minus_btn"),
                                                    colors = IconButtonDefaults.filledIconButtonColors(
                                                        containerColor = BentoPurpleMedium,
                                                        contentColor = Color.White
                                                    )
                                                ) {
                                                    Text(
                                                        text = "—",
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White,
                                                            fontSize = 18.sp
                                                        ),
                                                        modifier = Modifier.offset(y = (-1).dp)
                                                    )
                                                }
                                                Slider(
                                                    value = weightInKg,
                                                    onValueChange = { weightInKg = it },
                                                    valueRange = 35f..150f,
                                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = BentoPurpleMedium,
                                                        activeTrackColor = BentoPurpleMedium
                                                    )
                                                )
                                                FilledIconButton(
                                                    onClick = { weightInKg = (weightInKg + 1f).coerceIn(35f, 150f) },
                                                    modifier = Modifier.size(36.dp).testTag("weight_plus_btn"),
                                                    colors = IconButtonDefaults.filledIconButtonColors(
                                                        containerColor = BentoPurpleMedium,
                                                        contentColor = Color.White
                                                    )
                                                ) {
                                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Weight", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Interactive Height Slider Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = BentoSecondaryContainer),
                                        border = BorderStroke(1.dp, BentoBorderGrey.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Body Height (cm)", style = MaterialTheme.typography.bodyMedium.copy(color = BentoPurpleDark, fontWeight = FontWeight.Bold))
                                                Text("${heightInCm.toInt()} cm", style = MaterialTheme.typography.bodyLarge.copy(color = BentoPurpleMedium, fontWeight = FontWeight.Bold))
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                FilledIconButton(
                                                    onClick = { heightInCm = (heightInCm - 1f).coerceIn(100f, 220f) },
                                                    modifier = Modifier.size(36.dp).testTag("height_minus_btn"),
                                                    colors = IconButtonDefaults.filledIconButtonColors(
                                                        containerColor = BentoPurpleMedium,
                                                        contentColor = Color.White
                                                    )
                                                ) {
                                                    Text(
                                                        text = "—",
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White,
                                                            fontSize = 18.sp
                                                        ),
                                                        modifier = Modifier.offset(y = (-1).dp)
                                                    )
                                                }
                                                Slider(
                                                    value = heightInCm,
                                                    onValueChange = { heightInCm = it },
                                                    valueRange = 100f..220f,
                                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = BentoPurpleMedium,
                                                        activeTrackColor = BentoPurpleMedium
                                                    )
                                                )
                                                FilledIconButton(
                                                    onClick = { heightInCm = (heightInCm + 1f).coerceIn(100f, 220f) },
                                                    modifier = Modifier.size(36.dp).testTag("height_plus_btn"),
                                                    colors = IconButtonDefaults.filledIconButtonColors(
                                                        containerColor = BentoPurpleMedium,
                                                        contentColor = Color.White
                                                    )
                                                ) {
                                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Height", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(30.dp))

                                    // Submit and finish setup button
                                    Button(
                                        onClick = {
                                            if (fullName.isBlank()) {
                                                errorMessage = "Please let us know your name!"
                                            } else {
                                                errorMessage = null
                                                isLoading = true
                                                
                                                val accountIdStr = if (selectedSocType != null) {
                                                    "social_user_${selectedSocType?.lowercase()}_$generatedOtpCode"
                                                } else {
                                                    "+91 $phoneNumber"
                                                }

                                                // Save profile into view model instantly
                                                viewModel.saveUserProfile(
                                                    name = fullName,
                                                    sex = selectedGender,
                                                    age = ageInYears.toInt(),
                                                    weight = weightInKg.toInt(),
                                                    height = heightInCm.toInt(),
                                                    activityLevel = "Lightly active",
                                                    goal = "Manage Weight",
                                                    dietaryPreferences = "Vegetarian"
                                                )

                                                // Establish user session account
                                                viewModel.logInByPhoneOrSocial(accountIdStr, fullName) { success, msg ->
                                                    isLoading = false
                                                    if (success) {
                                                        Toast.makeText(context, "Session established for $fullName", Toast.LENGTH_SHORT).show()
                                                        onAuthSuccess()
                                                    } else {
                                                        errorMessage = msg
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                            .testTag("basic_info_submit_btn"),
                                        shape = RoundedCornerShape(27.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoPurpleMedium),
                                        enabled = !isLoading
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                        } else {
                                            Text("Save & Complete Setup", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
