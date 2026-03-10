package com.performex.mindmatenative.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.performex.mindmatenative.data.HistoryItem
import com.performex.mindmatenative.data.UserProfile
import com.performex.mindmatenative.data.HistoryType

object StorageManager {
    private const val PREFS_NAME = "MindMatePrefs"
    private const val KEY_HISTORY = "history"
    private const val KEY_PROFILE = "profile"
    private const val KEY_API_KEY = "api_key"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveHistoryItem(item: HistoryItem) {
        val history = getHistory().toMutableList()
        history.add(0, item) // Newest first
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_HISTORY, json).apply()
        
        // Update profile stats
        updateProfileStats()
    }

    fun getHistory(): List<HistoryItem> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<HistoryItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getUserProfile(): UserProfile {
        val json = prefs.getString(KEY_PROFILE, null) ?: return UserProfile()
        return gson.fromJson(json, UserProfile::class.java)
    }

    fun saveUserProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        prefs.edit().putString(KEY_PROFILE, json).apply()
    }

    fun getApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key).apply()
    }

    private fun updateProfileStats() {
        val history = getHistory()
        val quizzes = history.filter { it.type == HistoryType.QUIZ }
        val totalSummaries = history.count { it.type == HistoryType.SUMMARY }
        val totalQuizzes = quizzes.size
        
        val avgScore = if (totalQuizzes > 0) {
            val scores = quizzes.mapNotNull { 
                if (it.score != null && it.total != null && it.total > 0) {
                    (it.score.toFloat() / it.total.toFloat()) * 100
                } else null 
            }
            if (scores.isNotEmpty()) scores.average().toFloat() else 0f
        } else 0f

        val currentProfile = getUserProfile()
        saveUserProfile(currentProfile.copy(
            totalSummaries = totalSummaries,
            totalQuizzes = totalQuizzes,
            avgScore = avgScore
        ))
    }
}
