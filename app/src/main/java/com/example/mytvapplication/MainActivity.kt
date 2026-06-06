package com.example.mytvapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.mytvapplication.features.navigation.Screen
import com.example.mytvapplication.features.navigation.TvNavGraph
import com.example.mytvapplication.ui.theme.TvLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvLauncherTheme {
                val backStack = rememberNavBackStack(Screen.Dashboard)
                TvNavGraph(backStack = backStack)
            }
        }
    }
}
