package com.mohdhussain.hrcontacts.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.components.BrandMark
import com.mohdhussain.hrcontacts.ui.components.HrOutlinedButton
import com.mohdhussain.hrcontacts.ui.components.ResponsiveContent
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * The sign-in screen.
 *
 * The old version was an app name and a button on an empty canvas. This is the one screen where the
 * app has to earn a Google account handover, so it now says what it does with three concrete
 * promises and closes with what signing in actually means for the user's data. Nothing here is
 * decoration — it is the argument for tapping the button.
 *
 * Stateless: [onGoogleSignIn] hands back to the fragment, which owns the Credential Manager flow.
 */
@Composable
fun WelcomeScreen(
    loading: Boolean,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ResponsiveContent(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xxl, vertical = Spacing.xxxl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.size(Spacing.xxl))

                    BrandMark(size = 80.dp)

                    Text(
                        text = stringResource(R.string.welcome_tagline),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = Spacing.xl)
                    )

                    Text(
                        text = stringResource(R.string.welcome_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = Spacing.md)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        ValueProposition(
                            icon = painterResource(R.drawable.ic_filter),
                            text = stringResource(R.string.welcome_value_search)
                        )
                        ValueProposition(
                            icon = painterResource(R.drawable.ic_email),
                            text = stringResource(R.string.welcome_value_templates)
                        )
                        ValueProposition(
                            icon = painterResource(R.drawable.ic_person),
                            text = stringResource(R.string.welcome_value_private)
                        )
                    }

                    Spacer(Modifier.size(Spacing.xxxl))

                    HrOutlinedButton(
                        text = stringResource(R.string.continue_with_google),
                        onClick = onGoogleSignIn,
                        loading = loading,
                        leadingIcon = painterResource(R.drawable.ic_google)
                    )

                    Text(
                        text = stringResource(R.string.welcome_legal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = Spacing.lg)
                    )

                    Spacer(Modifier.size(Spacing.xl))
                }
            }
        }
    }
}

/** One of the three promises above the sign-in button. */
@Composable
private fun ValueProposition(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Sizes.IconSmall)
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@LightDarkPreview
@Composable
private fun WelcomeScreenPreview() {
    PreviewSurface {
        WelcomeScreen(loading = false, onGoogleSignIn = {})
    }
}

@LightDarkPreview
@Composable
private fun WelcomeScreenLoadingPreview() {
    PreviewSurface {
        WelcomeScreen(loading = true, onGoogleSignIn = {})
    }
}
