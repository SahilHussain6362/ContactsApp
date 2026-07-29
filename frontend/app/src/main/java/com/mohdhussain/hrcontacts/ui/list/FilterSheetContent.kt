package com.mohdhussain.hrcontacts.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.components.HrPrimaryButton
import com.mohdhussain.hrcontacts.ui.components.SheetHeader
import com.mohdhussain.hrcontacts.ui.components.SwitchRow
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * The filter sheet's contents.
 *
 * [initialFilter] seeds the controls once and is then ignored — the `rememberSaveable` values below
 * are the live state, which is what makes a half-adjusted filter survive a rotation instead of
 * snapping back to whatever is currently applied. The old sheet achieved the same thing with a
 * `savedInstanceState == null` guard; here it falls out of how the state is remembered.
 */
@Composable
fun FilterSheetContent(
    companies: List<String>,
    initialFilter: ContactFilter,
    onApply: (ContactFilter) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val anyCompany = stringResource(R.string.filter_any_company)

    var company by rememberSaveable { mutableStateOf(initialFilter.company) }
    var hasPhone by rememberSaveable { mutableStateOf(initialFilter.hasPhone) }
    var hasEmail by rememberSaveable { mutableStateOf(initialFilter.hasEmail) }
    var verifiedOnly by rememberSaveable { mutableStateOf(initialFilter.verifiedOnly) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg)
    ) {
        SheetHeader(
            title = stringResource(R.string.filter_contacts),
            trailing = {
                TextButton(onClick = onClearAll) {
                    Text(
                        text = stringResource(R.string.filter_clear_all),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )

        Spacer(Modifier.size(Spacing.lg))

        CompanyDropdown(
            selected = company.ifBlank { anyCompany },
            options = remember(companies, anyCompany) { listOf(anyCompany) + companies },
            onSelect = { picked -> company = if (picked == anyCompany) "" else picked }
        )

        Spacer(Modifier.size(Spacing.sm))

        SwitchRow(
            title = stringResource(R.string.filter_has_phone),
            checked = hasPhone,
            onCheckedChange = { hasPhone = it }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SwitchRow(
            title = stringResource(R.string.filter_has_email),
            checked = hasEmail,
            onCheckedChange = { hasEmail = it }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SwitchRow(
            title = stringResource(R.string.filter_verified_only),
            checked = verifiedOnly,
            onCheckedChange = { verifiedOnly = it }
        )

        Spacer(Modifier.size(Spacing.xl))

        HrPrimaryButton(
            text = stringResource(R.string.filter_apply),
            onClick = {
                onApply(
                    ContactFilter(
                        company = company,
                        hasPhone = hasPhone,
                        hasEmail = hasEmail,
                        verifiedOnly = verifiedOnly
                    )
                )
            }
        )

        Spacer(Modifier.size(Spacing.xxl))
    }
}

/**
 * Read-only dropdown over the companies that actually exist in the user's contacts.
 *
 * Not typable on purpose: the filter matches a company exactly, so a free-text field would let the
 * user enter something that can never match and give them an empty list with no explanation.
 */
@Composable
private fun CompanyDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(R.string.company)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.small,
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun FilterSheetPreview() {
    PreviewSurface {
        FilterSheetContent(
            companies = listOf("Acme Corporation", "Globex", "Initech"),
            initialFilter = ContactFilter(company = "Globex", hasEmail = true),
            onApply = {},
            onClearAll = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun FilterSheetEmptyPreview() {
    PreviewSurface {
        FilterSheetContent(
            companies = emptyList(),
            initialFilter = ContactFilter(),
            onApply = {},
            onClearAll = {}
        )
    }
}
