package com.example.navapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = SafetyPrimary,
    onPrimary = White,
    primaryContainer = SafetySecondary.copy(alpha = 0.2f),
    onPrimaryContainer = SafetyPrimary,
    secondary = SafetySecondary,
    onSecondary = White,
    secondaryContainer = SafetySecondary.copy(alpha = 0.2f),
    onSecondaryContainer = SafetySecondary,
    tertiary = SafetyTertiary,
    onTertiary = White,
    tertiaryContainer = SafetyTertiary.copy(alpha = 0.2f),
    onTertiaryContainer = SafetyTertiary,
    error = SafetyError,
    onError = White,
    errorContainer = SafetyError.copy(alpha = 0.1f),
    onErrorContainer = SafetyError,
    background = SafetyBackground,
    onBackground = Black,
    surface = SafetySurface,
    onSurface = Black,
    surfaceVariant = White.copy(alpha = 0.9f),
    onSurfaceVariant = Gray,
    outline = Gray
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = SafetyPrimaryDark,
    onPrimary = Black,
    primaryContainer = SafetyPrimaryDark.copy(alpha = 0.2f),
    onPrimaryContainer = SafetyPrimaryDark,
    secondary = SafetySecondaryDark,
    onSecondary = Black,
    secondaryContainer = SafetySecondaryDark.copy(alpha = 0.2f),
    onSecondaryContainer = SafetySecondaryDark,
    tertiary = SafetyTertiaryDark,
    onTertiary = Black,
    tertiaryContainer = SafetyTertiaryDark.copy(alpha = 0.2f),
    onTertiaryContainer = SafetyTertiaryDark,
    error = SafetyErrorDark,
    onError = Black,
    errorContainer = SafetyErrorDark.copy(alpha = 0.1f),
    onErrorContainer = SafetyErrorDark,
    background = SafetyBackgroundDark,
    onBackground = White,
    surface = SafetySurfaceDark,
    onSurface = White,
    surfaceVariant = Black.copy(alpha = 0.9f),
    onSurfaceVariant = Gray,
    outline = Gray
)

// Custom colors for our safety app
data class SafetyColors(
    val sosColor: Color,
    val warningColor: Color,
    val successColor: Color,
    val zoneHome: Color = ZoneHome,
    val zoneWork: Color = ZoneWork,
    val zoneSchool: Color = ZoneSchool,
    val zoneHospital: Color = ZoneHospital,
    val zonePolice: Color = ZonePolice,
    val zoneCustom: Color = ZoneCustom
)

// SafetyTheme composition local
val LocalSafetyColors = staticCompositionLocalOf {
    SafetyColors(
        sosColor = SafetySOS,
        warningColor = SafetyWarning,
        successColor = SafetySuccess
    )
}

@Composable
fun NavAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Default to false for consistent branding
    content: @Composable () -> Unit
) {
    val safetyColors = if (darkTheme) {
        SafetyColors(
            sosColor = SafetySOSDark,
            warningColor = SafetyWarningDark,
            successColor = SafetySuccessDark
        )
    } else {
        SafetyColors(
            sosColor = SafetySOS,
            warningColor = SafetyWarning,
            successColor = SafetySuccess
        )
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalSafetyColors provides safetyColors,
                content = content
            )
        }
    )
}

// Extension to access safety colors from anywhere
object SafetyTheme {
    val colors: SafetyColors
        @Composable
        get() = LocalSafetyColors.current
}