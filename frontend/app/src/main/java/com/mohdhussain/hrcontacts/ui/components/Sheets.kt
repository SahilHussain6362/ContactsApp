package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * The title block at the top of a bottom sheet.
 *
 * The drag handle above it comes from the `BottomSheetDialogFragment` container, which the sheets
 * still use so navigation and the ViewModel sharing between a sheet and its host fragment stay
 * exactly as they were.
 *
 * [trailing] is where a sheet-level action goes — the filter sheet's "Clear all".
 */
@Composable
fun SheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            trailing?.invoke()
        }
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SheetHeaderPreview() {
    PreviewSurface {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            SheetHeader(
                title = "New email template",
                description = "Prefills the subject and body when you mail a contact."
            )
        }
    }
}
