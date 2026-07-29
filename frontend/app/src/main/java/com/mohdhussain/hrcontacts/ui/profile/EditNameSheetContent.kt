package com.mohdhussain.hrcontacts.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.components.HrPrimaryButton
import com.mohdhussain.hrcontacts.ui.components.HrTextField
import com.mohdhussain.hrcontacts.ui.components.SheetHeader
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * Renames the signed-in user.
 *
 * The email is shown but not editable: it and the provider are owned by the identity provider, so the
 * display name is the only thing here the app can actually change.
 */
@Composable
fun EditNameSheetContent(
    initialName: String,
    email: String,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var error by remember { mutableStateOf<String?>(null) }

    val nameRequired = stringResource(R.string.profile_name_required)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = Spacing.lg)
    ) {
        SheetHeader(title = stringResource(R.string.profile_edit_name_title))

        Spacer(Modifier.size(Spacing.lg))

        HrTextField(
            value = name,
            onValueChange = {
                name = it
                if (error != null) error = null
            },
            label = stringResource(R.string.profile_display_name),
            leadingIcon = painterResource(R.drawable.ic_person),
            error = error,
            imeAction = ImeAction.Done
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm)
        )

        Spacer(Modifier.size(Spacing.xl))

        HrPrimaryButton(
            text = stringResource(R.string.save),
            onClick = {
                val trimmed = name.trim()
                if (trimmed.isEmpty()) {
                    error = nameRequired
                } else {
                    error = null
                    onSave(trimmed)
                }
            }
        )

        Spacer(Modifier.size(Spacing.xxl))
    }
}

@LightDarkPreview
@Composable
private fun EditNameSheetPreview() {
    PreviewSurface {
        EditNameSheetContent(
            initialName = "Mohd Hussain",
            email = "mohd.hussain@example.com",
            onSave = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun EditNameSheetEmptyPreview() {
    PreviewSurface {
        EditNameSheetContent(
            initialName = "",
            email = "mohd.hussain@example.com",
            onSave = {}
        )
    }
}
