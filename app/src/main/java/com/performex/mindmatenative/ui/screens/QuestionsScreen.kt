package com.performex.mindmatenative.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Refresh
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
import com.performex.mindmatenative.data.HistoryItem
import com.performex.mindmatenative.data.HistoryType
import com.performex.mindmatenative.utils.StorageManager
import kotlinx.coroutines.launch

@Composable
fun QuestionCard(
    index: Int,
    text: String,
    onCopy: () -> Unit
) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        for (i in text.indices) {
            displayedText = text.substring(0, i + 1)
            kotlinx.coroutines.delay(8)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PrimaryRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Q$index",
                        color = PrimaryRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Question",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = displayedText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionsScreen(
    uri: Uri?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var questions by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCopyToast by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(uri, retryTrigger) {
        if (uri == null) {
            errorMessage = "No file selected."
            isLoading = false
            return@LaunchedEffect
        }
        
        try {
            val pdfBytes = PdfUtils.getPdfBytes(context, uri)
            questions = GeminiService.generateQuestions(null, pdfBytes)
            
            // Save to History
            StorageManager.saveHistoryItem(
                HistoryItem(
                    type = HistoryType.QUESTIONS,
                    title = "Exam Questions: ${uri.lastPathSegment ?: "PDF"}",
                    content = questions.joinToString("\n")
                )
            )
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Questions \uD83D\uDCDC", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (questions.isNotEmpty()) {
                        IconButton(onClick = {
                            val fullText = questions.joinToString("\n\n")
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Generated Questions from MindMate AI:\n\n$fullText")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share All")
                        }
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
            ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analyzing PDF for important topics...", color = MaterialTheme.colorScheme.onBackground)
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: $errorMessage",
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        // Import not present, using Icons.Default.Check as placeholder or I should add import
                        // Actually I will just use Icons.Default.Check and the text "Try Again"
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Again")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(questions) { index, question ->
                        QuestionCard(
                            index = index + 1,
                            text = question,
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Question", question))
                                showCopyToast = true
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(2000)
                                    showCopyToast = false
                                }
                            }
                        )
                    }
                }
            }
            
            // Pop-up Notification for Copy
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp),
                visible = showCopyToast,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
                exit = fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 8.dp
                ) {
                    Text(
                        "Copied to clipboard!",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
}
