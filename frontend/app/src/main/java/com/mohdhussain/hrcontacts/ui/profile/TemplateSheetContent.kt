package com.mohdhussain.hrcontacts.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
 * Writes one message template.
 *
 * One sheet serves both types: the heading field is simply absent for WhatsApp, which has no subject
 * line to prefill. The 200 and 5000 character caps are the server's, surfaced as live counters so the
 * limit is visible while typing rather than discovered on save.
 */
@Composable
fun TemplateSheetContent(
    type: TemplateType,
    isEditing: Boolean,
    initialHeading: String,
    initialBody: String,
    onSave: (heading: String, body: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var heading by rememberSaveable { mutableStateOf(initialHeading) }
    var body by rememberSaveable { mutableStateOf(initialBody) }
    var headingError by remember { mutableStateOf<String?>(null) }
    var bodyError by remember { mutableStateOf<String?>(null) }

    val headingRequired = stringResource(R.string.templates_heading_required)
    val bodyRequired = stringResource(
        when (type) {
            TemplateType.EMAIL -> R.string.templates_body_required
            TemplateType.WHATSAPP -> R.string.templates_message_required
        }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = Spacing.lg)
    ) {
        SheetHeader(
            title = stringResource(
                when (type) {
                    TemplateType.EMAIL ->
                        if (isEditing) R.string.templates_edit_email else R.string.templates_new_email
                    TemplateType.WHATSAPP ->
                        if (isEditing) R.string.templates_edit_whatsapp else R.string.templates_new_whatsapp
                }
            ),
            description = stringResource(
                when (type) {
                    TemplateType.EMAIL -> R.string.templates_hint_email
                    TemplateType.WHATSAPP -> R.string.templates_hint_whatsapp
                }
            )
        )

        Spacer(Modifier.size(Spacing.lg))

        if (type == TemplateType.EMAIL) {
            HrTextField(
                value = heading,
                onValueChange = {
                    heading = it
                    if (headingError != null) headingError = null
                },
                label = stringResource(R.string.templates_heading),
                error = headingError,
                counterMax = HEADING_MAX,
                singleLine = false,
                maxLines = 2
            )
            Spacer(Modifier.size(Spacing.md))
        }

        HrTextField(
            value = body,
            onValueChange = {
                body = it
                if (bodyError != null) bodyError = null
            },
            label = stringResource(
                when (type) {
                    TemplateType.EMAIL -> R.string.templates_body
                    TemplateType.WHATSAPP -> R.string.templates_message
                }
            ),
            error = bodyError,
            counterMax = BODY_MAX,
            singleLine = false,
            minLines = 5,
            imeAction = ImeAction.Default
        )

        Spacer(Modifier.size(Spacing.xl))

        HrPrimaryButton(
            text = stringResource(R.string.save),
            onClick = {
                val trimmedBody = body.trim()
                val trimmedHeading = heading.trim()

                bodyError = bodyRequired.takeIf { trimmedBody.isEmpty() }
                headingError = if (type == TemplateType.EMAIL && trimmedHeading.isEmpty()) {
                    headingRequired
                } else {
                    null
                }

                if (bodyError == null && headingError == null) {
                    onSave(trimmedHeading, trimmedBody)
                }
            }
        )

        Spacer(Modifier.size(Spacing.xxl))
    }
}

/** The server's caps, mirrored so the counters stop typing at the same place the API would reject. */
private const val HEADING_MAX = 200
private const val BODY_MAX = 5000

@LightDarkPreview
@Composable
private fun TemplateSheetEmailPreview() {
    PreviewSurface {
        TemplateSheetContent(
            type = TemplateType.EMAIL,
            isEditing = true,
            initialHeading = "Following up on your JD",
            initialBody = "Hi,\n\nI came across the opening at your company and wanted to share my profile.",
            onSave = { _, _ -> }
        )
    }
}

@LightDarkPreview
@Composable
private fun TemplateSheetWhatsappPreview() {
    PreviewSurface {
        TemplateSheetContent(
            type = TemplateType.WHATSAPP,
            isEditing = false,
            initialHeading = "",
            initialBody = "",
            onSave = { _, _ -> }
        )
    }
}
