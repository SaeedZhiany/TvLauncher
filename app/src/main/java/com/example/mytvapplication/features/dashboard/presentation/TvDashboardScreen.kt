package com.example.mytvapplication.features.dashboard.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytvapplication.features.dashboard.domain.MovieCard
import com.example.mytvapplication.features.dashboard.domain.MovieCategory

/**
 * Principal TV Dashboard Implementation
 *
 * Requirements Met:
 * 1) Focused-Only Image Loading: Images only load if focus settles for 1000ms.
 * 2) No images loaded on launch for visible items (unless focused).
 * 3) Strict network cancellation during fast D-pad browsing.
 */
@Composable
fun TvDashboardScreen(
    viewModel: TvDashboardViewModel,
    onMovieSelected: (String) -> Unit,
) {
    val categories by viewModel.categories.collectAsState()
    val focusedMovieId by viewModel.currentGlobalFocusedMovieId.collectAsState()
    val rowFocusMemory by viewModel.rowFocusMemory.collectAsState()
    val bitmaps by viewModel.bitmaps.collectAsState()

    DashboardContent(
        categories = categories,
        bitmaps = bitmaps,
        focusedMovieIdProvider = { focusedMovieId },
        rowFocusMemoryProvider = { rowFocusMemory },
        onFocusChanged = { catId, movId -> viewModel.onMovieFocused(catId, movId) },
        onMovieClick = onMovieSelected,
    )
}

@Composable
private fun DashboardContent(
    categories: List<MovieCategory>,
    bitmaps: Map<String, Bitmap>,
    focusedMovieIdProvider: () -> String,
    rowFocusMemoryProvider: () -> Map<String, String>,
    onFocusChanged: (String, String) -> Unit,
    onMovieClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        items(
            items = categories,
            key = { it.id },
        ) { category ->
            CategoryRow(
                category = category,
                bitmaps = bitmaps,
                lastFocusedMovieIdProvider = { rowFocusMemoryProvider()[category.id] },
                focusedMovieIdProvider = focusedMovieIdProvider,
                onFocusChanged = onFocusChanged,
                onMovieClick = onMovieClick,
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: MovieCategory,
    bitmaps: Map<String, Bitmap>,
    lastFocusedMovieIdProvider: () -> String?,
    focusedMovieIdProvider: () -> String,
    onFocusChanged: (String, String) -> Unit,
    onMovieClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 58.dp, bottom = 12.dp),
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold,
        )

        val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
        val rowState = rememberLazyListState()

        LaunchedEffect(category.id) {
            val lastId = lastFocusedMovieIdProvider()
            if (lastId != null) {
                val index = category.movies.indexOfFirst { it.id == lastId }
                if (index >= 0) {
                    rowState.scrollToItem(index)
                }
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .focusGroup()
                .focusProperties {
                    onEnter = {
                        val lastId = lastFocusedMovieIdProvider()
                        lastId?.let { focusRequesters[it] } ?: FocusRequester.Default
                    }
                },
            state = rowState,
            contentPadding = PaddingValues(horizontal = 58.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                items = category.movies,
                key = { it.id },
            ) { movie ->
                val fr = remember(movie.id) { FocusRequester() }
                focusRequesters[movie.id] = fr

                MovieCardItem(
                    movie = movie,
                    bitmap = bitmaps[movie.id],
                    focusRequester = fr,
                    isFocusedProvider = { focusedMovieIdProvider() == movie.id },
                    onFocusGained = { onFocusChanged(category.id, movie.id) },
                    onMovieClick = onMovieClick,
                )
            }
        }
    }
}

@Composable
private fun MovieCardItem(
    movie: MovieCard,
    bitmap: Bitmap?,
    focusRequester: FocusRequester,
    isFocusedProvider: () -> Boolean,
    onFocusGained: () -> Unit,
    onMovieClick: (String) -> Unit,
) {
    val isFocused = isFocusedProvider()

    Box(
        modifier = Modifier
            .width(240.dp)
            .aspectRatio(16f / 9f)
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused) {
                    onFocusGained()
                }
            }
            .focusable()
            .clickable { onMovieClick(movie.id) }
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 4.dp else 0.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .background(Color.DarkGray.copy(alpha = 0.5f)),
        contentAlignment = Alignment.BottomStart,
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}
