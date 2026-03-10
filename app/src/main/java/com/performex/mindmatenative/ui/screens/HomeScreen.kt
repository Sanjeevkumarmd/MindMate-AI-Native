package com.performex.mindmatenative.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.performex.mindmatenative.ui.theme.*
import com.performex.mindmatenative.ui.components.DoodleBackground
import com.performex.mindmatenative.utils.PdfUtils
import kotlinx.coroutines.delay
import java.io.File

data class FeatureItem(val title: String, val description: String, val color: Color, val id: String)

@Composable
fun FeatureCard(
    title: String,
    description: String,
    baseColor: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GradientAnimation")
    val offset by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Offset"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            baseColor.copy(alpha = 0.8f),
            baseColor.copy(alpha = 0.6f),
            baseColor.copy(alpha = 0.8f),
            baseColor
        ),
        start = Offset(offset, 0f),
        end = Offset(offset + 600f, 600f),
        tileMode = TileMode.Mirror
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), ambientColor = baseColor, spotColor = baseColor),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (baseColor == PrimaryYellow || baseColor == AccentGreen) Color.Black else Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = (if (baseColor == PrimaryYellow || baseColor == AccentGreen) Color.Black else Color.White).copy(alpha = 0.8f)
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = (if (baseColor == PrimaryYellow || baseColor == AccentGreen) Color.Black else Color.White).copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBotPrompt(onNavigateToChat: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "LivingBot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )
    
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Float"
    )

    val colorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ColorPulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .graphicsLayer { translationY = floatOffset }
            .clickable { onNavigateToChat() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f * colorAlpha))
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryBlue.copy(alpha = colorAlpha),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Ask MindMate anything...",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 16.sp
                )
                Text(
                    text = "Unlock your document's secrets",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                Icons.Default.Send,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSummarize: (Uri) -> Unit,
    onNavigateToQuiz: (Uri) -> Unit,
    onNavigateToQuestions: (Uri) -> Unit,
    onNavigateToChat: (Uri) -> Unit,
    onNavigateToFlashcards: (Uri) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    var selectedFeature by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }
    
    val tips = listOf(
        "Tip: Upload clear PDFs for the best AI summaries! ✨",
        "Did you know? Flashcards are great for active recall! \uD83E\uDDE0",
        "Try the Chatbot to ask specific questions about your notes! \uD83D\uDDE3\uFE0F",
        "Master your exams by generating sample questions! \uD83D\uDCDD"
    )
    var currentTipIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        visible = true
        while (true) {
            delay(5000)
            currentTipIndex = (currentTipIndex + 1) % tips.size
        }
    }
    
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Cache the file immediately while we have permission
                PdfUtils.saveUriToTempFile(context, it)
                
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            when (selectedFeature) {
                "summarize" -> onNavigateToSummarize(it)
                "quiz" -> onNavigateToQuiz(it)
                "questions" -> onNavigateToQuestions(it)
                "chat" -> onNavigateToChat(it)
                "flashcards" -> onNavigateToFlashcards(it)
            }
        }
        selectedFeature = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MindMate AI \uD83D\uDE80", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Welcome Back!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "What would you like to learn today?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            val features = listOf(
                FeatureItem("Summarize", "Get instant, clear summaries of your notes.", PrimaryBlue, "summarize"),
                FeatureItem("Chat with PDF", "Ask questions and get answers from your document.", AccentGreen, "chat"),
                FeatureItem("AI Flashcards", "Master concepts with swipeable cards.", SecondaryPurple, "flashcards"),
                FeatureItem("Quick Quiz", "Test your knowledge with AI quizzes.", PrimaryYellow, "quiz"),
                FeatureItem("Generate Questions", "Get exam-worthy questions.", PrimaryRed, "questions")
            )

            items(features.size) { index ->
                val feature = features[index]
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500, delayMillis = 100 * (index + 1))) + 
                            slideInVertically(tween(500, delayMillis = 100 * (index + 1))) { 50 }
                ) {
                    FeatureCard(
                        title = feature.title,
                        description = feature.description,
                        baseColor = feature.color,
                        onClick = {
                            selectedFeature = feature.id
                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                        }
                    )
                }
            }

            // Living Chatbot Fragment
            item {
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800, delayMillis = 600)) + slideInVertically { 40 }
                ) {
                    ChatBotPrompt(onNavigateToChat = {
                        selectedFeature = "chat"
                        pdfPickerLauncher.launch(arrayOf("application/pdf"))
                    })
                }
            }

            // Encouragement Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800, delayMillis = 700))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            AnimatedContent(
                                targetState = tips[currentTipIndex],
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "TipAnimation"
                            ) { tipText ->
                                    Text(
                                        text = tipText,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Start
                                    )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
}
}
