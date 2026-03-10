package com.performex.mindmatenative.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.performex.mindmatenative.ui.theme.*
import com.performex.mindmatenative.utils.Flashcard
import com.performex.mindmatenative.utils.GeminiService
import com.performex.mindmatenative.utils.PdfUtils
import com.performex.mindmatenative.data.HistoryItem
import com.performex.mindmatenative.data.HistoryType
import com.performex.mindmatenative.utils.StorageManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    uri: Uri?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var flashcards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFlipped by remember { mutableStateOf(false) }
    var displayedBackText by remember { mutableStateOf("") }
    var retryTrigger by remember { mutableStateOf(0) }

    val rotation = animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(uri, retryTrigger) {
        if (uri == null) {
            errorMessage = "No file selected."
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val pdfBytes = PdfUtils.getPdfBytes(context, uri)
            flashcards = GeminiService.generateFlashcards(null, pdfBytes)

            // Save to History
            StorageManager.saveHistoryItem(
                HistoryItem(
                    type = HistoryType.FLASHCARDS,
                    title = "Flashcards: ${uri?.lastPathSegment ?: "PDF"}",
                    content = "Count: ${flashcards.size}"
                )
            )
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(isFlipped, currentIndex, flashcards) {
        if (isFlipped && flashcards.isNotEmpty()) {
            val fullText = flashcards[currentIndex].back
            for (i in fullText.indices) {
                displayedBackText = fullText.substring(0, i + 1)
                kotlinx.coroutines.delay(8)
            }
        } else {
            displayedBackText = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Flashcards \uD83D\uDCA1", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AccentGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Creating your flashcards...", color = MaterialTheme.colorScheme.onBackground)
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Oops! $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            errorMessage = null
                            isLoading = true
                            retryTrigger++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Again")
                    }
                }
            } else if (flashcards.isNotEmpty()) {
                val currentCard = flashcards[currentIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Card ${currentIndex + 1} of ${flashcards.size}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Flashcard with Flip Animation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .graphicsLayer {
                                rotationY = rotation.value
                                cameraDistance = 12f * density
                            }
                            .clickable { isFlipped = !isFlipped },
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotation.value <= 90f) {
                            // Front Side
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentCard.front,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                    val alpha by infiniteTransition.animateFloat(
                                        initialValue = 0.3f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "alpha"
                                    )
                                    
                                    Text(
                                        text = "Tap to flip",
                                        fontSize = 12.sp,
                                        color = Color.LightGray.copy(alpha = alpha),
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        } else {
                            // Back Side (Mirrored)
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f },
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.1f)),
                                border = androidx.compose.foundation.BorderStroke(2.dp, AccentGreen),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = displayedBackText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 28.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                if (currentIndex > 0) {
                                    currentIndex--
                                    isFlipped = false
                                }
                            },
                            enabled = currentIndex > 0,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                        }

                        Button(
                            onClick = {
                                if (currentIndex < flashcards.size - 1) {
                                    currentIndex++
                                    isFlipped = false
                                } else {
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.height(56.dp).weight(1f).padding(horizontal = 24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(if (currentIndex < flashcards.size - 1) "Next Card" else "Finish")
                        }

                        FilledTonalIconButton(
                            onClick = {
                                if (currentIndex < flashcards.size - 1) {
                                    currentIndex++
                                    isFlipped = false
                                }
                            },
                            enabled = currentIndex < flashcards.size - 1,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                        }
                    }
        }
    }
}
}
}
}
