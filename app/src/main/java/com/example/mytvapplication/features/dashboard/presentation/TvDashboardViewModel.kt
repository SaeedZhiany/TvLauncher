package com.example.mytvapplication.features.dashboard.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytvapplication.features.dashboard.domain.MovieCard
import com.example.mytvapplication.features.dashboard.domain.MovieCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class TvDashboardViewModel : ViewModel() {
    // Structural Data State
    private val _categories = MutableStateFlow<List<MovieCategory>>(emptyList())
    val categories: StateFlow<List<MovieCategory>> = _categories.asStateFlow()

    // Row Focus Memory (CategoryId -> LastFocusedMovieId)
    private val _rowFocusMemory = MutableStateFlow<Map<String, String>>(emptyMap())
    val rowFocusMemory: StateFlow<Map<String, String>> = _rowFocusMemory.asStateFlow()

    // Fast-updating Global Focus Primitive (Immediate UI highlight)
    private val _currentGlobalFocusedMovieId = MutableStateFlow("")
    val currentGlobalFocusedMovieId: StateFlow<String> = _currentGlobalFocusedMovieId.asStateFlow()

    // In-Memory Image Cache
    private val _bitmaps = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val bitmaps: StateFlow<Map<String, Bitmap>> = _bitmaps.asStateFlow()

    private val activeLoadJobs = mutableMapOf<String, Job>()

    init {
        loadMockData()

        /**
         * Principal Reactive Focus Strategy:
         * We observe the global focus state and use collectLatest.
         * collectLatest automatically cancels the previous block (including its delay)
         * whenever a new focus event arrives. This guarantees that:
         * 1. Images NEVER start downloading during fast navigation.
         * 2. Only the item where focus settles for 1000ms triggers a download.
         */
        viewModelScope.launch {
            _currentGlobalFocusedMovieId.collectLatest { movieId ->
                if (movieId.isBlank()) return@collectLatest
                if (_bitmaps.value.containsKey(movieId)) return@collectLatest

                delay(1000)

                val movie = findMovieById(movieId)
                if (movie != null) {
                    executeLoad(movie.id, movie.thumbnail)
                }
            }
        }
    }

    /**
     * Handles focus changes.
     * This now only updates the primitive focus state.
     * The image loading is driven reactively by the collectLatest block above.
     */
    fun onMovieFocused(categoryId: String, movieId: String) {
        _currentGlobalFocusedMovieId.value = movieId
        _rowFocusMemory.update { it + (categoryId to movieId) }
    }

    private fun executeLoad(movieId: String, url: String) {
        if (_bitmaps.value.containsKey(movieId)) return
        if (activeLoadJobs[movieId]?.isActive == true) return

        activeLoadJobs[movieId] = viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val connection = URL(url).openConnection()
                    connection.setRequestProperty("User-Agent", "Android-TvLauncher")
                    connection.connect()
                    connection.getInputStream().use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
                bitmap?.let { b ->
                    _bitmaps.update { it + (movieId to b) }
                }
            } catch (_: Exception) {
                // Cancellation or network error
            } finally {
                activeLoadJobs.remove(movieId)
            }
        }
    }

    private fun findMovieById(movieId: String): MovieCard? = categories.value
        .flatMap {
            it.movies
        }.find { it.id == movieId }

    private fun loadMockData() {
        val mockCategories = List(10) { catIndex ->
            MovieCategory(
                id = "cat_$catIndex",
                name = "Category ${catIndex + 1}",
                movies = List(15) { movIndex ->
                    val id = "cat_${catIndex}_mov_$movIndex"
                    MovieCard(
                        id = id,
                        title = "Movie ${movIndex + 1}",
                        thumbnail = "https://picsum.photos/seed/$id/400/225",
                        description = "Sample description",
                        rating = "4.5",
                        year = "2024",
                    )
                },
            )
        }
        _categories.value = mockCategories
    }
}
