package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.LocalHrColors
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * One contact in a list.
 *
 * Two lines, not four: the name leads, and everything that qualifies it — company, first email, how
 * many more there are — shares the line beneath. The old row put company and email on separate lines
 * and let the verified badge compete with the name for horizontal space, which made long names
 * ellipsise early on a state that is usually the boring one.
 *
 * The email is deliberately *not* tinted as a link. Tapping the row opens the contact; it does not
 * open a mail composer, and colouring it like an action would promise otherwise.
 */
@Composable
fun ContactCard(
    name: String,
    company: String,
    emails: List<String>,
    verified: Boolean,
    bookmarked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCompany: Boolean = true,
    selectionMode: Boolean = false,
    selected: Boolean = false
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "contactCardContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            LocalHrColors.current.cardBorder
        },
        label = "contactCardBorder"
    )

    val selectionStateDescription = when {
        !selectionMode -> null
        selected -> stringResource(R.string.selection_state_selected)
        else -> stringResource(R.string.selection_state_not_selected)
    }

    HrCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = containerColor,
        borderColor = borderColor
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    role = Role.Button
                )
                .semantics {
                    if (selectionStateDescription != null) {
                        stateDescription = selectionStateDescription
                    }
                }
                .heightIn(min = Sizes.MinTouchTarget + Spacing.lg)
                .padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                // Reflects the row's state only; the row itself owns the tap so there is one
                // target rather than two competing ones.
                Checkbox(
                    checked = selected,
                    onCheckedChange = null,
                    modifier = Modifier.clearAndSetSemantics { }
                )
                Spacer(Modifier.width(Spacing.sm))
            }

            Avatar(name = name, size = Sizes.AvatarSmall)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.md)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    VerifiedBadge(verified = verified)
                }

                val subtitle = buildSubtitle(company = if (showCompany) company else "", emails = emails)
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = Spacing.xxs)
                    )
                }
            }

            // Hidden while multi-selecting: there, a tap on the row means "toggle selection", so a
            // second competing target would only misfire.
            if (!selectionMode) {
                BookmarkButton(bookmarked = bookmarked, onClick = onBookmarkClick)
            }
        }
    }
}

/** "Acme Corp · priya@acme.com +2" — whichever of those parts exist. */
private fun buildSubtitle(company: String, emails: List<String>): String {
    val emailPart = when {
        emails.isEmpty() -> ""
        emails.size == 1 -> emails.first()
        else -> "${emails.first()} +${emails.size - 1}"
    }
    return listOf(company, emailPart).filter { it.isNotBlank() }.joinToString("  ·  ")
}

/**
 * The company heading above a group of contacts.
 *
 * [selectionState] doubles as the visibility switch for the group checkbox: null while not
 * multi-selecting, and otherwise the tri-state that says whether none, some or all of the group's
 * contacts are picked.
 */
@Composable
fun CompanySectionHeader(
    company: String,
    count: Int,
    modifier: Modifier = Modifier,
    selectionState: ToggleableState? = null,
    onSelectionClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.xs,
                end = Spacing.xs,
                top = Spacing.lg,
                bottom = Spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_business),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Sizes.IconSmall)
        )
        Text(
            text = company,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = Spacing.sm)
                .weight(1f)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = Spacing.sm)
        )
        if (selectionState != null) {
            TriStateCheckbox(
                state = selectionState,
                onClick = onSelectionClick,
                modifier = Modifier
                    .size(Sizes.MinTouchTarget)
                    .semantics {
                        stateDescription = company
                    }
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------------------------

@LightDarkPreview
@Composable
private fun ContactCardPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            ContactCard(
                name = "Priya Sharma",
                company = "Acme Corporation",
                emails = listOf("priya.sharma@acme.com", "priya@personal.com", "p.s@alt.com"),
                verified = true,
                bookmarked = true,
                onClick = {}, onLongClick = {}, onBookmarkClick = {}
            )
            ContactCard(
                name = "Rahul Verma",
                company = "Acme Corporation",
                emails = listOf("rahul@acme.com"),
                verified = false,
                bookmarked = false,
                onClick = {}, onLongClick = {}, onBookmarkClick = {}
            )
            ContactCard(
                name = "Anonymous",
                company = "Unlisted Startup",
                emails = emptyList(),
                verified = false,
                bookmarked = false,
                onClick = {}, onLongClick = {}, onBookmarkClick = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun ContactCardSelectionPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            ContactCard(
                name = "Priya Sharma",
                company = "Acme Corporation",
                emails = listOf("priya.sharma@acme.com"),
                verified = true,
                bookmarked = true,
                onClick = {}, onLongClick = {}, onBookmarkClick = {},
                selectionMode = true,
                selected = true
            )
            ContactCard(
                name = "Rahul Verma",
                company = "Acme Corporation",
                emails = listOf("rahul@acme.com"),
                verified = false,
                bookmarked = false,
                onClick = {}, onLongClick = {}, onBookmarkClick = {},
                selectionMode = true,
                selected = false
            )
        }
    }
}

/** The overflow case: a long name against a long company and a verified badge. */
@Preview(name = "Long content", showBackground = true, widthDp = 320)
@Composable
private fun ContactCardOverflowPreview() {
    PreviewSurface(darkTheme = false) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            ContactCard(
                name = "Bartholomew Featherstonehaugh-Wentworth",
                company = "International Consolidated Holdings Group Limited",
                emails = listOf("bartholomew.featherstonehaugh@international-holdings.example"),
                verified = true,
                bookmarked = false,
                onClick = {}, onLongClick = {}, onBookmarkClick = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun CompanySectionHeaderPreview() {
    PreviewSurface {
        Column(modifier = Modifier.padding(Spacing.md)) {
            CompanySectionHeader(company = "Acme Corporation", count = 4)
            CompanySectionHeader(
                company = "Globex",
                count = 2,
                selectionState = ToggleableState.Indeterminate
            )
            CompanySectionHeader(
                company = "International Consolidated Holdings Group Limited",
                count = 3,
                selectionState = ToggleableState.On
            )
        }
    }
}
