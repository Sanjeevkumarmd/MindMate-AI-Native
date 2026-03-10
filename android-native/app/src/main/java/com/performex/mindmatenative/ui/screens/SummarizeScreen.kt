package com.performex.mindmatenative.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.performex.mindmatenative.ui.theme.BackgroundBeige
import com.performex.mindmatenative.ui.theme.PrimaryBlue
import com.performex.mindmatenative.ui.theme.TextDark
import com.performex.mindmatenative.utils.GeminiService
import com.performex.mindmatenative.utils.PdfUtils
import com.performex.mindmatenative.data.HistoryItem
import com.performex.mindmatenative.data.HistoryType
import com.performex.mindmatenative.utils.StorageManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummarizeScreen(
    uri: Uri?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var summaryText by remember { mutableStateOf("") }
    var displayedSummary by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCopied by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(uri, retryTrigger) {
        if (uri == null) {
            errorMessage = "No file selected."
            isLoading = false
            return@LaunchedEffect
        }
        
        try {
            val pdfBytes = PdfUtils.getPdfBytes(context, uri)
            summaryText = GeminiService.summarizeText(null, pdfBytes)
            
            // Save to History
            StorageManager.saveHistoryItem(
                HistoryItem(
                    type = HistoryType.SUMMARY,
                    title = "Summary: ${uri.lastPathSegment ?: "PDF"}",
                    content = summaryText
                )
            )
        } catch (e: Exception) {
            errorMessage = e.message ?: "An unknown error occurred."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(summaryText) {
        if (summaryText.isNotEmpty()) {
            for (i in summaryText.indices) {
                displayedSummary = summaryText.substring(0, i + 1)
                kotlinx.coroutines.delay(5) // Fast typewriter
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary \uD83D\uDCDD", fontWeight = FontWeight.Bold) },
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
                    .padding(horizontal = 24.dp)
            ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Reading your document and thinking...", color = MaterialTheme.colorScheme.onBackground)
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    ) {
                        Text(
                            text = "Error: $errorMessage",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
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
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Again")
                    }
                }
            } else {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { 100 },
                        animationSpec = tween(500)
                    ) + fadeIn(tween(500))
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "\uD83C\uDDFA\uD83C\uDDF8 English Summary",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row {
                                        IconButton(onClick = {
                                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Summary", summaryText)
                                            clipboardManager.setPrimaryClip(clip)
                                            isCopied = true
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(2000)
                                                isCopied = false
                                            }
                                        }) {
                                            Icon(
                                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        IconButton(onClick = {
                                            val sendIntent: Intent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "MindMate AI Summary:\n\n$summaryText")
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, null)
                                            context.startActivity(shareIntent)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = displayedSummary,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
