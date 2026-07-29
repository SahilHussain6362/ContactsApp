package com.mohdhussain.hrcontacts.ui.theme

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Renders a preview twice, once per theme. Every component and screen preview in the app carries
 * this instead of a bare `@Preview`, so a contrast problem in dark mode shows up while the component
 * is being written rather than on a device later.
 */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class LightDarkPreview

/** Themed background for previews, so a component is never previewed on bare white. */
@Composable
fun PreviewSurface(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    HrContactsTheme(darkTheme = darkTheme) {
        Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
