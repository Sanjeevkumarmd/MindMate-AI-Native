package com.performex.mindmatenative.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.performex.mindmatenative.ui.theme.*
import com.performex.mindmatenative.utils.GeminiService
import com.performex.mindmatenative.utils.PdfUtils
import com.performex.mindmatenative.utils.QuizQuestion
import com.performex.mindmatenative.data.HistoryItem
import com.performex.mindmatenative.data.HistoryType
import com.performex.mindmatenative.utils.StorageManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    uri: Uri?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showCorrectAnswer by remember { mutableStateOf(false) }
    var displayedQuestion by remember { mutableStateOf("") }

    val loadQuiz = suspend {
        isLoading = true
        errorMessage = null
        try {
            if (uri == null) {
                errorMessage = "No file selected."
            } else {
                val pdfBytes = PdfUtils.getPdfBytes(context, uri)
                questions = GeminiService.generateQuiz(null, pdfBytes)
                if (questions.isEmpty()) {
                    errorMessage = "AI couldn't generate questions from this PDF."
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(uri) {
        loadQuiz()
    }

    LaunchedEffect(currentIndex, questions) {
        if (questions.isNotEmpty() && currentIndex < questions.size) {
            val fullText = questions[currentIndex].question
            for (i in fullText.indices) {
                displayedQuestion = fullText.substring(0, i + 1)
                kotlinx.coroutines.delay(10)
            }
        }
    }
    LaunchedEffect(isFinished) {
        if (isFinished) {
            StorageManager.saveHistoryItem(
                HistoryItem(
                    type = HistoryType.QUIZ,
                    title = "Quiz: ${uri?.lastPathSegment ?: "PDF"}",
                    content = "Score: $score/${questions.size}",
                    score = score,
                    total = questions.size
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Quiz \uD83C\uDFAF", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    CircularProgressIndicator(color = PrimaryYellow)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generating your personalized quiz...", color = MaterialTheme.colorScheme.onBackground)
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
                            coroutineScope.launch {
                                loadQuiz()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Again")
                    }
                }
            } else if (isFinished) {
                QuizResult(score, questions.size, onNavigateBack)
            } else if (questions.isNotEmpty()) {
                val question = questions[currentIndex]
                
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Progress
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PrimaryYellow,
                        trackColor = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "Question ${currentIndex + 1}/${questions.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = displayedQuestion,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 32.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        question.options.forEachIndexed { index, option ->
                            OptionCard(
                                text = option,
                                isSelected = selectedOption == index,
                                isCorrect = showCorrectAnswer && index == question.correctIndex,
                                isWrong = showCorrectAnswer && selectedOption == index && index != question.correctIndex,
                                onClick = {
                                    if (!showCorrectAnswer) {
                                        selectedOption = index
                                    }
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            if (!showCorrectAnswer) {
                                if (selectedOption != null) {
                                    showCorrectAnswer = true
                                    if (selectedOption == question.correctIndex) {
                                        score++
                                    }
                                }
                            } else {
                                if (currentIndex < questions.size - 1) {
                                    currentIndex++
                                    selectedOption = null
                                    showCorrectAnswer = false
                                    displayedQuestion = "" // Reset for next
                                } else {
                                    isFinished = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = selectedOption != null,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (!showCorrectAnswer) "Check Answer" else "Next Question",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun OptionCard(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected && isCorrect -> AccentGreen.copy(alpha = 0.3f)
        isSelected && !isCorrect -> PrimaryRed.copy(alpha = 0.2f)
        isSelected -> PrimaryBlue.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.background
    }
    val borderColor = when {
        isSelected && isCorrect -> AccentGreen
        isSelected && !isCorrect -> PrimaryRed
        isSelected -> PrimaryBlue
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (isSelected || isCorrect || isWrong) borderColor else Color.LightGray.copy(alpha = 0.3f),
                        RoundedCornerShape(6.dp)
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                color = if (isCorrect || isWrong) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun QuizResult(score: Int, total: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83C\uDF8A Quiz Complete! \uD83C\uDF8A",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Your Score",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        Text(
            text = "$score / $total",
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryYellow
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Go Home")
        }
    }
}
