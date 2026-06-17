package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engine.ShadowViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                // Keep the premium splash visible for 3200ms
                delay(3200)
                showSplash = false
            }

            val viewModel: ShadowViewModel = viewModel()
            val isDark by viewModel.isDarkMode.collectAsState()

            // Main Application Framework Custom Theme
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDark) Color(0xFF050505) else Color(0xFFF4F5F7)
                ) {
                    AnimatedContent(
                        targetState = showSplash,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(600, easing = LinearOutSlowInEasing)) togetherWith
                                    fadeOut(animationSpec = tween(500, easing = FastOutLinearInEasing))
                        },
                        label = "AppStageTransition"
                    ) { isSplashState ->
                        if (isSplashState) {
                            ShadowSplashScreen(isDark = isDark)
                        } else {
                            ShadowCalculatorScreen(viewModel = viewModel, isDark = isDark)
                        }
                    }
                }
            }
        }
    }
}

// ================= PRIVATE COMPOSABLES: SPLASH SCREEN =================

@Composable
fun AnimatedShadowLetters(isDark: Boolean) {
    val uppercaseWord = "SHADOW"
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        uppercaseWord.forEachIndexed { index, char ->
            var activeState by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(index * 130L)
                activeState = true
            }

            val scale by animateFloatAsState(
                targetValue = if (activeState) 1f else 0.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "scale"
            )

            val rotation by animateFloatAsState(
                targetValue = if (activeState) 0f else -45f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "rotation"
            )

            val alpha by animateFloatAsState(
                targetValue = if (activeState) 1f else 0f,
                animationSpec = tween(durationMillis = 500, easing = EaseInOutQuad),
                label = "alpha"
            )

            // Deluxe gold gradient brush for letters
            val goldBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFDF7D),
                    Color(0xFFD4AF37),
                    Color(0xFFAA7C11),
                    Color(0xFFD4AF37),
                    Color(0xFFFFDF7D)
                )
            )

            Text(
                text = char.toString(),
                style = TextStyle(
                    brush = goldBrush,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        rotationZ = rotation,
                        alpha = alpha
                    )
            )
        }
    }
}

