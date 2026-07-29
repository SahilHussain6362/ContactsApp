package com.mohdhussain.hrcontacts.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.components.CompanySectionHeader
import com.mohdhussain.hrcontacts.ui.components.ConfirmDialog
import com.mohdhussain.hrcontacts.ui.components.ContactCard
import com.mohdhussain.hrcontacts.ui.components.EmptyState
import com.mohdhussain.hrcontacts.ui.components.HrSearchField
import com.mohdhussain.hrcontacts.ui.components.HrTopAppBar
import com.mohdhussain.hrcontacts.ui.components.SelectionTopAppBar
import com.mohdhussain.hrcontacts.ui.components.SingleChoiceChipRow
import com.mohdhussain.hrcontacts.ui.components.columnsFor
import com.mohdhussain.hrcontacts.ui.theme.HrPill
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * The records list: every HR contact the user can see, grouped by company.
 *
 * Stateless: the search text and the lit scope chip arrive as parameters and are held by the host as
 * `rememberSaveable`, because the ViewModel debounces the query and never exposes the current scope
 * back. The only state owned here is whether the delete confirmation is showing, which nothing
 * outside this screen has any use for.
 *
 * Multi-select is drawn here rather than by an `ActionMode`: the top bar becomes
 * [SelectionTopAppBar] and the list rows grow checkboxes. The ViewModel calls behind those actions
 * are exactly the ones the old menu made.
 */
@Composable
fun ContactListScreen(
    items: List<ListItem>,
    query: String,
    onQueryChange: (String) -> Unit,
    scopeIndex: Int,
    onScopeChange: (Int) -> Unit,
    activeFilterCount: Int,
    onFilterClick: () -> Unit,
    selectionMode: Boolean,
    selectedCount: Int,
    onContactClick: (Long) -> Unit,
    onContactLongClick: (Long) -> Unit,
    onBookmarkClick: (Long) -> Unit,
    onHeaderSelectionClick: (String) -> Unit,
    onExitSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onSendEmail: () -> Unit,
    onDeleteSelected: () -> Unit,
    onAddContact: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.app_name),
    onNavigateBack: (() -> Unit)? = null,
    emptyIcon: Painter = painterResource(R.drawable.ic_contacts),
    emptyTitle: String = stringResource(R.string.empty_contacts_title),
    emptyBody: String? = stringResource(R.string.empty_contacts_body),
    emptyActionText: String? = stringResource(R.string.empty_contacts_action)
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // Horizontal only. The tab bar sits in its own row below this screen and pads itself for the
        // system navigation bar, so adding a bottom inset here would leave a gap above it. The top
        // bar handles the status bar itself. See the comment in activity_main.xml.
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        topBar = {
            if (selectionMode) {
                SelectionTopAppBar(
                    selectedCount = selectedCount,
                    onClose = onExitSelection
                ) {
                    IconButton(onClick = onSelectAll) {
                        Icon(
                            painter = painterResource(R.drawable.ic_select_all),
                            contentDescription = stringResource(R.string.select_all),
                            modifier = Modifier.size(Sizes.Icon)
                        )
                    }
                    IconButton(onClick = onSendEmail) {
                        Icon(
                            painter = painterResource(R.drawable.ic_email),
                            contentDescription = stringResource(R.string.send_email),
                            modifier = Modifier.size(Sizes.Icon)
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete_selected),
                            modifier = Modifier.size(Sizes.Icon)
                        )
                    }
                }
            } else {
                HrTopAppBar(title = title, onNavigateBack = onNavigateBack)
            }
        }
    ) { scaffoldPadding ->
        Column(modifier = Modifier.padding(scaffoldPadding)) {
            SearchAndFilterBar(
                query = query,
                onQueryChange = onQueryChange,
                scopeIndex = scopeIndex,
                onScopeChange = onScopeChange,
                activeFilterCount = activeFilterCount,
                onFilterClick = onFilterClick
            )

            val hasContacts = items.any { it is ListItem.ContactRow }
            val narrowed = query.isNotBlank() || activeFilterCount > 0

            when {
                !hasContacts && narrowed -> EmptyState(
                    icon = painterResource(R.drawable.ic_filter),
                    title = stringResource(R.string.empty_results_title),
                    description = stringResource(R.string.empty_results_body),
                    actionText = if (activeFilterCount > 0) {
                        stringResource(R.string.empty_results_action)
                    } else {
                        null
                    },
                    onAction = if (activeFilterCount > 0) onClearFilters else null
                )

                !hasContacts -> EmptyState(
                    icon = emptyIcon,
                    title = emptyTitle,
                    description = emptyBody,
                    actionText = emptyActionText,
                    onAction = if (emptyActionText != null) onAddContact else null
                )

                else -> ContactGrid(
                    items = items,
                    selectionMode = selectionMode,
                    onContactClick = onContactClick,
                    onContactLongClick = onContactLongClick,
                    onBookmarkClick = onBookmarkClick,
                    onHeaderSelectionClick = onHeaderSelectionClick
                )
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.delete_selected_title),
            message = stringResource(R.string.delete_selected_message, selectedCount),
            confirmText = stringResource(R.string.delete),
            destructive = true,
            onConfirm = {
                showDeleteConfirm = false
                onDeleteSelected()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

/**
 * Search box, filter button and scope chips. Pinned rather than scrolled away — these are the tools
 * the screen exists for, and a list of a few hundred contacts is exactly when you do not want to
 * scroll back up to reach them.
 */
@Composable
private fun SearchAndFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    scopeIndex: Int,
    onScopeChange: (Int) -> Unit,
    activeFilterCount: Int,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HrSearchField(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(Spacing.sm))
            FilterButton(activeCount = activeFilterCount, onClick = onFilterClick)
        }
        Spacer(Modifier.size(Spacing.sm))
        SingleChoiceChipRow(
            options = listOf(
                stringResource(R.string.filter_all),
                stringResource(R.string.filter_by_name),
                stringResource(R.string.filter_by_company)
            ),
            selectedIndex = scopeIndex,
            onSelect = onScopeChange
        )
        Spacer(Modifier.size(Spacing.sm))
    }
}

