package com.example.mytvapplication.features.details.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TvMovieDetailsScreen(
    movieId: String,
    onBackTriggered: () -> Unit,
) {
    var isBackFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Movie Details View", fontSize = 32.sp, color = Color.White)
            Text(text = "Target Resource ID: $movieId", fontSize = 18.sp, color = Color.LightGray)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 220.dp, height = 48.dp)
                    .onFocusChanged { isBackFocused = it.isFocused }
                    .focusable()
                    .background(
                        if (isBackFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    ).clickable { onBackTriggered() },
            ) {
                Text(
                    text = "Return to Dashboard",
                    color = if (isBackFocused) Color.White else Color.White,
                )
            }
        }
    }
}
