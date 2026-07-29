package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * What a screen shows when it has nothing to show.
 *
 * The app used to answer this with one centred line of grey text. An empty state is the screen a new
 * user meets first, so it now says what the screen is for, why it is empty, and — when there is one —
 * offers the action that fills it.
 */
@Composable
fun EmptyState(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xxl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.lg)
        )

        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }

        if (actionText != null && onAction != null) {
            Box(
                modifier = Modifier
                    .padding(top = Spacing.xl)
                    .widthIn(max = 260.dp)
            ) {
                HrPrimaryButton(text = actionText, onClick = onAction)
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun EmptyStateWithActionPreview() {
    PreviewSurface {
        EmptyState(
            icon = painterResource(R.drawable.ic_contacts),
            title = "No HR contacts yet",
            description = "Save a recruiter's details once and reach out from here any time.",
            actionText = "Add a contact",
            onAction = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun EmptyStateNoResultsPreview() {
    PreviewSurface {
        EmptyState(
            icon = painterResource(R.drawable.ic_filter),
            title = "No matches",
            description = "No contacts match your search or filters. Try clearing one of them."
        )
    }
}
