package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing
import androidx.compose.foundation.selection.toggleable

/**
 * The app's text field.
 *
 * [error] is a nullable message rather than a boolean plus a separate string: an error state with no
 * explanation is not a state worth having, and pairing them makes it impossible to set one without
 * the other. When set, it also becomes the field's accessibility error text.
 */
@Composable
fun HrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: Painter? = null,
    error: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    counterMax: Int? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { new ->
                if (counterMax == null || new.length <= counterMax) onValueChange(new)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = label) },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Sizes.Icon)
                    )
                }
            },
            trailingIcon = trailing,
            isError = error != null,
            supportingText = when {
                error != null -> {
                    { Text(text = error, style = MaterialTheme.typography.bodySmall) }
                }
                counterMax != null -> {
                    {
                        Text(
                            text = "${value.length} / $counterMax",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                supportingText != null -> {
                    { Text(text = supportingText, style = MaterialTheme.typography.bodySmall) }
                }
                else -> null
            },
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            shape = MaterialTheme.shapes.small,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

/**
 * A labelled switch row — the private-contact toggle, the three filter toggles.
 *
 * The whole row is the target, not just the switch, and the row carries the toggle semantics so a
 * screen reader announces the label and the state together.
 */
@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            )
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
        }
        Switch(
            checked = checked,
            // The row owns the interaction; the switch is the indicator.
            onCheckedChange = null,
            modifier = Modifier.padding(start = Spacing.md)
        )
    }
}

/** A heading that groups a run of form fields, so a long form reads as sections rather than a wall. */
@Composable
fun FormSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = Spacing.lg, bottom = Spacing.sm)
    )
}

@LightDarkPreview
@Composable
private fun FormFieldsPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            FormSectionHeader(title = "Who they are")
            HrTextField(
                value = "Priya Sharma",
                onValueChange = {},
                label = "Name",
                leadingIcon = painterResource(R.drawable.ic_person)
            )
            HrTextField(
                value = "",
                onValueChange = {},
                label = "Company",
                leadingIcon = painterResource(R.drawable.ic_business),
                error = "Company is required"
            )
            HrTextField(
                value = "12345",
                onValueChange = {},
                label = "Mobile",
                leadingIcon = painterResource(R.drawable.ic_phone),
                keyboardType = KeyboardType.Phone,
                supportingText = "Optional if an email is given"
            )
            HorizontalDivider()
            SwitchRow(
                title = "Private contact",
                description = "Only you can see a private contact.",
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}
