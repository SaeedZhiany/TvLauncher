package com.example.mytvapplication.features.dashboard.presentation

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mytvapplication.features.dashboard.domain.MovieCard
import com.example.mytvapplication.features.dashboard.domain.MovieCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TvDashboardScreen(
    viewModel: TvDashboardViewModel,
    onMovieSelected: (MovieCard) -> Unit,
) {
    val categories by viewModel.categories.collectAsState()
    val focusedMovieId by viewModel.currentGlobalFocusedMovieId.collectAsState()
    val rowFocusMemory by viewModel.rowFocusMemory.collectAsState()
    val settledIds by viewModel.settledIds.collectAsState()

    val firstItemRequester = remember { FocusRequester() }
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && focusedMovieId.isEmpty()) {
            try {
                firstItemRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    DashboardContent(
        categories = categories,
        settledIds = settledIds,
        firstItemRequester = firstItemRequester,
        focusedMovieIdProvider = { focusedMovieId },
        rowFocusMemoryProvider = { rowFocusMemory },
        onFocusChanged = { catId, movId -> viewModel.onMovieFocused(catId, movId) },
        onMovieSettled = { viewModel.onMovieSettled(it) },
        onMovieClick = onMovieSelected,
    )
}

@Composable
private fun DashboardContent(
    categories: List<MovieCategory>,
    settledIds: Set<String>,
    firstItemRequester: FocusRequester,
    focusedMovieIdProvider: () -> String,
    rowFocusMemoryProvider: () -> Map<String, String>,
    onFocusChanged: (String, String) -> Unit,
    onMovieSettled: (String) -> Unit,
    onMovieClick: (MovieCard) -> Unit,
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
                settledIds = settledIds,
                firstItemRequester = if (categories.firstOrNull()?.id == category.id) firstItemRequester else null,
                lastFocusedMovieIdProvider = { rowFocusMemoryProvider()[category.id] },
                focusedMovieIdProvider = focusedMovieIdProvider,
                onFocusChanged = onFocusChanged,
                onMovieSettled = onMovieSettled,
                onMovieClick = onMovieClick,
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: MovieCategory,
    settledIds: Set<String>,
    firstItemRequester: FocusRequester?,
    lastFocusedMovieIdProvider: () -> String?,
    focusedMovieIdProvider: () -> String,
    onFocusChanged: (String, String) -> Unit,
    onMovieSettled: (String) -> Unit,
    onMovieClick: (MovieCard) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 58.dp, bottom = 12.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
                val fr = remember(movie.id) {
                    if (category.movies.firstOrNull()?.id == movie.id) {
                        firstItemRequester ?: FocusRequester()
                    } else {
                        FocusRequester()
                    }
                }
                focusRequesters[movie.id] = fr

                MovieCardItem(
                    movie = movie,
                    isSettled = settledIds.contains(movie.id),
                    focusRequester = fr,
                    isFocusedProvider = { focusedMovieIdProvider() == movie.id },
                    onFocusGained = { onFocusChanged(category.id, movie.id) },
                    onMovieSettled = onMovieSettled,
                    onMovieClick = onMovieClick,
                )
            }
        }
    }
}

@Composable
private fun MovieCardItem(
    movie: MovieCard,
    isSettled: Boolean,
    focusRequester: FocusRequester,
    isFocusedProvider: () -> Boolean,
    onFocusGained: () -> Unit,
    onMovieSettled: (String) -> Unit,
    onMovieClick: (MovieCard) -> Unit,
) {
    val isFocused = isFocusedProvider()
    val scope = rememberCoroutineScope()

    DisposableEffect(movie.id) {
        val job = if (!isSettled) {
            scope.launch {
                delay(1000)
                onMovieSettled(movie.id)
            }
        } else {
            null
        }

        onDispose {
            job?.cancel()
        }
    }

    Box(
        modifier = Modifier
            .width(240.dp)
            .aspectRatio(16f / 9f)
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused) {
                    onFocusGained()
                }
            }.focusable()
            .clickable { onMovieClick(movie) }
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 4.dp else 0.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            ).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.BottomStart,
    ) {
        AsyncImage(
            model = ImageRequest
                .Builder(LocalContext.current)
                .data(if (isSettled) movie.thumbnail else null)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = movie.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}
