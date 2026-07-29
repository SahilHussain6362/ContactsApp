package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * A yes/no confirmation.
 *
 * [destructive] tints the confirm button with the error colour — used for the two deletes in the app,
 * where the point is that the button should not look like the safe one.
 */
@Composable
fun ConfirmDialog(
    title: String? = null,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = stringResource(R.string.cancel),
    destructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = title?.let {
            { Text(text = it, style = MaterialTheme.typography.titleLarge) }
        },
        text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (destructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText, style = MaterialTheme.typography.labelLarge)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large
    )
}

/**
 * A single-choice list dialog — which saved template to send with.
 *
 * Scrolls, because a template's heading is free text and three long ones can outgrow a short screen.
 */
@Composable
fun ChoiceDialog(
    title: String,
    options: List<String>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                options.forEachIndexed { index, option ->
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { onSelect(index) }
                            .heightIn(min = Sizes.MinTouchTarget)
                            .padding(vertical = Spacing.md)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large
    )
}

@LightDarkPreview
@Composable
private fun ConfirmDialogPreview() {
    PreviewSurface {
        ConfirmDialog(
            title = stringResource(R.string.delete_contact_title),
            message = stringResource(R.string.delete_contact_message),
            confirmText = stringResource(R.string.delete),
            onConfirm = {},
            onDismiss = {},
            destructive = true
        )
    }
}

@LightDarkPreview
@Composable
private fun ChoiceDialogPreview() {
    PreviewSurface {
        ChoiceDialog(
            title = stringResource(R.string.templates_choose_email),
            options = listOf(
                "Following up on your JD",
                "Introduction — backend engineer",
                "Thanks for the call"
            ),
            onSelect = {},
            onDismiss = {}
        )
    }
}
