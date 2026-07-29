package com.mohdhussain.hrcontacts.ui.add

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.components.FormSectionHeader
import com.mohdhussain.hrcontacts.ui.components.HrIconButton
import com.mohdhussain.hrcontacts.ui.components.HrPrimaryButton
import com.mohdhussain.hrcontacts.ui.components.HrTextField
import com.mohdhussain.hrcontacts.ui.components.HrTopAppBar
import com.mohdhussain.hrcontacts.ui.components.ResponsiveContent
import com.mohdhussain.hrcontacts.ui.components.SwitchRow
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/** The form's starting values: blank for a new contact, the saved ones when editing. */
data class ContactFormValues(
    val name: String = "",
    val company: String = "",
    val mobile: String = "",
    val emails: List<String> = listOf(""),
    val linkedin: String = "",
    val isPrivate: Boolean = false
)

/**
 * Add or edit a contact.
 *
 * [initialValues] is null while an existing contact is still being read, which is the only reason
 * this screen has a loading state. Once the form is composed it owns its own values and the initial
 * ones are never applied again — the old screen re-applied them on every emission from the database,
 * so a background sync landing mid-edit would wipe out what had been typed.
 *
 * All the field values are `rememberSaveable`, including the list of emails. Rotating the old screen
 * lost every email row, because they were Views inflated into a LinearLayout with nothing saving them.
 */
@Composable
fun AddContactScreen(
    isEditing: Boolean,
    initialValues: ContactFormValues?,
    onBack: () -> Unit,
    onSave: (ContactFormValues) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HrTopAppBar(
                title = stringResource(
                    if (isEditing) R.string.edit_contact else R.string.add_contact
                ),
                onNavigateBack = onBack
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize()
        ) {
            if (initialValues == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                ContactForm(initial = initialValues, onSave = onSave)
            }
        }
    }
}

@Composable
private fun ContactForm(
    initial: ContactFormValues,
    onSave: (ContactFormValues) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var company by rememberSaveable { mutableStateOf(initial.company) }
    var mobile by rememberSaveable { mutableStateOf(initial.mobile) }
    var linkedin by rememberSaveable { mutableStateOf(initial.linkedin) }
    var isPrivate by rememberSaveable { mutableStateOf(initial.isPrivate) }
    var emails by rememberSaveable {
        mutableStateOf(initial.emails.ifEmpty { listOf("") }.toList())
    }

    var errors by remember { mutableStateOf(FormErrors()) }

    val companyRequired = stringResource(R.string.company_required)
    val mobileInvalid = stringResource(R.string.mobile_invalid)
    val emailInvalid = stringResource(R.string.email_invalid)
    val eitherRequired = stringResource(R.string.mobile_or_email_required)

    fun attemptSave() {
        val result = validate(
            company = company,
            mobile = mobile,
            emails = emails,
            companyRequired = companyRequired,
            mobileInvalid = mobileInvalid,
            emailInvalid = emailInvalid,
            eitherRequired = eitherRequired
        )
        errors = result
        if (result.isValid) {
            onSave(
                ContactFormValues(
                    name = name.trim(),
                    company = company.trim(),
                    mobile = mobile.trim(),
                    emails = emails.map { it.trim() }.filter { it.isNotEmpty() },
                    linkedin = linkedin.trim(),
                    isPrivate = isPrivate
                )
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ResponsiveContent(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
            ) {
                FormSectionHeader(title = stringResource(R.string.form_section_identity))

                HrTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.name),
                    leadingIcon = painterResource(R.drawable.ic_person),
                    keyboardType = KeyboardType.Text
                )
                Spacer(Modifier.size(Spacing.sm))
                HrTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = stringResource(R.string.company),
                    leadingIcon = painterResource(R.drawable.ic_business),
                    error = errors.company
                )

                FormSectionHeader(title = stringResource(R.string.form_section_reach))

                HrTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = stringResource(R.string.mobile),
                    leadingIcon = painterResource(R.drawable.ic_phone),
                    keyboardType = KeyboardType.Phone,
                    error = errors.mobile,
                    supportingText = stringResource(R.string.mobile_optional_hint)
                )

                Spacer(Modifier.size(Spacing.sm))

                emails.forEachIndexed { index, email ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        HrTextField(
                            value = email,
                            onValueChange = { new ->
                                emails = emails.toMutableList().also { it[index] = new }
                            },
                            label = stringResource(R.string.email),
                            leadingIcon = painterResource(R.drawable.ic_email),
                            keyboardType = KeyboardType.Email,
                            error = errors.emails.getOrNull(index),
                            modifier = Modifier.weight(1f)
                        )
                        // The first row has no remove button: a contact form always offers at
                        // least one email field, so removing the only one leaves nothing behind.
                        if (emails.size > 1) {
                            Spacer(Modifier.width(Spacing.sm))
                            HrIconButton(
                                onClick = {
                                    emails = emails.toMutableList().also { it.removeAt(index) }
                                    errors = errors.copy(emails = emptyList())
                                },
                                icon = painterResource(R.drawable.ic_delete),
                                contentDescription = stringResource(R.string.email_remove),
                                modifier = Modifier.padding(top = Spacing.sm)
                            )
                        }
                    }
                    Spacer(Modifier.size(Spacing.sm))
                }

                if (errors.emailsGeneral != null) {
                    Text(
                        text = errors.emailsGeneral.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )
                }

                TextButton(onClick = { emails = emails + "" }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.add_another_email),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = Spacing.xs)
                    )
                }

                Spacer(Modifier.size(Spacing.sm))

                HrTextField(
                    value = linkedin,
                    onValueChange = { linkedin = it },
                    label = stringResource(R.string.linkedin_profile),
                    leadingIcon = painterResource(R.drawable.ic_linkedin),
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                )

                FormSectionHeader(title = stringResource(R.string.form_section_visibility))

                // No verified toggle here by design: that flag is server-owned and read-only in
                // the app. It still shows as a badge on the list and detail screens.
                SwitchRow(
                    title = stringResource(R.string.private_contact),
                    description = stringResource(R.string.private_contact_hint),
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it }
                )

                Spacer(Modifier.size(Spacing.xl))
            }
        }

        // Pinned rather than scrolled to: on a form this long, a save button at the bottom of the
        // content is a scroll away from wherever the user finished typing.
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ResponsiveContent {
                    // imePadding only: the enclosing Scaffold already applies the system-bar insets
                    // to its content, so padding for the navigation bar again here would leave a
                    // second gap below the button. The keyboard is not a system-bar inset, and
                    // enableEdgeToEdge turns the manifest's adjustResize into a no-op, so this is
                    // what lifts the button clear of it.
                    Box(
                        modifier = Modifier
                            .padding(Spacing.lg)
                            .imePadding()
                    ) {
                        HrPrimaryButton(
                            text = stringResource(R.string.save),
                            onClick = ::attemptSave
                        )
                    }
                }
            }
        }
    }
}

