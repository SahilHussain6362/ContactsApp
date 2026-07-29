package com.mohdhussain.hrcontacts.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.add.AddContactBottomSheetFragment
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme
import kotlinx.coroutines.launch

/**
 * Host for [ContactListScreen].
 *
 * Keeps everything that is not drawing: the ViewModel, navigation, the bulk-email intent, and the
 * filter sheet — which is still shown through `childFragmentManager` so it goes on sharing this
 * fragment's [ContactListViewModel] exactly as it did before.
 *
 * Also hosts the Bookmarks / Your Contacts folders pushed from [com.mohdhussain.hrcontacts.ui.hub.ContactsHubFragment]
 * — same class, a different nav_graph destination id (`contactsFilteredFragment`) and a "scope"
 * argument, which is why navigation below goes to destination ids directly rather than through a
 * named action: an action declared on one of those two graph nodes would not exist on the other.
 */
class ContactListFragment : Fragment() {

    private lateinit var viewModel: ContactListViewModel

    private val scope: ContactListScope by lazy {
        arguments?.getString("scope")?.let { runCatching { ContactListScope.valueOf(it) }.getOrNull() }
            ?: ContactListScope.ALL
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            ContactListViewModelFactory(requireContext(), scope)
        )[ContactListViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                // Owned here rather than in the ViewModel: the query the ViewModel holds is the
                // debounced one, and it exposes no scope back, so these two are the host's to keep.
                // rememberSaveable is what makes a rotation no longer clear the search box.
                var query by rememberSaveable { mutableStateOf("") }
                var scopeIndex by rememberSaveable { mutableIntStateOf(0) }

                val items by viewModel.listItems.observeAsState(initial = emptyList())
                val filter by viewModel.filter.observeAsState(initial = ContactFilter())
                val selectionMode by viewModel.isSelectionMode.observeAsState(initial = false)
                val selectedIds by viewModel.selectedIds.observeAsState(initial = emptySet())

                HrContactsTheme {
                    ContactListScreen(
                        items = items,
                        query = query,
                        onQueryChange = { new ->
                            query = new
                            viewModel.onSearchQueryChanged(new)
                        },
                        scopeIndex = scopeIndex,
                        onScopeChange = { index ->
                            scopeIndex = index
                            viewModel.setSearchScope(SCOPES[index])
                        },
                        activeFilterCount = filter.activeCount,
                        onFilterClick = ::showFilterSheet,
                        selectionMode = selectionMode,
                        selectedCount = selectedIds.size,
                        onContactClick = { contactId ->
                            findNavController().navigate(
                                R.id.contactDetailFragment,
                                bundleOf("contactId" to contactId)
                            )
                        },
                        onContactLongClick = { contactId ->
                            if (viewModel.isSelectionMode.value == true) {
                                viewModel.toggleSelection(contactId)
                            } else {
                                viewModel.enterSelectionMode(contactId)
                            }
                        },
                        onBookmarkClick = viewModel::toggleBookmark,
                        onHeaderSelectionClick = viewModel::selectAllFromCompany,
                        onExitSelection = viewModel::clearSelection,
                        onSelectAll = viewModel::selectAll,
                        onSendEmail = ::sendBulkEmail,
                        onDeleteSelected = viewModel::deleteSelected,
                        onAddContact = ::showAddContactSheet,
                        onClearFilters = viewModel::clearFilter,
                        title = stringResource(
                            when (scope) {
                                ContactListScope.ALL -> R.string.nav_home
                                ContactListScope.MINE -> R.string.contacts_hub_your_contacts
                                ContactListScope.BOOKMARKED -> R.string.contacts_hub_bookmarks
                            }
                        ),
                        onNavigateBack = if (scope == ContactListScope.ALL) {
                            null
                        } else {
                            { findNavController().popBackStack() }
                        },
                        emptyIcon = painterResource(
                            when (scope) {
                                ContactListScope.ALL -> R.drawable.ic_contacts
                                ContactListScope.MINE -> R.drawable.ic_contacts
                                ContactListScope.BOOKMARKED -> R.drawable.ic_bookmark_border
                            }
                        ),
                        emptyTitle = stringResource(
                            when (scope) {
                                ContactListScope.ALL -> R.string.empty_contacts_title
                                ContactListScope.MINE -> R.string.empty_added_title
                                ContactListScope.BOOKMARKED -> R.string.empty_bookmarks_title
                            }
                        ),
                        emptyBody = stringResource(
                            when (scope) {
                                ContactListScope.ALL -> R.string.empty_contacts_body
                                ContactListScope.MINE -> R.string.empty_added_body
                                ContactListScope.BOOKMARKED -> R.string.empty_bookmarks_body
                            }
                        ),
                        emptyActionText = if (scope == ContactListScope.ALL) {
                            stringResource(R.string.empty_contacts_action)
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    private fun showFilterSheet() {
        // childFragmentManager keeps this fragment as the sheet's parent, which is how the sheet
        // reaches the shared ContactListViewModel.
        if (childFragmentManager.findFragmentByTag(FilterBottomSheetFragment.TAG) == null) {
            FilterBottomSheetFragment().show(
                childFragmentManager,
                FilterBottomSheetFragment.TAG
            )
        }
    }

    private fun showAddContactSheet() {
        if (childFragmentManager.findFragmentByTag(AddContactBottomSheetFragment.TAG) == null) {
            AddContactBottomSheetFragment().show(
                childFragmentManager,
                AddContactBottomSheetFragment.TAG
            )
        }
    }

    private fun sendBulkEmail() {
        val emails = viewModel.getSelectedEmails()
        if (emails.isEmpty()) return

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_BCC, emails.toTypedArray())
        }

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.no_email_app, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { viewModel.syncNow() }
    }

    private companion object {
        /** Index order must match the chip order in [ContactListScreen]. */
        private val SCOPES = listOf(SearchScope.ALL, SearchScope.NAME, SearchScope.COMPANY)
    }
}
