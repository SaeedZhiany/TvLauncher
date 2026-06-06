package com.example.mytvapplication.features.navigation

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    ) : Screen
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TvNavGraph(
    backStack: NavBackStack<NavKey>,
) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)
    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
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
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("Select a video")
                            }
                        }
                    },
                ),
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
                        )
                        if (backStack.lastOrNull() != nextScreen) {
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
                )
            }
        },
    )
}
