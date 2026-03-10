package com.performex.mindmatenative.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.performex.mindmatenative.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf("") }
    val fullText = "MindMate AI"
    val tagline = "Your Intelligent Study Buddy \uD83E\uDDE0"

    val scale = animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(500)
        // Typewriter effect for "MindMate AI"
        for (i in fullText.indices) {
            displayedText = fullText.substring(0, i + 1)
            delay(100)
        }
        delay(1500)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Icon/Logo Placeholder
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale.value),
                tint = PrimaryBlue
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Animating App Name
            Text(
                text = displayedText,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 2.sp
            )

            // Fading Tagline
            AnimatedVisibility(
                visible = displayedText == fullText,
                enter = fadeIn(animationSpec = tween(1000)) + expandVertically()
            ) {
                Text(
                    text = tagline,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
