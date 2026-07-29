package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.mohdhussain.hrcontacts.ui.theme.Sizes

/**
 * Centres [content] and caps how wide it can grow.
 *
 * On a phone this does nothing at all. On a tablet or in landscape it is what stops a form field
 * from stretching to 900dp and a paragraph from running to a line length nobody can track back to
 * the start of.
 */
@Composable
fun ResponsiveContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = Sizes.ContentMaxWidth,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(Modifier.widthIn(max = maxWidth)) { content() }
    }
}

/**
 * How many columns of contact cards [availableWidth] has room for. One on a phone, more once there
 * is width to fill — a single stretched column on a tablet wastes most of the screen.
 */
fun columnsFor(availableWidth: Dp): Int = when {
    availableWidth >= Sizes.ThreeColumnBreakpoint -> 3
    availableWidth >= Sizes.TwoColumnBreakpoint -> 2
    else -> 1
}