/**
 * Which fields are in error, and why.
 *
 * [emailsGeneral] is the one message that belongs to the email block as a whole rather than to a
 * single row — "at least one of mobile or email is required".
 */
private data class FormErrors(
    val company: String? = null,
    val mobile: String? = null,
    val emails: List<String?> = emptyList(),
    val emailsGeneral: String? = null
) {
    val isValid: Boolean
        get() = company == null && mobile == null && emailsGeneral == null &&
            emails.all { it == null }
}

/**
 * The form rules, unchanged from the View implementation:
 *
 * - the name is optional and becomes "Anonymous" in the ViewModel,
 * - the company is required,
 * - the mobile is optional but must look like a phone number if given,
 * - each email is optional but must look like an email if given,
 * - and at least one of mobile or email has to be there, since a contact you cannot reach is not
 *   worth saving.
 */
private fun validate(
    company: String,
    mobile: String,
    emails: List<String>,
    companyRequired: String,
    mobileInvalid: String,
    emailInvalid: String,
    eitherRequired: String
): FormErrors {
    val trimmedCompany = company.trim()
    val trimmedMobile = mobile.trim()
    val trimmedEmails = emails.map { it.trim() }

    val mobileValid = trimmedMobile.isEmpty() || trimmedMobile.matches(MOBILE_PATTERN)
    val emailErrors = trimmedEmails.map { email ->
        if (email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            null
        } else {
            emailInvalid
        }
    }
    val allEmailsValid = emailErrors.all { it == null }
    val hasAnyEmail = trimmedEmails.any { it.isNotEmpty() }

    val nothingToReachOn = mobileValid && allEmailsValid &&
        trimmedMobile.isEmpty() && !hasAnyEmail

    return FormErrors(
        company = companyRequired.takeIf { trimmedCompany.isEmpty() },
        mobile = when {
            !mobileValid -> mobileInvalid
            nothingToReachOn -> eitherRequired
            else -> null
        },
        emails = emailErrors,
        emailsGeneral = eitherRequired.takeIf { nothingToReachOn }
    )
}

private val MOBILE_PATTERN = Regex("^\\+?[0-9]{7,15}$")

// ---------------------------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------------------------

@LightDarkPreview
@Composable
private fun AddContactEmptyPreview() {
    PreviewSurface {
        AddContactScreen(
            isEditing = false,
            initialValues = ContactFormValues(),
            onBack = {},
            onSave = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun AddContactEditingPreview() {
    PreviewSurface {
        AddContactScreen(
            isEditing = true,
            initialValues = ContactFormValues(
                name = "Priya Sharma",
                company = "Acme Corporation",
                mobile = "+919876543210",
                emails = listOf("priya.sharma@acme.com", "priya@personal.example"),
                linkedin = "https://linkedin.com/in/priyasharma",
                isPrivate = true
            ),
            onBack = {},
            onSave = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun AddContactLoadingPreview() {
    PreviewSurface {
        AddContactScreen(
            isEditing = true,
            initialValues = null,
            onBack = {},
            onSave = {}
        )
    }
}
