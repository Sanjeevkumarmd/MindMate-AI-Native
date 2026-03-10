package com.performex.mindmatenative.utils

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object GeminiService {
    
    // Default API Key
    private const val DEFAULT_API_KEY = "AIzaSyD2BI1KacfQXJxhP0nB-mf8NCzZIVbaBDE"
    
    private var _generativeModel: GenerativeModel? = null
    private var _lastApiKey: String? = null

    private fun getGenerativeModel(): GenerativeModel {
        val apiKey = StorageManager.getApiKey() ?: DEFAULT_API_KEY
        if (_generativeModel == null || apiKey != _lastApiKey) {
            _lastApiKey = apiKey
            _generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )
        }
        return _generativeModel!!
    }

    private const val MAX_RETRIES = 3
    private const val BASE_DELAY_MS = 2000L

    private suspend fun <T> withRetry(block: suspend () -> T): T {
        var lastException: Exception? = null
        for (i in 0 until MAX_RETRIES) {
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                val errorMsg = e.message ?: ""
                if (errorMsg.contains("429") || errorMsg.contains("quota") || errorMsg.contains("exhausted")) {
                    if (i == MAX_RETRIES - 1) {
                        throw Exception("MindMate is catching its breath! The free-tier limit was reached. Please wait a moment or update your API Key in Profile.")
                    }
                    val delayTime = BASE_DELAY_MS * Math.pow(2.0, i.toDouble()).toLong()
                    kotlinx.coroutines.delay(delayTime)
                } else {
                    throw e
                }
            }
        }
        throw lastException ?: Exception("Max retries reached")
    }

    private const val MAX_INPUT_CHARS = 8000

    private fun truncateInput(text: String?): String {
        if (text != null && text.length > MAX_INPUT_CHARS) {
            return text.substring(0, MAX_INPUT_CHARS) + "\n\n[...content truncated for efficiency]"
        }
        return text ?: ""
    }

    suspend fun summarizeText(text: String?, pdfBytes: ByteArray? = null): String = withRetry {
        withContext(Dispatchers.IO) {
            val prompt = "Summarize concisely (bullet points/headings). STRICT: Use ONLY this content."
            
            val response = if (pdfBytes != null) {
                getGenerativeModel().generateContent(content {
                    blob("application/pdf", pdfBytes)
                    text(prompt)
                })
            } else {
                val safeText = truncateInput(text)
                getGenerativeModel().generateContent("$prompt\n\n$safeText")
            }
            return@withContext response.text ?: ""
        }
    }

    suspend fun generateQuiz(text: String?, pdfBytes: ByteArray? = null): List<QuizQuestion> = withRetry {
        withContext(Dispatchers.IO) {
            val prompt = "Quiz Master. 10 MCQs based ONLY on content. Format: JSON array [{\"question\": \"...\", \"options\": [\"A\",\"B\",\"C\",\"D\"], \"correctIndex\": 0}]"

            val response = if (pdfBytes != null) {
                getGenerativeModel().generateContent(content {
                    blob("application/pdf", pdfBytes)
                    text(prompt)
                })
            } else {
                val safeText = truncateInput(text)
                getGenerativeModel().generateContent("$prompt\n\nContent:\n$safeText")
            }
            
            val rawText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: "[]"
            
            val questions = mutableListOf<QuizQuestion>()
            try {
                val jsonArray = JSONArray(rawText)
                for (i in 0 until jsonArray.length()) {
                    val qObj = jsonArray.getJSONObject(i)
                    val optionsArray = qObj.getJSONArray("options")
                    val options = List(optionsArray.length()) { optionsArray.getString(it) }
                    questions.add(
                        QuizQuestion(
                            question = qObj.getString("question"),
                            options = options,
                            correctIndex = qObj.getInt("correctIndex")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext questions
        }
    }

    suspend fun generateQuestions(text: String?, pdfBytes: ByteArray? = null): List<String> = withRetry {
        withContext(Dispatchers.IO) {
            val prompt = "Study Buddy. 10 exam questions based ONLY on content. Format: JSON array of strings [\"Q1?\", \"Q2?\", ...]"

            val response = if (pdfBytes != null) {
                getGenerativeModel().generateContent(content {
                    blob("application/pdf", pdfBytes)
                    text(prompt)
                })
            } else {
                val safeText = truncateInput(text)
                getGenerativeModel().generateContent("$prompt\n\nContent:\n$safeText")
            }
            val rawText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: "[]"
            
            val qs = mutableListOf<String>()
            try {
                val jsonArray = JSONArray(rawText)
                for (i in 0 until jsonArray.length()) {
                    qs.add(jsonArray.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext qs
        }
    }

    fun resetChat() {
        chatHistory.clear()
    }

    private var chatHistory = mutableListOf<ChatMessage>()

    suspend fun chatWithText(userMessage: String, pdfContext: String?, pdfBytes: ByteArray? = null): String = withRetry {
        withContext(Dispatchers.IO) {
            chatHistory.add(ChatMessage("user", userMessage))
            if (chatHistory.size > 20) chatHistory.removeAt(0)
 
            val historyContext = chatHistory.takeLast(6).joinToString("\n") { 
                "${if (it.role == "user") "Student" else "AI"}: ${it.content}" 
            }
 
            val prompt = "MindMate AI (Study Buddy). Respond concisely using ONLY provided content. History: $historyContext. Message: $userMessage"
 
            val response = if (pdfBytes != null) {
                getGenerativeModel().generateContent(content {
                    blob("application/pdf", pdfBytes)
                    text(prompt)
                })
            } else {
                val safeContext = truncateInput(pdfContext)
                getGenerativeModel().generateContent("$prompt\n\nContext:\n$safeContext")
            }
            val botResponse = response.text ?: ""
            chatHistory.add(ChatMessage("bot", botResponse))
            
            return@withContext botResponse
        }
    }

    suspend fun generateFlashcards(text: String?, pdfBytes: ByteArray? = null): List<Flashcard> = withRetry {
        withContext(Dispatchers.IO) {
            val prompt = "Study Buddy. 10 flashcards based ONLY on content. Format: JSON array [{\"front\": \"...\", \"back\": \"...\"}]"

            val response = if (pdfBytes != null) {
                getGenerativeModel().generateContent(content {
                    blob("application/pdf", pdfBytes)
                    text(prompt)
                })
            } else {
                val safeText = truncateInput(text)
                getGenerativeModel().generateContent("$prompt\n\nContent:\n$safeText")
            }
            val rawText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: "[]"
            
            val flashcards = mutableListOf<Flashcard>()
            try {
                val jsonArray = JSONArray(rawText)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    flashcards.add(
                        Flashcard(
                            front = obj.getString("front"),
                            back = obj.getString("back")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext flashcards
        }
    }
}

data class Flashcard(val front: String, val back: String)
data class ChatMessage(val role: String, val content: String, val isError: Boolean = false)
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
