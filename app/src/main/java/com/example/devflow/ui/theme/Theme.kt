package com.example.devflow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary              = LGPrimary,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFD6EBFF),
    onPrimaryContainer   = Color(0xFF001D3D),
    secondary            = LGSecondary,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFD4F5DF),
    onSecondaryContainer = Color(0xFF002110),
    tertiary             = LGTertiary,
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFFFE5B4),
    onTertiaryContainer  = Color(0xFF2D1B00),
    error                = LGError,
    onError              = Color.White,
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),
    background           = LGBackground,
    onBackground         = Color(0xFF1C1C1E),
    surface              = LGSurface,
    onSurface            = Color(0xFF1C1C1E),
    surfaceVariant       = Color(0xE6F2F2F7),
    onSurfaceVariant     = Color(0xFF6E6E73),
    outline              = Color(0xFFD1D1D6),
    outlineVariant       = Color(0xFFE5E5EA),
    scrim                = Color(0xFF000000),
    inverseSurface       = Color(0xFF1C1C1E),
    inverseOnSurface     = Color(0xFFF2F2F7),
    inversePrimary       = Color(0xFF82B9FF)
)

private val DarkColorScheme = darkColorScheme(
    primary              = LGDarkPrimary,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFF003D7A),
    onPrimaryContainer   = Color(0xFFD6EBFF),
    secondary            = LGDarkSecondary,
    onSecondary          = Color.Black,
    secondaryContainer   = Color(0xFF003B1A),
    onSecondaryContainer = Color(0xFFD4F5DF),
    tertiary             = LGDarkTertiary,
    onTertiary           = Color.Black,
    tertiaryContainer    = Color(0xFF4A2E00),
    onTertiaryContainer  = Color(0xFFFFE5B4),
    error                = LGDarkError,
    onError              = Color.Black,
    errorContainer       = Color(0xFF7A0000),
    onErrorContainer     = Color(0xFFFFDAD6),
    background           = LGDarkBackground,
    onBackground         = Color(0xFFF2F2F7),
    surface              = LGDarkSurface,
    onSurface            = Color(0xFFF2F2F7),
    surfaceVariant       = Color(0xFF2C2C2E),
    onSurfaceVariant     = Color(0xFFAEAEB2),
    outline              = Color(0xFF3A3A3C),
    outlineVariant       = Color(0xFF2C2C2E),
    scrim                = Color(0xFF000000),
    inverseSurface       = Color(0xFFF2F2F7),
    inverseOnSurface     = Color(0xFF1C1C1E),
    inversePrimary       = LGPrimary
)

@Composable
fun DevFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}