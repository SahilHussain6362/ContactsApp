package com.mohdhussain.hrcontacts.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.components.Avatar
import com.mohdhussain.hrcontacts.ui.components.ConfirmDialog
import com.mohdhussain.hrcontacts.ui.components.HrCard
import com.mohdhussain.hrcontacts.ui.components.HrIconButton
import com.mohdhussain.hrcontacts.ui.components.HrTopAppBar
import com.mohdhussain.hrcontacts.ui.components.TemplateCard
import com.mohdhussain.hrcontacts.ui.components.TemplateSectionHeader
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * The user's own page: who they are signed in as, and the message templates they send with.
 *
 * Bookmarked and added contacts used to live here as a pair of stat-tile-switched collections; they
 * now have their own nav bar tab, so this page is just identity plus templates.
 *
 * The whole page scrolls, header included: templates are usually few, and a pinned header on a
 * profile page spends space on something the user reads once.
 */
@Composable
fun ProfileScreen(
    displayName: String,
    email: String,
    provider: String?,
    templateItems: List<TemplateListItem>,
    canAddTemplate: (TemplateType) -> Boolean,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onAddTemplate: (TemplateType) -> Unit,
    onEditTemplate: (TemplateType, String) -> Unit,
    onDeleteTemplate: (TemplateType, String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var pendingTemplateDelete by remember { mutableStateOf<Pair<TemplateType, String>?>(null) }
    var limitReachedFor by remember { mutableStateOf<TemplateType?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Horizontal only, for the same reason as the records list: the tab bar below this screen
        // owns the system navigation bar area. See the comment in activity_main.xml.
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        topBar = {
            HrTopAppBar(title = stringResource(R.string.profile_title)) {
                IconButton(onClick = { showLogoutConfirm = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logout),
                        contentDescription = stringResource(R.string.logout),
                        modifier = Modifier.size(Sizes.Icon)
                    )
                }
            }
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.md,
                end = Spacing.md,
                bottom = Spacing.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item(key = "identity") {
                IdentityCard(
                    displayName = displayName,
                    email = email,
                    provider = provider,
                    onEditProfile = onEditProfile
                )
            }

            item(key = "templates-intro") {
                Text(
                    text = stringResource(R.string.profile_templates_intro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.sm)
                )
            }
            items(
                items = templateItems,
                key = { item ->
                    when (item) {
                        is TemplateListItem.Header -> "template-header-${item.type}"
                        is TemplateListItem.Row -> "template-${item.id}"
                    }
                }
            ) { item ->
                when (item) {
                    is TemplateListItem.Header -> TemplateSectionHeader(
                        title = stringResource(
                            when (item.type) {
                                TemplateType.EMAIL -> R.string.templates_email_title
                                TemplateType.WHATSAPP -> R.string.templates_whatsapp_title
                            }
                        ),
                        count = item.count,
                        max = TEMPLATES_PER_TYPE_LIMIT,
                        emptyText = stringResource(
                            when (item.type) {
                                TemplateType.EMAIL -> R.string.templates_none_email
                                TemplateType.WHATSAPP -> R.string.templates_none_whatsapp
                            }
                        ),
                        onAdd = {
                            // The cap is answered with a message rather than a greyed-out
                            // button: the point of the tap is to learn why nothing happened,
                            // and only a message can say that one template has to go first.
                            if (canAddTemplate(item.type)) {
                                onAddTemplate(item.type)
                            } else {
                                limitReachedFor = item.type
                            }
                        },
                        modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.xs)
                    )

                    is TemplateListItem.Row -> TemplateCard(
                        title = item.title,
                        preview = item.preview,
                        onClick = { onEditTemplate(item.type, item.id) },
                        onDelete = { pendingTemplateDelete = item.type to item.id }
                    )
                }
            }
        }
    }

    if (showLogoutConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.logout_title),
            message = stringResource(R.string.logout_confirm),
            confirmText = stringResource(R.string.logout),
            destructive = true,
            onConfirm = {
                showLogoutConfirm = false
                onLogout()
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }

    pendingTemplateDelete?.let { (type, id) ->
        ConfirmDialog(
            message = stringResource(R.string.templates_delete_confirm),
            confirmText = stringResource(R.string.delete),
            destructive = true,
            onConfirm = {
                pendingTemplateDelete = null
                onDeleteTemplate(type, id)
            },
            onDismiss = { pendingTemplateDelete = null }
        )
    }

    limitReachedFor?.let { type ->
        ConfirmDialog(
            title = stringResource(R.string.templates_limit_title),
            message = stringResource(
                when (type) {
                    TemplateType.EMAIL -> R.string.templates_limit_email
                    TemplateType.WHATSAPP -> R.string.templates_limit_whatsapp
                },
                TEMPLATES_PER_TYPE_LIMIT
            ),
            confirmText = stringResource(android.R.string.ok),
            dismissText = stringResource(R.string.cancel),
            onConfirm = { limitReachedFor = null },
            onDismiss = { limitReachedFor = null }
        )
    }
}

/** Who the user is signed in as. Email and provider are owned by the identity provider, so read-only. */
@Composable
private fun IdentityCard(
    displayName: String,
    email: String,
    provider: String?,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    HrCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(name = displayName, size = Sizes.AvatarLarge)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.md)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
                if (!provider.isNullOrBlank()) {
                    Text(
                        text = stringResource(
                            R.string.profile_signed_in_with,
                            provider.lowercase().replaceFirstChar(Char::uppercase)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.xxs)
                    )
                }
            }
            Spacer(Modifier.width(Spacing.sm))
            HrIconButton(
                onClick = onEditProfile,
                icon = painterResource(R.drawable.ic_edit),
                contentDescription = stringResource(R.string.profile_edit)
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------------------------

private val previewTemplates = listOf(
    TemplateListItem.Header(TemplateType.EMAIL, 1),
    TemplateListItem.Row(TemplateType.EMAIL, "e1", "Following up on your JD", "Hi, I came across the opening at your company and wanted to share my profile."),
    TemplateListItem.Header(TemplateType.WHATSAPP, 0)
)

@Composable
private fun PreviewProfile(templates: List<TemplateListItem> = previewTemplates) {
    ProfileScreen(
        displayName = "Mohd Hussain",
        email = "mohd.hussain@example.com",
        provider = "google",
        templateItems = templates,
        canAddTemplate = { true },
        onEditProfile = {},
        onLogout = {},
        onAddTemplate = {},
        onEditTemplate = { _, _ -> },
        onDeleteTemplate = { _, _ -> }
    )
}

@LightDarkPreview
@Composable
private fun ProfileTemplatesPreview() {
    PreviewSurface { PreviewProfile() }
}

@LightDarkPreview
@Composable
private fun ProfileEmptyPreview() {
    PreviewSurface { PreviewProfile(templates = emptyList()) }
}