/**
 * Opens the filter sheet, and carries a badge with how many criteria are live.
 *
 * The badge replaces the old "Filter (2)" label: the count is the part that matters and a numeric
 * badge reads it faster than a parenthesis, without the button changing width each time a filter is
 * added.
 */
@Composable
private fun FilterButton(
    activeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val active = activeCount > 0
    BadgedBox(
        modifier = modifier,
        badge = {
            if (active) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(text = "$activeCount")
                }
            }
        }
    ) {
        FilledTonalButton(
            onClick = onClick,
            shape = HrPill,
            modifier = Modifier.height(Sizes.MinTouchTarget),
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_filter),
                contentDescription = stringResource(R.string.filter_contacts),
                modifier = Modifier.size(Sizes.IconSmall)
            )
            Text(
                text = stringResource(R.string.filter),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }
    }
}

/**
 * The grouped list.
 *
 * A grid rather than a column so that a tablet or a landscape phone fills the width with a second and
 * third column instead of one very wide row. Company headers span every column, which is what keeps
 * a group readable as a group once there is more than one.
 */
@Composable
private fun ContactGrid(
    items: List<ListItem>,
    selectionMode: Boolean,
    onContactClick: (Long) -> Unit,
    onContactLongClick: (Long) -> Unit,
    onBookmarkClick: (Long) -> Unit,
    onHeaderSelectionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = columnsFor(maxWidth)
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(
                start = Spacing.md,
                end = Spacing.md,
                bottom = Spacing.md
            ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(
                items = items,
                key = { item ->
                    when (item) {
                        is ListItem.Header -> "header-${item.company}"
                        is ListItem.ContactRow -> "contact-${item.id}"
                    }
                },
                span = { item ->
                    when (item) {
                        is ListItem.Header -> GridItemSpan(maxLineSpan)
                        is ListItem.ContactRow -> GridItemSpan(1)
                    }
                }
            ) { item ->
                when (item) {
                    is ListItem.Header -> CompanySectionHeader(
                        company = item.company,
                        count = item.count,
                        selectionState = if (selectionMode) {
                            item.selectionState.toToggleableState()
                        } else {
                            null
                        },
                        onSelectionClick = { onHeaderSelectionClick(item.company) }
                    )

                    is ListItem.ContactRow -> ContactCard(
                        name = item.name,
                        company = item.company,
                        emails = item.emails,
                        verified = item.verified,
                        bookmarked = item.bookmarked,
                        // The company is already the heading above this group.
                        showCompany = false,
                        selectionMode = selectionMode,
                        selected = item.isSelected,
                        onClick = {
                            if (selectionMode) onContactLongClick(item.id) else onContactClick(item.id)
                        },
                        onLongClick = { onContactLongClick(item.id) },
                        onBookmarkClick = { onBookmarkClick(item.id) }
                    )
                }
            }
        }
    }
}

