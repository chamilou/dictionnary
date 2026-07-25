package com.avardiction.app.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NightMoss,
    secondary = NightClay,
    tertiary = GoldMist,
    onTertiary = NightForest,
    tertiaryContainer = Color(0xFF4A422C),
    onTertiaryContainer = NightInk,
    background = NightForest,
    surface = NightCard,
    surfaceVariant = NightCardRaised,
    onPrimary = NightForest,
    onSecondary = NightForest,
    onBackground = NightInk,
    onSurface = NightInk,
    onSurfaceVariant = NightMuted,
    outline = NightLine,
    primaryContainer = NightPrimaryContainer,
    onPrimaryContainer = NightInk,
    secondaryContainer = NightSecondaryContainer,
    onSecondaryContainer = NightInk
)

private val LightColorScheme = lightColorScheme(
    primary = Moss,
    onPrimary = Paper,
    primaryContainer = Color(0xFFD8E6E0),
    onPrimaryContainer = Ink,
    secondary = Clay,
    onSecondary = Paper,
    secondaryContainer = ClaySoft,
    onSecondaryContainer = Ink,
    tertiary = GoldMist,
    onTertiary = Ink,
    tertiaryContainer = Color(0xFFF3E8C5),
    onTertiaryContainer = Ink,
    background = SandLight,
    surface = Paper,
    surfaceVariant = Color(0xFFF0E6D6),
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Slate,
    outline = Line
)

@Composable
fun DictionnaryTheme(
    darkTheme: Boolean = false,
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
