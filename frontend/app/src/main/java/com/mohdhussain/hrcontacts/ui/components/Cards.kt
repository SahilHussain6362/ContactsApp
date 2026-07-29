package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.ui.theme.LocalHrColors
import com.mohdhussain.hrcontacts.ui.theme.Sizes

/**
 * The surface every panel in the app is built on: flat, hairline-bordered, no shadow.
 *
 * Shadows would be the obvious way to separate a card from the page, and the wrong one here. These
 * screens stack a lot of small panels, and a drop shadow on each turns a contact list into something
 * fussy. A 1dp border does the same job and stays quiet, which is what a screen full of other
 * people's contact details should be.
 */
@Composable
fun HrCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color = LocalHrColors.current.cardBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(Sizes.CardBorder, borderColor),
        content = content
    )
}
