package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing
import com.mohdhussain.hrcontacts.ui.theme.avatarColorsFor
import com.mohdhussain.hrcontacts.ui.theme.initialFor

/**
 * A contact's initial in a coloured circle, with the colour derived from their name.
 *
 * Carries no content description: the name it stands for is always written next to it, so
 * announcing the initial as well would just make a screen reader say the first letter twice.
 */
@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = Sizes.AvatarSmall,
    fontSize: TextUnit = (size.value / 2.4f).sp
) {
    val colors = avatarColorsFor(name, isSystemInDarkTheme())
    Box(
        modifier = modifier
            .size(size)
            .background(colors.container, CircleShape)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialFor(name),
            color = colors.content,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize),
            textAlign = TextAlign.Center
        )
    }
}

@LightDarkPreview
@Composable
private fun AvatarPalettePreview() {
    PreviewSurface {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            listOf("Priya Sharma", "Rahul Verma", "Aisha Khan", "Tom Becker", "")
                .forEach { Avatar(name = it, size = 48.dp) }
        }
    }
}

@LightDarkPreview
@Composable
private fun AvatarSizesPreview() {
    PreviewSurface {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar("Nadia Iqbal", size = Sizes.AvatarSmall)
            Avatar("Nadia Iqbal", size = Sizes.AvatarMedium)
            Avatar("Nadia Iqbal", size = Sizes.AvatarLarge)
            Avatar("Nadia Iqbal", size = Sizes.AvatarXLarge)
        }
    }
}
