package com.example.mytvapplication.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import com.example.mytvapplication.features.dashboard.domain.MovieCard
import com.example.mytvapplication.features.dashboard.domain.MovieCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TvDashboardViewModel : ViewModel() {
    // 1. Structural Data State
    private val _categories = MutableStateFlow<List<MovieCategory>>(emptyList())
    val categories: StateFlow<List<MovieCategory>> = _categories.asStateFlow()

    // 2. Row Focus Memory (CategoryId -> LastFocusedMovieId)
    private val _rowFocusMemory = MutableStateFlow<Map<String, String>>(emptyMap())
    val rowFocusMemory: StateFlow<Map<String, String>> = _rowFocusMemory.asStateFlow()

    // 3. Global Focused Movie ID
    private val _currentGlobalFocusedMovieId = MutableStateFlow(value = "")
    val currentGlobalFocusedMovieId: StateFlow<String> = _currentGlobalFocusedMovieId.asStateFlow()

    // 4. Persistence for loaded images
    private val _settledIds = MutableStateFlow<Set<String>>(value = emptySet())
    val settledIds: StateFlow<Set<String>> = _settledIds.asStateFlow()

    init {
        loadMockData()
    }

    /**
     * Marks a movie as settled (visibility timer passed).
     */
    fun onMovieSettled(movieId: String) {
        if (_settledIds.value.contains(movieId)) return
        _settledIds.update { it + movieId }
    }

    /**
     * Updates the focus state.
     */
    fun onMovieFocused(categoryId: String, movieId: String) {
        if (_currentGlobalFocusedMovieId.value == movieId) return
        _currentGlobalFocusedMovieId.value = movieId
        _rowFocusMemory.update { it + (categoryId to movieId) }
    }

    private fun loadMockData() {
        val mockCategories = List(size = 10) { catIndex ->
            MovieCategory(
                id = "cat_$catIndex",
                name = "Category ${catIndex + 1}",
                movies = List(size = 15) { movIndex ->
                    val id = "cat_${catIndex}_mov_$movIndex"
                    MovieCard(
                        id = id,
                        title = "Movie ${movIndex + 1}",
                        thumbnail = "https://picsum.photos/seed/$id/400/225",
                        description = "Sample description",
                        rating = "4.5",
                        year = "2024",
                        videoUrl = "https://vjs.zencdn.net/v/oceans.mp4",
                    )
                },
            )
        }
        _categories.value = mockCategories
    }
}
