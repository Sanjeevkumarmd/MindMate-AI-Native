package com.performex.mindmatenative.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.performex.mindmatenative.ui.theme.*
import com.performex.mindmatenative.utils.ChatMessage
import com.performex.mindmatenative.utils.GeminiService
import com.performex.mindmatenative.utils.PdfUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uri: Uri?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    
    var pdfBytes by remember { mutableStateOf<ByteArray?>(null) }
    var pdfText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(ChatMessage("bot", "Hi! I've read your PDF. Ask me anything about it! \uD83E\uDDE0"))) }
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var isInitialLoading by remember { mutableStateOf(true) }

    val sendMessage: (String?) -> Unit = { overrideMessage ->
        val messageToSend = overrideMessage ?: inputText
        if (messageToSend.isNotBlank() && !isTyping) {
            if (overrideMessage == null) inputText = ""
            
            // If it's a retry, we remove the last error message from the list first
            if (overrideMessage != null && messages.lastOrNull()?.isError == true) {
                messages = messages.dropLast(1)
            } else {
                messages = messages + ChatMessage("user", messageToSend)
            }
            
            isTyping = true
            
            coroutineScope.launch {
                try {
                    val response = GeminiService.chatWithText(messageToSend, pdfText, pdfBytes)
                    messages = messages + ChatMessage("bot", response)
                } catch (e: Exception) {
                    messages = messages + ChatMessage("bot", "Oops! Let's try that again. (${e.message})", isError = true)
                } finally {
                    isTyping = false
                    try {
                        scrollState.animateScrollToItem(messages.size - 1)
                    } catch (e: Exception) {}
                }
            }
        }
    }

    LaunchedEffect(uri) {
        if (uri != null) {
            try {
                pdfBytes = PdfUtils.getPdfBytes(context, uri)
            } catch (e: Exception) {
                messages = messages + ChatMessage("bot", "Error loading PDF: ${e.message}")
            }
        }
        isInitialLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LivingAiIcon()
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Chat with PDF", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ChatInput(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = sendMessage,
                isTyping = isTyping
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isInitialLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
            } else {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(
                            message = msg,
                            onRetry = {
                                val lastUserMsg = messages.take(messages.indexOf(msg)).lastOrNull { it.role == "user" }
                                lastUserMsg?.let { sendMessage(it.content) }
                            }
                        )
                    }
                    if (isTyping) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LivingAiIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "LivingPulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraScale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraAlpha"
    )

    val colorPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ColorPulse"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
        // Outer Aura
        Canvas(modifier = Modifier.size(32.dp).graphicsLayer {
            scaleX = auraScale
            scaleY = auraScale
            alpha = auraAlpha
        }) {
            drawCircle(color = AccentGreen)
        }
        
        // Inner Glow
        Canvas(modifier = Modifier.size(32.dp).graphicsLayer {
            scaleX = scale
            scaleY = scale
        }) {
            drawCircle(
                color = AccentGreen.copy(alpha = 0.2f * colorPulse)
            )
        }
        
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = AccentGreen.copy(alpha = colorPulse)
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onRetry: () -> Unit) {
    val isUser = message.role == "user"
    val isError = message.isError
    var displayedText by remember { mutableStateOf(if (isUser || isError) message.content else "") }

    LaunchedEffect(message.content) {
        if (!isUser) {
            // Typewriter effect for bot answers
            for (i in message.content.indices) {
                displayedText = message.content.substring(0, i + 1)
                delay(10) // Fast typewriter
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isError) MaterialTheme.colorScheme.errorContainer else if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            tonalElevation = 2.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column {
                Text(
                    text = if (isUser) message.content else displayedText,
                    modifier = Modifier.padding(12.dp),
                    color = if (isError) MaterialTheme.colorScheme.onErrorContainer else if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                
                if (isError) {
                    Divider(color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f))
                    TextButton(
                        onClick = onRetry,
                        modifier = Modifier.align(Alignment.End).padding(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: (String?) -> Unit,
    isTyping: Boolean
) {
    Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Ask about the PDF...") },
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onSend(null) },
                enabled = text.isNotBlank() && !isTyping,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (text.isNotBlank() && !isTyping) PrimaryBlue else Color.LightGray)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "Thinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Row(
        modifier = Modifier
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(16.dp).graphicsLayer { this.alpha = alpha },
            tint = AccentGreen
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("MindMate is thinking...", fontSize = 12.sp, color = Color.Gray)
    }
}
