package com.performex.mindmatenative.data

import java.util.*

enum class HistoryType {
    SUMMARY, QUIZ, QUESTIONS, FLASHCARDS
}

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val type: HistoryType,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val content: String, // JSON payload or raw text
    val score: Int? = null,
    val total: Int? = null
)

data class UserProfile(
    val name: String = "Studious Learner",
    val avatarEmoji: String = "🎓",
    val totalSummaries: Int = 0,
    val totalQuizzes: Int = 0,
    val avgScore: Float = 0f
)
