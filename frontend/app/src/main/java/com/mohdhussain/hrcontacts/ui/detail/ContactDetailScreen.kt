package com.mohdhussain.hrcontacts.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.components.Avatar
import com.mohdhussain.hrcontacts.ui.components.ChoiceDialog
import com.mohdhussain.hrcontacts.ui.components.ConfirmDialog
import com.mohdhussain.hrcontacts.ui.components.DetailInfoCard
import com.mohdhussain.hrcontacts.ui.components.EmptyState
import com.mohdhussain.hrcontacts.ui.components.HrCard
import com.mohdhussain.hrcontacts.ui.components.HrIconButton
import com.mohdhussain.hrcontacts.ui.components.HrTopAppBar
import com.mohdhussain.hrcontacts.ui.components.InfoValueRow
import com.mohdhussain.hrcontacts.ui.components.PrivateBadge
import com.mohdhussain.hrcontacts.ui.components.QuickAction
import com.mohdhussain.hrcontacts.ui.components.QuickActionRow
import com.mohdhussain.hrcontacts.ui.components.ResponsiveContent
import com.mohdhussain.hrcontacts.ui.components.VerifiedBadge
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.LocalHrColors
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * One contact, in full.
 *
 * The change of substance here is the row of labelled quick actions under the header. Calling,
 * messaging and mailing are why this screen gets opened, and they used to be three unlabelled 44dp
 * icons tucked into the corner of the mobile card — reachable only after reading the card, and only
 * if you knew what the glyphs meant. They are now the first thing under the name, named.
 *
 * The information cards keep a copy button each, and each email address keeps its own send button, so
 * a contact with three addresses can still be mailed at a specific one.
 */