private fun SelectionState.toToggleableState(): ToggleableState = when (this) {
    SelectionState.ALL -> ToggleableState.On
    SelectionState.PARTIAL -> ToggleableState.Indeterminate
    SelectionState.NONE -> ToggleableState.Off
}

// ---------------------------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------------------------

private val previewItems = listOf(
    ListItem.Header("Acme Corporation", 2, SelectionState.NONE),
    ListItem.ContactRow(
        1, "Priya Sharma", "Acme Corporation", "+919876543210",
        listOf("priya.sharma@acme.com", "priya@alt.com"), true, true, false
    ),
    ListItem.ContactRow(
        2, "Rahul Verma", "Acme Corporation", "+919876500000",
        listOf("rahul@acme.com"), false, false, false
    ),
    ListItem.Header("Globex", 1, SelectionState.NONE),
    ListItem.ContactRow(
        3, "Aisha Khan", "Globex", "", listOf("aisha.khan@globex.example"), true, false, false
    )
)

private val previewSelectionItems = listOf(
    ListItem.Header("Acme Corporation", 2, SelectionState.PARTIAL),
    ListItem.ContactRow(
        1, "Priya Sharma", "Acme Corporation", "+919876543210",
        listOf("priya.sharma@acme.com"), true, true, true
    ),
    ListItem.ContactRow(
        2, "Rahul Verma", "Acme Corporation", "+919876500000",
        listOf("rahul@acme.com"), false, false, false
    )
)

@Composable
private fun PreviewList(
    items: List<ListItem>,
    query: String = "",
    activeFilterCount: Int = 0,
    selectionMode: Boolean = false,
    selectedCount: Int = 0
) {
    ContactListScreen(
        items = items,
        query = query,
        onQueryChange = {},
        scopeIndex = 0,
        onScopeChange = {},
        activeFilterCount = activeFilterCount,
        onFilterClick = {},
        selectionMode = selectionMode,
        selectedCount = selectedCount,
        onContactClick = {},
        onContactLongClick = {},
        onBookmarkClick = {},
        onHeaderSelectionClick = {},
        onExitSelection = {},
        onSelectAll = {},
        onSendEmail = {},
        onDeleteSelected = {},
        onAddContact = {},
        onClearFilters = {}
    )
}

@LightDarkPreview
@Composable
private fun ContactListPopulatedPreview() {
    PreviewSurface { PreviewList(items = previewItems, activeFilterCount = 2) }
}

@LightDarkPreview
@Composable
private fun ContactListSelectionPreview() {
    PreviewSurface {
        PreviewList(
            items = previewSelectionItems,
            selectionMode = true,
            selectedCount = 1
        )
    }
}

@LightDarkPreview
@Composable
private fun ContactListEmptyPreview() {
    PreviewSurface { PreviewList(items = emptyList()) }
}

@LightDarkPreview
@Composable
private fun ContactListNoResultsPreview() {
    PreviewSurface {
        PreviewList(items = emptyList(), query = "zzz", activeFilterCount = 1)
    }
}
