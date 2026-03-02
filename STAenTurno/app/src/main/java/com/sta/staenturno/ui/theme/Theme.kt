package com.sta.staenturno.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Color scheme completo para modo CLARO.
 * Al especificar onSurface, onBackground y surface explícitamente,
 * evitamos que Material3 los derive de dynamic color (Material You),
 * que en Android 12+ puede generar colores casi invisibles según el wallpaper.
 *
 * WCAG AA mínimo (4.5:1). TextDark sobre BackgroundLavender = ~7:1 ✓
 */
private val LightColorScheme = lightColorScheme(
    primary            = PastelPurple,
    onPrimary          = SurfaceWhite,
    primaryContainer   = PastelPurpleLight,
    onPrimaryContainer = TextDark,
    secondary          = PurpleGrey40,
    onSecondary        = SurfaceWhite,
    tertiary           = Pink40,
    onTertiary         = SurfaceWhite,
    background         = BackgroundLavender,
    onBackground       = TextDark,          // Texto sobre fondo principal ← clave
    surface            = SurfaceWhite,
    onSurface          = TextDark,          // Texto dentro de los TextField ← clave
    surfaceVariant     = CardWhite,
    onSurfaceVariant   = TextMedium,
    outline            = TextLight,
    error              = PastelRed,
    onError            = SurfaceWhite
)

/**
 * Color scheme completo para modo OSCURO.
 * Mismo principio: onSurface/onBackground explícito para máximo contraste.
 */
private val DarkColorScheme = darkColorScheme(
    primary            = Purple80,
    onPrimary          = TextDark,
    primaryContainer   = PastelPurpleDark,
    onPrimaryContainer = TextOnDark,
    secondary          = PurpleGrey80,
    onSecondary        = TextDark,
    tertiary           = Pink80,
    onTertiary         = TextDark,
    background         = BackgroundDark,
    onBackground       = TextOnDark,        // Texto sobre fondo oscuro ← clave
    surface            = SurfaceDark,
    onSurface          = TextOnDark,        // Texto dentro de los TextField oscuros ← clave
    surfaceVariant     = SurfaceDark,
    onSurfaceVariant   = TextMediumDark,
    outline            = TextHintDark,
    error              = PastelRed,
    onError            = SurfaceWhite
)

@Composable
fun STAenTurnoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // dynamicColor deshabilitado intencionalmente:
    // Material You en Android 12+ puede generar colores onSurface casi invisibles
    // según el wallpaper del dispositivo (problema crítico reportado).
    // Usamos nuestro color scheme completo para control total del contraste.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
