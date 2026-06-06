package com.example.mytvapplication.features.dashboard.domain

import androidx.compose.runtime.Immutable

@Immutable
data class MovieCard(
    val id: String,
    val title: String,
    val thumbnail: String,
    val description: String,
    val rating: String,
    val year: String
)

@Immutable
data class MovieCategory(
    val id: String,
    val name: String,
    val movies: List<MovieCard>
)
