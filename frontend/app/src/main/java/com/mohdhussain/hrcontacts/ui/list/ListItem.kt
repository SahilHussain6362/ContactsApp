package com.mohdhussain.hrcontacts.ui.list

/**
 * The shape of the records list, as [ContactListViewModel] builds it.
 *
 * These declarations used to live in `ContactListAdapter.kt`. They moved here unchanged — same
 * package, same names — because the ViewModels depend on them and the adapter does not survive the
 * move to Compose. Nothing about the data or the grouping logic changed.
 */

/** Whether none, some or all of a company's contacts are selected. */
enum class SelectionState { NONE, PARTIAL, ALL }

sealed class ListItem {
    data class Header(
        val company: String,
        val count: Int,
        val selectionState: SelectionState = SelectionState.NONE
    ) : ListItem()

    data class ContactRow(
        val id: Long,
        val name: String,
        val company: String,
        val mobile: String,
        val emails: List<String>,
        val verified: Boolean,
        val bookmarked: Boolean,
        val isSelected: Boolean
    ) : ListItem()
}
