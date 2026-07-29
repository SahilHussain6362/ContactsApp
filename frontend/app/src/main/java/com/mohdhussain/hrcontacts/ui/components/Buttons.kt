package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.HrPill
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.LocalHrColors
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * A tonal icon button at the 48dp accessibility floor.
 *
 * The equivalent buttons in the old layouts were 44dp, and the bookmark and template buttons 40dp.
 * Every icon-only action in the app now goes through here so that cannot drift again.
 */
@Composable
fun HrIconButton(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(Sizes.MinTouchTarget),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = tint
        )
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Sizes.Icon)
        )
    }
}

@Composable
fun HrIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(Sizes.MinTouchTarget),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = tint
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Sizes.Icon)
        )
    }
}

/**
 * The bookmark toggle.
 *
 * Animates colour and gives a small scale kick on the way in, because a bookmark is a deliberate
 * act and the only confirmation the user gets is the icon itself changing.
 */
@Composable
fun BookmarkButton(
    bookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHrColors.current
    val tint by animateColorAsState(
        targetValue = if (bookmarked) colors.bookmarkActive else colors.bookmarkInactive,
        label = "bookmarkTint"
    )
    val scale by animateFloatAsState(
        targetValue = if (bookmarked) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 900f),
        label = "bookmarkScale"
    )
    IconButton(
        onClick = onClick,
        modifier = modifier.size(Sizes.MinTouchTarget)
    ) {
        Icon(
            painter = painterResource(
                if (bookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
            ),
            contentDescription = stringResource(
                if (bookmarked) R.string.bookmark_remove else R.string.bookmark_add
            ),
            tint = tint,
            modifier = Modifier
                .size(Sizes.Icon)
                .scale(scale)
        )
    }
}

/**
 * The one primary action on a screen. Full width, pill, and able to show progress in place so a save
 * never leaves the user wondering whether the tap registered.
 */
@Composable
fun HrPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: Painter? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = HrPill,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.MinTouchTarget),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    painter = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(Sizes.Icon)
                        .padding(end = 0.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = if (leadingIcon != null) Spacing.sm else 0.dp)
            )
        }
    }
}

/** The secondary counterpart to [HrPrimaryButton], for the alternative on a two-choice screen. */
@Composable
fun HrOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: Painter? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = HrPill,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.MinTouchTarget)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    painter = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(Sizes.Icon)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = if (leadingIcon != null) Spacing.sm else 0.dp)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun ButtonsPreview() {
    PreviewSurface {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HrIconButton(
                onClick = {},
                icon = painterResource(R.drawable.ic_copy),
                contentDescription = "Copy"
            )
            HrIconButton(
                onClick = {},
                icon = painterResource(R.drawable.ic_whatsapp),
                contentDescription = "WhatsApp",
                tint = LocalHrColors.current.whatsapp
            )
            BookmarkButton(bookmarked = true, onClick = {})
            BookmarkButton(bookmarked = false, onClick = {})
        }
    }
}

@LightDarkPreview
@Composable
private fun PrimaryButtonPreview() {
    PreviewSurface {
        Row(modifier = Modifier.padding(Spacing.lg)) {
            HrPrimaryButton(text = "Save", onClick = {})
        }
    }
}

@LightDarkPreview
@Composable
private fun PrimaryButtonLoadingPreview() {
    PreviewSurface {
        Row(modifier = Modifier.padding(Spacing.lg)) {
            HrPrimaryButton(text = "Save", onClick = {}, loading = true)
        }
    }
}
