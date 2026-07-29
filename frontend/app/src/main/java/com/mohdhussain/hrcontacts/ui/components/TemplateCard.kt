package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.HrPill
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * One saved message template.
 *
 * [title] is null for WhatsApp templates, which have no subject line — in that case the message
 * itself gets the extra line the missing heading frees up.
 */
@Composable
fun TemplateCard(
    title: String?,
    preview: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    HrCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick, role = Role.Button)
                .padding(start = Spacing.lg, top = Spacing.md, end = Spacing.sm, bottom = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (title == null) 3 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = if (title == null) 0.dp else Spacing.xxs)
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(Sizes.MinTouchTarget)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.templates_delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Sizes.Icon)
                )
            }
        }
    }
}

/**
 * The heading above each template section: what it is, how many of the cap are used, and the add
 * button.
 *
 * The add button stays enabled at the cap rather than greying out — the host answers the tap with the
 * message explaining that one template has to go first, which is more use than a dead control.
 */
@Composable
fun TemplateSectionHeader(
    title: String,
    count: Int,
    max: Int,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    emptyText: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.templates_count, count, max),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
            FilledTonalButton(
                onClick = onAdd,
                shape = HrPill,
                modifier = Modifier.padding(start = Spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(Sizes.IconSmall)
                )
                Text(
                    text = stringResource(R.string.templates_add),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = Spacing.xs)
                )
            }
        }
        if (count == 0 && emptyText != null) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun TemplateSectionPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            TemplateSectionHeader(
                title = stringResource(R.string.templates_email_title),
                count = 2,
                max = 3,
                onAdd = {}
            )
            TemplateCard(
                title = "Following up on your JD",
                preview = "Hi, I came across the opening at your company and wanted to share my profile for consideration.",
                onClick = {},
                onDelete = {}
            )
            TemplateCard(
                title = null,
                preview = "Hi! I saw your posting about the backend role. Would it be alright if I sent across my CV?",
                onClick = {},
                onDelete = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun TemplateSectionEmptyPreview() {
    PreviewSurface {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            TemplateSectionHeader(
                title = stringResource(R.string.templates_whatsapp_title),
                count = 0,
                max = 3,
                onAdd = {},
                emptyText = stringResource(R.string.templates_none_whatsapp)
            )
        }
    }
}
