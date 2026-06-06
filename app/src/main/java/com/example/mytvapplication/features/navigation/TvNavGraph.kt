package com.example.mytvapplication.features.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.mytvapplication.features.dashboard.presentation.TvDashboardScreen
import com.example.mytvapplication.features.dashboard.presentation.TvDashboardViewModel
import com.example.mytvapplication.features.details.presentation.ui.TvMovieDetailsScreen
import com.example.mytvapplication.features.info.presentation.TvMovieInfoScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Dashboard : Screen

    @Serializable
    data class Details(
        val movieId: String,
        val movieTitle: String,
        val thumbnail: String,
        val videoUrl: String,
        val description: String,
        val rating: String,
        val year: String,
    ) : Screen

    @Serializable
    data class Info(
        val movieTitle: String,
        val description: String,
        val rating: String,
        val year: String,
    ) : Screen
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TvNavGraph(
    backStack: NavBackStack<NavKey>,
) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()

    // Global Back Handler to prevent app exit in both Portrait and Landscape.
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    // Dynamic Directive Strategy:
    val directive = remember(windowAdaptiveInfo, backStack.size) {
        val base = calculatePaneScaffoldDirective(windowAdaptiveInfo)
        if (backStack.size <= 1) {
            base.copy(maxHorizontalPartitions = 1, horizontalPartitionSpacerSize = 0.dp)
        } else {
            base.copy(horizontalPartitionSpacerSize = 0.dp)
        }
    }

    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
        },
        popTransitionSpec = {
            scaleIn(animationSpec = spring(3F)) togetherWith
                scaleOut(animationSpec = spring(3F))
        },
        predictivePopTransitionSpec = {
            slideInHorizontally { -it } togetherWith
                slideOutHorizontally { it }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        sceneStrategies = listOf(listDetailStrategy),
        entryProvider = entryProvider {
            entry<Screen.Dashboard>(
                metadata = ListDetailSceneStrategy.listPane(),
            ) {
                val dashboardViewModel: TvDashboardViewModel = viewModel {
                    TvDashboardViewModel()
                }
                TvDashboardScreen(
                    viewModel = dashboardViewModel,
                    onMovieSelected = { movie ->
                        val nextScreen = Screen.Details(
                            movieId = movie.id,
                            movieTitle = movie.title,
                            thumbnail = movie.thumbnail,
                            videoUrl = movie.videoUrl,
                            description = movie.description,
                            rating = movie.rating,
                            year = movie.year,
                        )
                        if (backStack.lastOrNull() != nextScreen) {
                            if (backStack.any { it is Screen.Details }) {
                                backStack.removeIf { (it is Screen.Details) || (it is Screen.Info) }
                            }
                            backStack.add(nextScreen)
                        }
                    },
                )
            }

            entry<Screen.Details>(
                metadata = ListDetailSceneStrategy.detailPane(),
            ) { key ->
                TvMovieDetailsScreen(
                    movieTitle = key.movieTitle,
                    thumbnail = key.thumbnail,
                    videoUrl = key.videoUrl,
                    onBackTriggered = {
                        backStack.removeLastOrNull()
                    },
                    onInfoTriggered = {
                        val infoScreen = Screen.Info(
                            movieTitle = key.movieTitle,
                            description = key.description,
                            rating = key.rating,
                            year = key.year,
                        )
                        if (backStack.lastOrNull() != infoScreen) {
                            backStack.add(infoScreen)
                        }
                    },
                )
            }

            entry<Screen.Info>(
                metadata = ListDetailSceneStrategy.extraPane(),
            ) { key ->
                TvMovieInfoScreen(
                    movieTitle = key.movieTitle,
                    description = key.description,
                    rating = key.rating,
                    year = key.year,
                )
            }
        },
    )
}
