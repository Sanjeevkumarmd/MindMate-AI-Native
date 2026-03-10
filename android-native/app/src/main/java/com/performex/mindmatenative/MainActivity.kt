package com.performex.mindmatenative

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.performex.mindmatenative.ui.screens.*
import com.performex.mindmatenative.ui.theme.MindmateNativeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.performex.mindmatenative.utils.StorageManager.init(this)
        
        setContent {
            MindmateNativeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(onAnimationFinished = {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }
                        composable("home") {
                            HomeScreen(
                                onNavigateToSummarize = { uri -> 
                                    navController.navigate("summarize/${Uri.encode(uri.toString())}") 
                                },
                                onNavigateToQuiz = { uri -> 
                                    navController.navigate("quiz/${Uri.encode(uri.toString())}") 
                                },
                                onNavigateToQuestions = { uri -> 
                                    navController.navigate("questions/${Uri.encode(uri.toString())}") 
                                },
                                onNavigateToChat = { uri -> 
                                    navController.navigate("chat/${Uri.encode(uri.toString())}") 
                                },
                                onNavigateToFlashcards = { uri ->
                                    navController.navigate("flashcards/${Uri.encode(uri.toString())}")
                                },
                                onNavigateToProfile = {
                                    navController.navigate("profile")
                                },
                                onNavigateToHistory = {
                                    navController.navigate("history")
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToHistory = { navController.navigate("history") }
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("summarize/{uri}") { backStackEntry ->
                            val uriString = backStackEntry.arguments?.getString("uri")
                            val uri = uriString?.let { Uri.parse(Uri.decode(it)) }
                            SummarizeScreen(
                                uri = uri,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("quiz/{uri}") { backStackEntry ->
                            val uriString = backStackEntry.arguments?.getString("uri")
                            val uri = uriString?.let { Uri.parse(Uri.decode(it)) }
                            QuizScreen(
                                uri = uri,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("questions/{uri}") { backStackEntry ->
                            val uriString = backStackEntry.arguments?.getString("uri")
                            val uri = uriString?.let { Uri.parse(Uri.decode(it)) }
                            QuestionsScreen(
                                uri = uri,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("chat/{uri}") { backStackEntry ->
                            val uriString = backStackEntry.arguments?.getString("uri")
                            val uri = uriString?.let { Uri.parse(Uri.decode(it)) }
                            ChatScreen(
                                uri = uri,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("flashcards/{uri}") { backStackEntry ->
                            val uriString = backStackEntry.arguments?.getString("uri")
                            val uri = uriString?.let { Uri.parse(Uri.decode(it)) }
                            FlashcardScreen(
                                uri = uri,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
