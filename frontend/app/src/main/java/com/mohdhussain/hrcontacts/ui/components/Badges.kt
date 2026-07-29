package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.HrPill
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.LocalHrColors
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * A small pill of status text, optionally led by an icon.
 *
 * [border] is what lets the quiet variants exist: a filled badge shouts, an outlined one states.
 */
@Composable
fun StatusBadge(
    text: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    border: Color? = null,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .background(containerColor, HrPill)
            .then(if (border != null) Modifier.border(BorderStroke(1.dp, border), HrPill) else Modifier)
            .padding(horizontal = Spacing.sm, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

/**
 * Whether a contact's details have been confirmed by the server.
 *
 * Verified is the exceptional, valuable state, so it gets the filled green badge and a tick.
 * Unverified is simply the default for a freshly added contact and now says so quietly, in an
 * outline — it used to be as loud as its opposite, which made every list look like a wall of
 * warnings.
 */
@Composable
fun VerifiedBadge(verified: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalHrColors.current
    if (verified) {
        StatusBadge(
            text = stringResource(R.string.verified),
            contentColor = colors.verifiedContent,
            containerColor = colors.verifiedContainer,
            icon = Icons.Filled.Check,
            modifier = modifier
        )
    } else {
        StatusBadge(
            text = stringResource(R.string.not_verified),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            border = MaterialTheme.colorScheme.outlineVariant,
            modifier = modifier
        )
    }
}

/** Shown only on private contacts; a shared contact is the norm and needs no badge of its own. */
@Composable
fun PrivateBadge(modifier: Modifier = Modifier) {
    val colors = LocalHrColors.current
    StatusBadge(
        text = stringResource(R.string.private_contact),
        contentColor = colors.privateContent,
        containerColor = colors.privateContainer,
        icon = Icons.Filled.Lock,
        modifier = modifier
    )
}

@LightDarkPreview
@Composable
private fun BadgesPreview() {
    PreviewSurface {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VerifiedBadge(verified = true)
            VerifiedBadge(verified = false)
            PrivateBadge()
        }
    }
}
