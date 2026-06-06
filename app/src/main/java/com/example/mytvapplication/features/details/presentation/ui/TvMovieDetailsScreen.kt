package com.example.mytvapplication.features.details.presentation.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage

@OptIn(UnstableApi::class)
@Composable
fun TvMovieDetailsScreen(
    movieTitle: String,
    thumbnail: String,
    videoUrl: String,
    onBackTriggered: () -> Unit,
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(value = false) }

    // Initialize ExoPlayer with proper configuration for TV
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = false // Wait for user to press play
        }
    }

    // Clean up player on dispose
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            // Real Video Player View
            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = true
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Start playback when entering playing state
            LaunchedEffect(Unit) {
                exoPlayer.play()
            }
        } else {
            // Thumbnail with Play Button in center
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark overlay
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

                // Movie Name at the top
                Text(
                    text = movieTitle,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                )

                // Large Play Button in Center
                IconButton(
                    onClick = { isPlaying = true },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(80.dp),
                    scale = IconButtonDefaults.scale(focusedScale = 1.2f),
                    colors = IconButtonDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.8f),
                        contentColor = Color.Black,
                        focusedContainerColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Back Button
                Button(
                    onClick = onBackTriggered,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                ) {
                    Text(text = "Back")
                }
            }
        }
    }
}
