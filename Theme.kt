package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LoopTealDark,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = LoopTealContainerDark,
    onPrimaryContainer = LoopTealContainer,
    secondary = LoopAmberDark,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = LoopAmberContainerDark,
    onSecondaryContainer = LoopAmberContainer,
    background = LoopNavyDarkBg,
    onBackground = TextPrimaryDark,
    surface = LoopNavyDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = LoopNavyDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF334155),
    error = LoopDangerRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LoopTealPrimary,
    onPrimary = Color.White,
    primaryContainer = LoopTealContainer,
    onPrimaryContainer = Color(0xFF134E4A),
    secondary = LoopAmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = LoopAmberContainer,
    onSecondaryContainer = Color(0xFF78350F),
    background = LoopLightBg,
    onBackground = TextPrimaryLight,
    surface = LoopLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LoopLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1),
    error = LoopDangerRed,
    onError = Color.White
)

@Composable
fun LoopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
