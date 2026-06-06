package com.example.mytvapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme

@Composable
fun TvLauncherTheme(
    isInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isInDarkTheme) {
        darkColorScheme(
            primary = PrimaryColor,
            secondary = SecondaryColor,
            tertiary = TertiaryColor,
            background = BackgroundDark,
            surface = SurfaceDark,
            onBackground = OnBackgroundDark,
            onSurface = OnSurfaceDark,
        )
    } else {
        lightColorScheme(
            primary = PrimaryColorDark,
            secondary = SecondaryColorDark,
            tertiary = TertiaryColorDark,
            background = BackgroundLight,
            surface = SurfaceLight,
            onBackground = OnBackgroundLight,
            onSurface = OnSurfaceLight,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
