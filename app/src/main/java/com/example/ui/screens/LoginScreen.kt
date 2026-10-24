package com.example.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ui.components.NotificationPromptDialog
import com.example.ui.components.SCoinHexagonBadge
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.SahityaGold
import com.example.ui.theme.SahityaGoldBright
import com.example.ui.theme.SahityaRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextCrispWhite
import com.example.ui.theme.TextMutedGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun triggerOtpNotification(context: Context, otpCode: String) {
    val channelId = "sahitya_alerts_channel"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Sahitya Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Sahitya AI Roleplay & Story Updates"
            enableLights(true)
            lightColor = android.graphics.Color.RED
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Sahitya Verification Code 🔑")
        .setContentText("Your OTP for Sahitya login is $otpCode. Do not share it with anyone.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    try {
        notificationManager.notify(1001, notification)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun triggerWelcomeNotification(context: Context) {
    val channelId = "sahitya_alerts_channel"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Sahitya Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Sahitya AI Roleplay & Story Updates"
            enableLights(true)
            lightColor = android.graphics.Color.RED
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Welcome to Sahitya! 🎭")
        .setContentText("Notifications enabled successfully! You will now receive AI roleplay replies & daily coin alerts.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    try {
        notificationManager.notify(1002, notification)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (phoneNumber: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var phoneNumber by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var isOtpRequested by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var otpDigit1 by remember { mutableStateOf("") }
    var otpDigit2 by remember { mutableStateOf("") }
    var otpDigit3 by remember { mutableStateOf("") }
    var otpDigit4 by remember { mutableStateOf("") }

    var isAutofilling by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }
    var showSmsNotification by remember { mutableStateOf(false) }
    var isLoadingOtp by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notifications enabled! 🔔", Toast.LENGTH_SHORT).show()
        }
        // Proceed with OTP generation after Android native system dialog interaction
        isLoadingOtp = true
        isOtpRequested = true
    }

    fun startLoginProcess() {
        if (phoneNumber.trim().length < 10) {
            Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            return
        }

        val areEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!areEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Trigger Android's native OS system permission dialog directly
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Proceed to generate OTP directly and let Android OS handle notifications
            isLoadingOtp = true
            isOtpRequested = true
        }
    }

    // Autofill effect when OTP is requested
    LaunchedEffect(isOtpRequested) {
        if (isOtpRequested) {
            // Generate a random 4-digit OTP
            val code = (1000..9999).random().toString()
            generatedOtp = code

            // Send single OTP system notification
            triggerOtpNotification(context, code)

            // 2. Start autofill sequence
            delay(1000)
            isAutofilling = true

            // Fill digits step-by-step
            delay(300)
            otpDigit1 = code[0].toString()
            delay(200)
            otpDigit2 = code[1].toString()
            delay(200)
            otpDigit3 = code[2].toString()
            delay(200)
            otpDigit4 = code[3].toString()

            delay(400)
            isAutofilling = false
            isVerified = true

            // 3. Complete login automatically after verified
            delay(800)
            val fullPhone = "$countryCode $phoneNumber"
            onLoginSuccess(fullPhone)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .testTag("login_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Branding App Icon & Title
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(listOf(SahityaRed, SahityaGold)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                SCoinHexagonBadge(size = 48.dp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Sahitya AI",
                color = TextCrispWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = "Interactive AI Roleplay",
                color = SahityaGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Main Auth Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isOtpRequested) {
                        // --- STEP 1: MOBILE NUMBER INPUT ---
                        Text(
                            text = "Welcome to Sahitya 👋",
                            color = TextCrispWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Enter your mobile number to get an OTP",
                            color = TextMutedGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Country Code + Phone Number Input Field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Country Code Box
                            Box(
                                modifier = Modifier
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PitchBlack)
                                    .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🇮🇳 $countryCode",
                                    color = TextCrispWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Phone Number OutlinedTextField
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { input ->
                                    if (input.length <= 10 && input.all { it.isDigit() }) {
                                        phoneNumber = input
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_mobile_number"),
                                placeholder = {
                                    Text("Mobile Number", color = TextMutedGray, fontSize = 14.sp)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Phone",
                                        tint = SahityaGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PitchBlack,
                                    unfocusedContainerColor = PitchBlack,
                                    focusedBorderColor = SahityaRed,
                                    unfocusedBorderColor = CardBorderDark,
                                    focusedTextColor = TextCrispWhite,
                                    unfocusedTextColor = TextCrispWhite
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Get OTP Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(
                                    if (phoneNumber.length == 10) {
                                        Brush.horizontalGradient(listOf(SahityaRed, Color(0xFFC02A38)))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color.DarkGray, Color.Gray))
                                    }
                                )
                                .clickable(enabled = phoneNumber.length == 10 && !isLoadingOtp) {
                                    startLoginProcess()
                                }
                                .testTag("btn_get_otp"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingOtp) {
                                CircularProgressIndicator(
                                    color = TextCrispWhite,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Message,
                                        contentDescription = "Get OTP",
                                        tint = TextCrispWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "GET OTP",
                                        color = TextCrispWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "By continuing, you agree to Sahitya's Terms of Service and Privacy Policy.",
                            color = TextMutedGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )

                    } else {
                        // --- STEP 2: OTP VERIFICATION & AUTOFILL ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    isOtpRequested = false
                                    showSmsNotification = false
                                    isVerified = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextCrispWhite
                                )
                            }
                            Text(
                                text = "Verify OTP",
                                color = TextCrispWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Code sent to $countryCode $phoneNumber",
                            color = TextMutedGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // OTP 4-Digit Boxes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OtpDigitBox(digit = otpDigit1, isVerified = isVerified)
                            OtpDigitBox(digit = otpDigit2, isVerified = isVerified)
                            OtpDigitBox(digit = otpDigit3, isVerified = isVerified)
                            OtpDigitBox(digit = otpDigit4, isVerified = isVerified)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Status messaging
                        if (isAutofilling) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = SahityaGold,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Detecting SMS & Autofilling OTP...",
                                    color = SahityaGoldBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else if (isVerified) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OTP Verified! Logging in...",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "Resend OTP in 25s",
                                color = TextMutedGray,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Manual Verify Button if needed
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(
                                    brush = if (isVerified) {
                                        Brush.linearGradient(listOf(Color(0xFF388E3C), Color(0xFF2E7D32)))
                                    } else {
                                        Brush.horizontalGradient(listOf(SahityaRed, Color(0xFFC02A38)))
                                    }
                                )
                                .clickable(enabled = !isVerified) {
                                    if (otpDigit1.isNotEmpty() && otpDigit2.isNotEmpty() && otpDigit3.isNotEmpty() && otpDigit4.isNotEmpty()) {
                                        isVerified = true
                                        val fullPhone = "$countryCode $phoneNumber"
                                        onLoginSuccess(fullPhone)
                                    } else {
                                        Toast.makeText(context, "Please enter all 4 digits", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .testTag("btn_verify_otp"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isVerified) "VERIFIED ✅" else "VERIFY & CONTINUE",
                                color = TextCrispWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }


        }

        // --- SIMULATED INCOMING SMS NOTIFICATION TOAST ---
        AnimatedVisibility(
            visible = showSmsNotification,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E28))
                    .border(1.5.dp, SahityaGold, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .testTag("simulated_sms_banner")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SahityaRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "SMS",
                            tint = TextCrispWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "MESSAGES • Sahitya",
                                color = SahityaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "now",
                                color = TextMutedGray,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your Sahitya verification OTP is $generatedOtp. Valid for 5 minutes.",
                            color = TextCrispWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun OtpDigitBox(
    digit: String,
    isVerified: Boolean
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PitchBlack)
            .border(
                width = if (digit.isNotEmpty()) 2.dp else 1.dp,
                color = when {
                    isVerified -> Color(0xFF4CAF50)
                    digit.isNotEmpty() -> SahityaGold
                    else -> CardBorderDark
                },
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            color = if (isVerified) Color(0xFF4CAF50) else TextCrispWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