@Composable
fun ContactDetailScreen(
    name: String,
    company: String,
    mobile: String,
    emails: List<String>,
    linkedinProfile: String,
    verified: Boolean,
    isPrivate: Boolean,
    bookmarked: Boolean,
    emailTemplateLabels: List<String>,
    whatsappTemplateLabels: List<String>,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    onCopyMobile: () -> Unit,
    onCopyEmail: (String) -> Unit,
    onCopyLinkedin: () -> Unit,
    onSendEmail: (email: String, templateIndex: Int?) -> Unit,
    onSendWhatsapp: (templateIndex: Int?) -> Unit,
    onOpenLinkedin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    // Non-null while the user is choosing which template to mail this address with.
    var emailPickerFor by remember { mutableStateOf<String?>(null) }
    var showWhatsappPicker by remember { mutableStateOf(false) }

    val mobileCopied = stringResource(R.string.mobile_copied)
    val emailCopied = stringResource(R.string.email_copied)
    val linkedinCopied = stringResource(R.string.linkedin_copied)

    fun toast(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    /**
     * With a single template there is nothing to choose between, so it is applied straight away — the
     * composer is still editable, so this takes nothing away. More than one and the user picks.
     */
    fun startEmail(email: String) {
        if (emailTemplateLabels.size <= 1) {
            onSendEmail(email, if (emailTemplateLabels.isEmpty()) null else 0)
        } else {
            emailPickerFor = email
        }
    }

    fun startWhatsapp() {
        if (whatsappTemplateLabels.size <= 1) {
            onSendWhatsapp(if (whatsappTemplateLabels.isEmpty()) null else 0)
        } else {
            showWhatsappPicker = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HrTopAppBar(
                title = stringResource(R.string.detail_title),
                onNavigateBack = onBack
            ) {
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        painter = painterResource(
                            if (bookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
                        ),
                        contentDescription = stringResource(
                            if (bookmarked) R.string.bookmark_remove else R.string.bookmark_add
                        ),
                        tint = if (bookmarked) {
                            LocalHrColors.current.bookmarkActive
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(Sizes.Icon)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.edit_contact),
                        modifier = Modifier.size(Sizes.Icon)
                    )
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Sizes.Icon)
                    )
                }
            }
        }
    ) { scaffoldPadding ->
        ResponsiveContent(
            modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
            ) {
                IdentityHeader(
                    name = name,
                    company = company,
                    verified = verified,
                    isPrivate = isPrivate
                )

                val hasMobile = mobile.isNotBlank()
                val hasEmails = emails.any { it.isNotBlank() }

                if (hasMobile || hasEmails) {
                    Text(
                        text = stringResource(R.string.detail_section_reach_out).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.sm)
                    )
                    QuickActionRow {
                        if (hasMobile) {
                            QuickAction(
                                icon = painterResource(R.drawable.ic_phone),
                                label = stringResource(R.string.action_call),
                                onClick = onCall
                            )
                            QuickAction(
                                icon = painterResource(R.drawable.ic_whatsapp),
                                label = stringResource(R.string.action_whatsapp),
                                onClick = ::startWhatsapp,
                                tint = LocalHrColors.current.whatsapp
                            )
                        }
                        if (hasEmails) {
                            QuickAction(
                                icon = painterResource(R.drawable.ic_email),
                                label = stringResource(R.string.action_email),
                                onClick = { startEmail(emails.first { it.isNotBlank() }) }
                            )
                        }
                    }
                }

                Spacer(Modifier.size(Spacing.xl))

                if (!hasMobile && !hasEmails && linkedinProfile.isBlank()) {
                    EmptyState(
                        icon = painterResource(R.drawable.ic_person),
                        title = stringResource(R.string.detail_no_details_title),
                        description = stringResource(R.string.detail_no_details_body),
                        actionText = stringResource(R.string.edit_contact),
                        onAction = onEdit
                    )
                }

                if (hasMobile) {
                    DetailInfoCard(
                        icon = painterResource(R.drawable.ic_phone),
                        label = stringResource(R.string.mobile)
                    ) {
                        InfoValueRow(value = mobile) {
                            HrIconButton(
                                onClick = {
                                    onCopyMobile()
                                    toast(mobileCopied)
                                },
                                icon = painterResource(R.drawable.ic_copy),
                                contentDescription = stringResource(R.string.copy_mobile)
                            )
                        }
                    }
                    Spacer(Modifier.size(Spacing.md))
                }

                if (hasEmails) {
                    DetailInfoCard(
                        icon = painterResource(R.drawable.ic_email),
                        label = stringResource(R.string.email)
                    ) {
                        emails.filter { it.isNotBlank() }.forEach { email ->
                            InfoValueRow(value = email) {
                                HrIconButton(
                                    onClick = {
                                        onCopyEmail(email)
                                        toast(emailCopied)
                                    },
                                    icon = painterResource(R.drawable.ic_copy),
                                    contentDescription = stringResource(R.string.copy_email)
                                )
                                Spacer(Modifier.width(Spacing.sm))
                                HrIconButton(
                                    onClick = { startEmail(email) },
                                    icon = painterResource(R.drawable.ic_email),
                                    contentDescription = stringResource(R.string.send_email)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.size(Spacing.md))
                }

                if (linkedinProfile.isNotBlank()) {
                    DetailInfoCard(
                        icon = painterResource(R.drawable.ic_linkedin),
                        label = stringResource(R.string.linkedin_profile)
                    ) {
                        InfoValueRow(value = linkedinProfile) {
                            HrIconButton(
                                onClick = {
                                    onCopyLinkedin()
                                    toast(linkedinCopied)
                                },
                                icon = painterResource(R.drawable.ic_copy),
                                contentDescription = stringResource(R.string.action_copy_linkedin)
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            HrIconButton(
                                onClick = onOpenLinkedin,
                                icon = painterResource(R.drawable.ic_linkedin),
                                contentDescription = stringResource(R.string.action_open_linkedin)
                            )
                        }
                    }
                }

                Spacer(Modifier.size(Spacing.xxl))
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.delete_contact_title),
            message = stringResource(R.string.delete_contact_message),
            confirmText = stringResource(R.string.delete),
            destructive = true,
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    emailPickerFor?.let { email ->
        ChoiceDialog(
            title = stringResource(R.string.templates_choose_email),
            options = emailTemplateLabels,
            onSelect = { index ->
                emailPickerFor = null
                onSendEmail(email, index)
            },
            onDismiss = { emailPickerFor = null }
        )
    }

    if (showWhatsappPicker) {
        ChoiceDialog(
            title = stringResource(R.string.templates_choose_whatsapp),
            options = whatsappTemplateLabels,
            onSelect = { index ->
                showWhatsappPicker = false
                onSendWhatsapp(index)
            },
            onDismiss = { showWhatsappPicker = false }
        )
    }
}

/** Avatar, name, company and the two status badges. */
@Composable
private fun IdentityHeader(
    name: String,
    company: String,
    verified: Boolean,
    isPrivate: Boolean,
    modifier: Modifier = Modifier
) {
    HrCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(name = name, size = Sizes.AvatarXLarge)
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.md)
            )
            if (company.isNotBlank()) {
                Text(
                    text = company,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
            Row(
                modifier = Modifier.padding(top = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                VerifiedBadge(verified = verified)
                if (isPrivate) PrivateBadge()
            }
        }
    }
}

/** Shown while the contact is being read out of the database. */
@Composable
fun ContactDetailLoading(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HrTopAppBar(
                title = stringResource(R.string.detail_title),
                onNavigateBack = onBack
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------------------------

@Composable
private fun PreviewDetail(
    mobile: String = "+91 98765 43210",
    emails: List<String> = listOf("priya.sharma@acme.com", "priya@personal.example"),
    linkedin: String = "https://linkedin.com/in/priyasharma",
    verified: Boolean = true,
    isPrivate: Boolean = false,
    bookmarked: Boolean = true
) {
    ContactDetailScreen(
        name = "Priya Sharma",
        company = "Acme Corporation",
        mobile = mobile,
        emails = emails,
        linkedinProfile = linkedin,
        verified = verified,
        isPrivate = isPrivate,
        bookmarked = bookmarked,
        emailTemplateLabels = listOf("Following up on your JD"),
        whatsappTemplateLabels = listOf("Intro message"),
        onBack = {}, onToggleBookmark = {}, onEdit = {}, onDelete = {}, onCall = {},
        onCopyMobile = {}, onCopyEmail = {}, onCopyLinkedin = {},
        onSendEmail = { _, _ -> }, onSendWhatsapp = {}, onOpenLinkedin = {}
    )
}

@LightDarkPreview
@Composable
private fun ContactDetailFullPreview() {
    PreviewSurface { PreviewDetail() }
}

@LightDarkPreview
@Composable
private fun ContactDetailPrivateUnverifiedPreview() {
    PreviewSurface {
        PreviewDetail(
            emails = emptyList(),
            linkedin = "",
            verified = false,
            isPrivate = true,
            bookmarked = false
        )
    }
}

@LightDarkPreview
@Composable
private fun ContactDetailNoDetailsPreview() {
    PreviewSurface {
        PreviewDetail(mobile = "", emails = emptyList(), linkedin = "", verified = false)
    }
}

@LightDarkPreview
@Composable
private fun ContactDetailLoadingPreview() {
    PreviewSurface { ContactDetailLoading(onBack = {}) }
}
