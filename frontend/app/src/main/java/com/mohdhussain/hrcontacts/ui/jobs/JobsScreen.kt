package com.mohdhussain.hrcontacts.ui.jobs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.components.EmptyState
import com.mohdhussain.hrcontacts.ui.components.HrTopAppBar
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface

/**
 * The Jobs tab. There is nothing behind it yet — this is a placeholder so the tab has somewhere to
 * land rather than being left out of the nav bar until the feature is built.
 */
@Composable
fun JobsScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // Horizontal only — the tab bar below this screen owns the system navigation bar area.
        // See the comment in activity_main.xml.
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        topBar = { HrTopAppBar(title = stringResource(R.string.jobs_title)) }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyState(
                icon = painterResource(R.drawable.ic_hourglass),
                title = stringResource(R.string.jobs_coming_soon_title),
                description = stringResource(R.string.jobs_coming_soon_body)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun JobsScreenPreview() {
    PreviewSurface { JobsScreen() }
}
