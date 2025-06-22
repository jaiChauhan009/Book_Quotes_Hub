package com.example.book_quotes_hub

import android.os.Bundle
import android.window.SplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.book_quotes_hub.navigation.MainScreen
import com.example.book_quotes_hub.ui.theme.Book_Hub_Theme
import com.example.book_quotes_hub.view.SplashScreen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Book_Hub_Theme {
                BookHubApp()
            }
        }
    }
}


@Composable
fun BookHubApp() {
    val rootNavController = rememberNavController()

    NavHost(navController = rootNavController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(onSplashFinished = {
                rootNavController.navigate("main_app_screen") {
                    popUpTo("splash_screen") { inclusive = true } // Remove splash from back stack
                }
            })
        }
        composable("main_app_screen") {
            MainScreen(rootNavController = rootNavController)
        }
    }
}