@Composable
fun ShadowSplashScreen(isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashPulse")
    
    // Smooth gold shimmer animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val fadeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FadeAlpha"
    )

    // Deluxe gold gradient brush
    val goldBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFE8B5),
            Color(0xFFD4AF37),
            Color(0xFFAA7C11),
            Color(0xFFD4AF37),
            Color(0xFFFFE8B5)
        )
    )

    // Dark luxury theme colors (Void Black for Frosted glass contrast)
    val bgGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF040405), Color(0xFF0A0B0D)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE2E6EC)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // SC Luxury circular monogram emblem pulsating elegantly
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .border(BorderStroke(2.dp, goldBrush), CircleShape)
                    .background(if (isDark) Color(0x0CFFFFFF) else Color(0x33BCD4E6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SR",
                    style = TextStyle(
                        brush = goldBrush,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.offset(x = 1.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Brand new high fidelity staggered letters animation for word "SHADOW"
            AnimatedShadowLetters(isDark = isDark)

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle that fades in elements nicely
            var showSubtitle by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(950) // appear right after letters start animating
                showSubtitle = true
            }

            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(animationSpec = tween(800)) + expandVertically(),
                exit = fadeOut()
            ) {
                Text(
                    text = "CALCULATION ENGINE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 6.sp,
                    color = if (isDark) Color(0xFFD4AF37).copy(alpha = 0.8f) else Color(0xFF8A6D1C),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Premium pulsing developer identification check
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = fadeAlpha }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0x1AD4AF37), CircleShape)
                            .border(BorderStroke(1.dp, Color(0xFFD4AF37)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD4AF37)
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.made_by),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E2022),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "STABLE SECURED ENVIRONMENT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ================= PRIVATE COMPOSABLES: CALCULATOR SCREEN =================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShadowCalculatorScreen(viewModel: ShadowViewModel, isDark: Boolean) {
    val expr by viewModel.expression.collectAsState()
    val res by viewModel.result.collectAsState()
    val previewRes by viewModel.previewResult.collectAsState()
    val radMode by viewModel.isRadians.collectAsState()
    val memoryVal by viewModel.memory.collectAsState()
    val isSecretVisible by viewModel.isSecretVisible.collectAsState()
    val historyList by viewModel.history.collectAsState()

    var showHistoryPanel by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showIphoneNotification by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(600) // Brief suspension after entering screen
        showIphoneNotification = true
        delay(6000) // Stay visible for 6 seconds
        showIphoneNotification = false
    }

    // Premium Theme Brushes
    val goldBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFE8B5),
            Color(0xFFD4AF37),
            Color(0xFFAA7C11),
            Color(0xFFD4AF37),
            Color(0xFFFFE8B5)
        )
    )

    val silverBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE5E4E2),
            Color(0xFFB0B3B6),
            Color(0xFFFFFFFF),
            Color(0xFFB0B3B6),
            Color(0xFFE5E4E2)
        )
    )

    val textBaseColor = if (isDark) Color.White else Color(0xFF1C1E20)
    
    // Background space-gradient designed specifically to amplify Frosted Glass cards
    val containerBgGrad = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF050505), Color(0xFF121316)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF3F4F6), Color(0xFFE2E6EC)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(containerBgGrad)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP HEADER NAVIGATION BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Brand: Logo SC + Title Column
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("logo_tap_area")
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFD4AF37), Color(0xFF8A6D1C))
                                )
                            )
                            .clickable(onClick = { viewModel.onLogoClicked() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SC",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Column {
                        Text(
                            text = "Shadow Calculation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6D1C),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "BY SHAHZAIB RAO",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )
                    }
                }

                // Right Controls: DEG/RAD, Theme Toggle, History Access
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // DEG/RAD Toggle Button (Frosted style)
                    Button(
                        onClick = { viewModel.toggleAngleUnit() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (radMode) {
                                if (isDark) Color(0x1AD4AF37) else Color(0x1F8A6D1C)
                            } else {
                                Color.Transparent
                            }
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = if (radMode) "RAD" else "DEG",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (radMode) {
                                if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6D1C)
                            } else textBaseColor
                        )
                    }

                    // Theme Toggle Action Button (Frosted style)
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier
                            .size(34.dp)
                            .border(BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)), CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.04f), CircleShape)
                    ) {
                        Text(
                            text = if (isDark) "☀" else "🌙",
                            fontSize = 13.sp,
                            color = textBaseColor
                        )
                    }

                    // Calculation Logs Trigger Button (Frosted style)
                    IconButton(
                        onClick = { showHistoryPanel = !showHistoryPanel },
                        modifier = Modifier
                            .testTag("history_button")
                            .size(34.dp)
                            .border(BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)), CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.04f), CircleShape)
                    ) {
                        Text(
                            text = "🕒",
                            fontSize = 13.sp,
                            color = textBaseColor
                        )
                    }
                }
            }

            // 2. PRIMARY CALCULATION DISPLAY (High-Fidelity Frosted Glass Panel)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.38f)
                    .padding(vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0x0CFFFFFF) else Color(0x3DFFFFFF)
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Quick Memory & Action Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (memoryVal != 0.0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color(0x14D4AF37) else Color(0x1F8A6D1C))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "M",
                                        color = if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6C11),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text(
                                        text = "= ${FormatMemoryOutput(memoryVal)}",
                                        color = textBaseColor,
                                        fontSize = 11.sp
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(1.dp))
                            }

                            // Auxiliary Clipboard and reset access
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (res.isNotEmpty() && res != "Error") {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(res))
                                            Toast.makeText(context, context.getString(R.string.copy_success), Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .testTag("copy_button")
                                            .size(28.dp)
                                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), CircleShape)
                                    ) {
                                        Text(
                                            text = "📋",
                                            fontSize = 11.sp,
                                            color = textBaseColor
                                        )
                                    }
                                }
                                if (expr.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.clearAll() },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), CircleShape)
                                    ) {
                                        Text(
                                            text = "✕",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)
                                        )
                                    }
                                }
                            }
                        }

                        // Expression Viewer Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 100.dp)
                                .horizontalScroll(scrollState),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = expr.ifEmpty { "0" },
                                fontSize = if (expr.length > 12) 28.sp else 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }

                        // Luxury glowing result view
                        Column(horizontalAlignment = Alignment.End) {
                            if (previewRes.isNotEmpty() && res.isEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = previewRes,
                                        fontSize = 18.sp,
                                        color = if (isDark) Color(0xFFD4AF37).copy(alpha = 0.75f) else Color(0xFF8A6D1C).copy(alpha = 0.75f),
                                        textAlign = TextAlign.End,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(3.dp)
                                            .background(if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6D1C), CircleShape)
                                    )
                                }
                            }
                            if (res.isNotEmpty()) {
                                Text(
                                    text = res,
                                    style = TextStyle(
                                        brush = if (res.startsWith("Error") || res.contains("Format")) {
                                            Brush.linearGradient(listOf(Color(0xFFE57373), Color(0xFFF44336)))
                                        } else {
                                            if (isDark) goldBrush else Brush.linearGradient(listOf(Color(0xFF8A6D1C), Color(0xFF5C4712)))
                                        },
                                        fontSize = if (res.length > 10) 36.sp else 48.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.End
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 3. RESPONSIVE FROSTED BUTTON KEYPAD PANEL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.56f)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val keyCols = 5
                    val keyRows = 8
                    val spacing = 6.dp
                    val btnWidth = (maxWidth - (spacing * (keyCols - 1))) / keyCols
                    val btnHeight = (maxHeight - (spacing * (keyRows - 1))) / keyRows

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        // ROW 1: MC, MR, M+, M-, !
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            LuxuryButton(txt = "MC", w = btnWidth, h = btnHeight, colorGroup = "M", isDark = isDark) { viewModel.handleMemory("MC") }
                            LuxuryButton(txt = "MR", w = btnWidth, h = btnHeight, colorGroup = "M", isDark = isDark) { viewModel.handleMemory("MR") }
                            LuxuryButton(txt = "M+", w = btnWidth, h = btnHeight, colorGroup = "M", isDark = isDark) { viewModel.handleMemory("M+") }
                            LuxuryButton(txt = "M-", w = btnWidth, h = btnHeight, colorGroup = "M", isDark = isDark) { viewModel.handleMemory("M-") }
                            LuxuryButton(txt = "!", w = btnWidth, h = btnHeight, colorGroup = "S", isDark = isDark) { viewModel.appendText("!") }
                        }

                        // ROW 2: sin, cos, tan, log, ln
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            LuxuryButton(txt = "sin", w = btnWidth, h = btnHeight, colorGroup = "F", isDark = isDark) { viewModel.appendFunction("sin") }
                            LuxuryButton(txt = "cos", w = btnWidth, h = btnHeight, colorGroup = "F", isDark = isDark) { viewModel.appendFunction("cos") }
                            LuxuryButton(txt = "tan", w = btnWidth, h = btnHeight, colorGroup = "F", isDark = isDark) { viewModel.appendFunction("tan") }
                            LuxuryButton(txt = "log", w = btnWidth, h = btnHeight, colorGroup = "F", isDark = isDark) { viewModel.appendFunction("log") }
                            LuxuryButton(txt = "ln", w = btnWidth, h = btnHeight, colorGroup = "F", isDark = isDark) { viewModel.appendFunction("ln") }
                        }

                        // ROW 3: ^, √, (, ), %
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            LuxuryButton(txt = "^", w = btnWidth, h = btnHeight, colorGroup = "S", isDark = isDark) { viewModel.appendText("^") }
                            LuxuryButton(txt = "√", w = btnWidth, h = btnHeight, colorGroup = "S", isDark = isDark) { viewModel.appendFunction("sqrt") }
                            LuxuryButton(txt = "(", w = btnWidth, h = btnHeight, colorGroup = "S", isDark = isDark) { viewModel.appendText("(") }
                            LuxuryButton(txt = ")", w = btnWidth, h = btnHeight, colorGroup = "S", isDark = isDark) { viewModel.appendText(")") }
                            LuxuryButton(txt = "%", w = btnWidth, h = btnHeight, colorGroup = "S", isDark = isDark) { viewModel.appendText("%") }
                        }

                        // ROW 4: π, e, Clear, Delete, Operator Divide
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            LuxuryButton(txt = "π", w = btnWidth, h = btnHeight, colorGroup = "C", isDark = isDark) { viewModel.appendText("π") }
                            LuxuryButton(txt = "e", w = btnWidth, h = btnHeight, colorGroup = "C", isDark = isDark) { viewModel.appendText("e") }
                            LuxuryButton(txt = "C", w = btnWidth, h = btnHeight, colorGroup = "D", isDark = isDark) { viewModel.clearAll() }
                            LuxuryButton(txt = "⌫", w = btnWidth, h = btnHeight, colorGroup = "D", isDark = isDark) { viewModel.deleteLast() }
                            LuxuryButton(txt = "÷", w = btnWidth, h = btnHeight, colorGroup = "O", isDark = isDark) { viewModel.appendText("÷") }
                        }

                        // ROW 5: 7, 8, 9, ×
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            LuxuryButton(txt = "7", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText("7") }
                            LuxuryButton(txt = "8", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText("8") }
                            LuxuryButton(txt = "9", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText("9") }
                            LuxuryButton(txt = "×", w = btnWidth, h = btnHeight, colorGroup = "O", isDark = isDark) { viewModel.appendText("×") }
                        }

                        // ROW 6: 4, 5, 6, −
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            LuxuryButton(txt = "4", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText("4") }
                            LuxuryButton(txt = "5", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText("5") }
                            LuxuryButton(txt = "6", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText("6") }
                            LuxuryButton(txt = "−", w = btnWidth, h = btnHeight, colorGroup = "O", isDark = isDark) { viewModel.appendText("−") }
                        }

                        // ROW 7: 1, 2, 3, +
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            LuxuryButton(txt = "1", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText("1") }
                            LuxuryButton(txt = "2", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText("2") }
                            LuxuryButton(txt = "3", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText("3") }
                            LuxuryButton(txt = "+", w = btnWidth, h = btnHeight, colorGroup = "O", isDark = isDark) { viewModel.appendText("+") }
                        }

                        // ROW 8: 0, ., copy / = (perfectly balanced split)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            LuxuryButton(txt = "0", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText("0") }
                            LuxuryButton(txt = ".", w = btnWidth, h = btnHeight, colorGroup = "N", isDark = isDark) { viewModel.appendText(".") }
                            // Copy button (as shown in Design HTML footer frame)
                            LuxuryButton(txt = "Copy", w = btnWidth, h = btnHeight, colorGroup = "C", isDark = isDark) {
                                if (res.isNotEmpty() && res != "Error") {
                                    clipboardManager.setText(AnnotatedString(res))
                                    Toast.makeText(context, context.getString(R.string.copy_success), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No result to copy", Toast.LENGTH_SHORT).show()
                                }
                            }
                            // Custom math constants or placeholders matching column width
                            LuxuryButton(txt = "%", w = btnWidth, h = btnHeight, colorGroup = "S", isDark = isDark) { viewModel.appendText("%") }
                            // Equals key shimmers with gold linear gradient
                            LuxuryButton(
                                txt = "=",
                                w = btnWidth,
                                h = btnHeight,
                                colorGroup = "E",
                                isDark = isDark
                            ) { viewModel.performCalculation() }
                        }
                    }
                }
            }

            // 4. HISTORICAL OR CERTIFICATION ENCRYPTED FOOTER (Matching Design HTML)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.alpha(0.6f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF00FF00), CircleShape)
                    )
                    Text(
                        text = "SECURE ENCRYPTED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = textBaseColor,
                        letterSpacing = 1.5.sp
                    )
                }

                // Decorative secure status level indicator
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.35f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6D1C))
                    )
                }
            }
        }

        // ================= OVERLAYS & SHEETS =================

        // 1. SECURE CALCULATION HISTORY LAYER Drawer
        AnimatedVisibility(
            visible = showHistoryPanel,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xEB0F1012) else Color(0xEBFAFBFD)
                ),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CALCULATION HISTORY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 1.5.sp,
                            color = if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6C11)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconButton(
                                onClick = {
                                    viewModel.clearDatabaseHistory()
                                    Toast.makeText(context, context.getString(R.string.history_cleared), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    text = "🗑️",
                                    fontSize = 16.sp
                                )
                            }
                            IconButton(onClick = { showHistoryPanel = false }, modifier = Modifier.size(32.dp)) {
                                Text(
                                    text = "✕",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textBaseColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (historyList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Secure local log is empty",
                                fontSize = 13.sp,
                                color = textBaseColor.copy(alpha = 0.5f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(historyList) { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDark) Color(0x19FFFFFF) else Color(0x0FFFFFFF))
                                        .border(BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.04f)), RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.useHistoryItem(item.expression)
                                            showHistoryPanel = false
                                        }
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = item.expression,
                                        fontSize = 14.sp,
                                        color = textBaseColor.copy(alpha = 0.6f),
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "= " + item.result,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6C11),
                                        textAlign = TextAlign.End,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // iOS pop-up notification banner
        AnimatedVisibility(
            visible = showIphoneNotification,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(600, easing = FastOutLinearInEasing)
            ) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 12.dp, end = 12.dp)
                .widthIn(max = 440.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showIphoneNotification = false },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xF21C1C1E) else Color(0xF2F2F2F7)
                ),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFD4AF37), Color(0xFF8A6D1C))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontFamily = FontFamily.Serif
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "SYSTEM ACCESS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6D1C),
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(if (isDark) Color.White.copy(0.4f) else Color.Black.copy(0.4f), CircleShape)
                                )
                                Text(
                                    text = "now",
                                    fontSize = 10.sp,
                                    color = if (isDark) Color.White.copy(0.5f) else Color.Black.copy(0.5f),
                                    fontFamily = FontFamily.Default
                                )
                            }
                            
                            Text(
                                text = "Made by Shahzaib Rao",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color.White else Color(0xFF1C1C1E),
                                fontFamily = FontFamily.Default
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF34C759), CircleShape)
                        )
                        Text(
                            text = "Active",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f)
                        )
                    }
                }
            }
        }

        // 2. HIDDEN PRIVACY & CERTIFICATION DRAWER (Activated 5x logo tap)
        if (isSecretVisible) {
            Dialog(onDismissRequest = { viewModel.closeSecretSection() }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(max = 480.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xEB0D0E10) else Color(0xEBFAFBFD)
                    ),
                    border = BorderStroke(1.5.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x11D4AF37)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔒",
                                fontSize = 32.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "SECURITY CREDENTIALS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 2.sp,
                            color = if (isDark) Color(0xFFD4AF37) else Color(0xFF4A4E54)
                        )

                        Text(
                            text = "Designed by Shahzaib",
                            fontSize = 12.sp,
                            color = textBaseColor.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Privacy Policy Block
                        Text(
                            text = stringResource(id = R.string.privacy_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textBaseColor,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(id = R.string.privacy_content),
                            fontSize = 12.sp,
                            color = textBaseColor.copy(0.81f),
                            textAlign = TextAlign.Justify,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Terms of Service Block
                        Text(
                            text = stringResource(id = R.string.terms_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textBaseColor,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(id = R.string.terms_content),
                            fontSize = 12.sp,
                            color = textBaseColor.copy(0.81f),
                            textAlign = TextAlign.Justify,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Close button
                        Button(
                            onClick = { viewModel.closeSecretSection() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6C11)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.close),
                                color = if (isDark) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= TACTILE PREMIUM BUTTON COMPOSABLE =================

@Composable
fun LuxuryButton(
    txt: String,
    w: androidx.compose.ui.unit.Dp,
    h: androidx.compose.ui.unit.Dp,
    colorGroup: String, // N=Digit, O=Operator, M=Memory, F=Trig/Log, S=Misc Scientific, D=Delete, E=Equals, C=Constants
    isDark: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth spring tactile scaling feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ButtonPressTactility"
    )

    // Deluxe colors depending on category grouping and light/dark theme
    val containerColor = when (colorGroup) {
        "N" -> if (isDark) Color(0x0CFFFFFF) else Color(0x33FFFFFF) // Digits
        "O" -> if (isDark) Color(0x0FFFFFFF) else Color(0x44FFFFFF) // Operators
        "E" -> Color.Transparent // Shimmers with gold/silver brush
        "D" -> if (isDark) Color(0x1FFFFFFF) else Color(0x55FFFFFF) // Clear / Delete
        "M" -> if (isDark) Color(0x06FFFFFF) else Color(0x1F000000) // Memory buttons
        "F" -> if (isDark) Color(0x0CFFFFFF) else Color(0x33FFFFFF) // Trig / Log
        else -> if (isDark) Color(0x0CFFFFFF) else Color(0x33FFFFFF) // Scientific functions & Constants
    }

    val contentColor = when (colorGroup) {
        "N" -> if (isDark) Color(0xFFEBEBEB) else Color(0xFF1E2022) // White/80 or Black equivalent
        "O" -> if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6D1C) // Gold theme colors
        "E" -> Color.Black // Contrast text over gold gradient
        "D" -> {
            if (txt == "C") {
                if (isDark) Color(0xFFC0C0C0) else Color(0xFF5A5F66) // Silver matching spec
            } else {
                if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828) // Delete red accent
            }
        }
        "M" -> if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
        "F" -> if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f) // serif italic white/80
        "C" -> if (isDark) Color(0xFFC0C0C0) else Color(0xFF5A5F66) // Silver logic
        else -> if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
    }

    // Special equals gold gradient shader (Matching SC branding)
    val equalBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFD4AF37), Color(0xFF8A6D1C))
    )

    val borderStroke = when (colorGroup) {
        "E" -> BorderStroke(1.dp, if (isDark) Color(0xFFD4AF37) else Color(0xFF8A6D1C))
        "C" -> BorderStroke(1.dp, if (isDark) Color(0xFFC0C0C0).copy(alpha = 0.2f) else Color(0x15000000))
        "D" -> {
            if (txt == "C") {
                BorderStroke(1.dp, if (isDark) Color(0xFFC0C0C0).copy(alpha = 0.2f) else Color(0x15000000))
            } else {
                BorderStroke(1.dp, if (isDark) Color(0xFFFF5252).copy(alpha = 0.2f) else Color(0x1FFF5252))
            }
        }
        else -> BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f))
    }

    Box(
        modifier = Modifier
            .size(width = w, height = h)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .then(
                if (colorGroup == "E") {
                    Modifier.background(brush = equalBrush, shape = RoundedCornerShape(16.dp))
                } else {
                    Modifier.background(color = containerColor, shape = RoundedCornerShape(16.dp))
                }
            )
            .then(
                if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(16.dp)) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        val fontStyleFamily = when (colorGroup) {
            "F" -> FontFamily.Serif
            "N" -> FontFamily.SansSerif
            else -> FontFamily.Default
        }
        Text(
            text = txt,
            fontSize = if (txt.length > 2) 14.sp else 21.sp,
            fontWeight = if (colorGroup == "N") FontWeight.Bold else FontWeight.ExtraBold,
            fontFamily = fontStyleFamily,
            fontStyle = if (colorGroup == "F") FontStyle.Italic else FontStyle.Normal,
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}

// Safely format memory output decimals
private fun FormatMemoryOutput(v: Double): String {
    val longVal = v.toLong()
    val res = if (v == longVal.toDouble()) {
        longVal.toString()
    } else {
        String.format("%.4f", v)
    }
    return if (res.length > 8) res.substring(0, 8) + ".." else res
}
