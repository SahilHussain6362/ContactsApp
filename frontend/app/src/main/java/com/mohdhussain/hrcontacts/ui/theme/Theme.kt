package com.mohdhussain.hrcontacts.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Reaches the product-specific colours from anywhere inside [HrContactsTheme]. Static because these
 * only change when the whole theme does, so nothing needs to recompose on a read.
 */
val LocalHrColors = staticCompositionLocalOf { LightSemanticColors }

/**
 * The app theme. Wrap the content of every `ComposeView` in this.
 *
 * Deliberately not wired to dynamic colour: the palette is doing a job here — a steady, cool blue is
 * what makes a screen full of other people's contact details read as trustworthy — and handing that
 * over to whatever wallpaper the user picked would undo it.
 */
@Composable
fun HrContactsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) HrDarkColorScheme else HrLightColorScheme
    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors

    // Keeps the status and navigation bar icons legible against whichever surface is behind them.
    // Skipped in previews and the layout editor, where there is no window to configure.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalHrColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HrTypography,
            shapes = HrShapes,
            content = content
        )
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
