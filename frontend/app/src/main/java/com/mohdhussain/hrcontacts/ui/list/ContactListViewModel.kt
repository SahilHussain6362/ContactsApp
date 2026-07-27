package com.mohdhussain.hrcontacts.ui.list

import android.content.Context
import androidx.lifecycle.*
import com.mohdhussain.hrcontacts.data.model.HrContact
import com.mohdhussain.hrcontacts.data.repository.ContactRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SearchScope { ALL, NAME, COMPANY }

/**
 * Criteria picked in the filter bottom sheet. Each field narrows the list only when set —
 * a blank [company] or a `false` toggle means "don't narrow on this" — so the default
 * instance matches everything. The three toggles are presence checks, not value matches.
 */
data class ContactFilter(
    val company: String = "",
    val hasPhone: Boolean = false,
    val hasEmail: Boolean = false,
    val verifiedOnly: Boolean = false
) {
    val activeCount: Int
        get() = listOf(
            company.isNotBlank(),
            hasPhone,
            hasEmail,
            verifiedOnly
        ).count { it }

    val isActive: Boolean get() = activeCount > 0
}

class ContactListViewModel(private val repository: ContactRepository) : ViewModel() {

    init {
        viewModelScope.launch { repository.syncNow() }
    }

    private val allContacts: LiveData<List<HrContact>> = repository.allContacts

    private val _searchQuery = MutableLiveData("")
    private val _searchScope = MutableLiveData(SearchScope.ALL)
    private val _filter = MutableLiveData(ContactFilter())
    private val _selectedIds = MutableLiveData<Set<Long>>(emptySet())
    private val _isSelectionMode = MutableLiveData(false)

    val selectedIds: LiveData<Set<Long>> = _selectedIds
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode
    val filter: LiveData<ContactFilter> = _filter

    /** Distinct company names across all contacts, for the filter sheet dropdown. */
    val companies: LiveData<List<String>> = allContacts.map { contacts ->
        contacts.map { it.company.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    }

    val listItems: LiveData<List<ListItem>> = MediatorLiveData<List<ListItem>>().also { mediator ->
        val recompute = { _: Any? ->
            val query = _searchQuery.value.orEmpty().trim().lowercase()
            val scope = _searchScope.value ?: SearchScope.ALL
            val filter = _filter.value ?: ContactFilter()
            val selected = _selectedIds.value ?: emptySet()
            val contacts = allContacts.value ?: emptyList()

            val filtered = contacts.filter { contact ->
                val matchesQuery = if (query.isEmpty()) true
                else when (scope) {
                    SearchScope.NAME -> contact.name.lowercase().contains(query)
                    SearchScope.COMPANY -> contact.company.lowercase().contains(query)
                    SearchScope.ALL -> contact.name.lowercase().contains(query) ||
                            contact.company.lowercase().contains(query)
                }
                matchesQuery && matchesFilter(contact, filter)
            }

            val items = mutableListOf<ListItem>()
            filtered.groupBy { it.company }.forEach { (company, groupContacts) ->
                // Parent state is recalculated from its children every time selection
                // changes: none selected -> NONE, some -> PARTIAL (indeterminate), all -> ALL.
                val selectedCount = groupContacts.count { it.id in selected }
                val selectionState = when {
                    selectedCount == 0 -> SelectionState.NONE
                    selectedCount == groupContacts.size -> SelectionState.ALL
                    else -> SelectionState.PARTIAL
                }
                items.add(ListItem.Header(company, groupContacts.size, selectionState))
                groupContacts.forEach { contact ->
                    items.add(
                        ListItem.ContactRow(
                            id = contact.id,
                            name = contact.name,
                            company = contact.company,
                            mobile = contact.mobile,
                            emails = contact.emails,
                            verified = contact.verified,
                            bookmarked = contact.bookmarked,
                            isSelected = contact.id in selected
                        )
                    )
                }
            }
            mediator.value = items
        }
        mediator.addSource(allContacts, recompute)
        mediator.addSource(_searchQuery, recompute)
        mediator.addSource(_searchScope, recompute)
        mediator.addSource(_filter, recompute)
        mediator.addSource(_selectedIds, recompute)
    }

    // Company is an exact match (it comes from a dropdown of existing companies); the rest
    // keep only contacts that have the field at all. A contact can be saved with a mobile
    // or an email but not necessarily both, which is what makes these two worth filtering on.
    private fun matchesFilter(contact: HrContact, filter: ContactFilter): Boolean {
        if (filter.company.isNotBlank() && !contact.company.trim().equals(filter.company.trim(), ignoreCase = true)) {
            return false
        }
        if (filter.hasPhone && contact.mobile.isBlank()) {
            return false
        }
        if (filter.hasEmail && contact.emails.none { it.isNotBlank() }) {
            return false
        }
        if (filter.verifiedOnly && !contact.verified) {
            return false
        }
        return true
    }

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _searchQuery.value = query
        }
    }

    fun setSearchScope(scope: SearchScope) {
        _searchScope.value = scope
    }

    fun applyFilter(filter: ContactFilter) {
        _filter.value = filter
    }

    fun clearFilter() {
        _filter.value = ContactFilter()
    }

    /** True when the visible list is narrowed by the search box or the filter sheet. */
    fun hasActiveCriteria(): Boolean =
        _searchQuery.value.orEmpty().isNotBlank() || _filter.value?.isActive == true

    fun enterSelectionMode(contactId: Long) {
        _isSelectionMode.value = true
        _selectedIds.value = setOf(contactId)
    }

    fun toggleSelection(contactId: Long) {
        val current = _selectedIds.value ?: emptySet()
        _selectedIds.value = if (contactId in current) current - contactId else current + contactId
    }

    // Parent (company header) checkbox click: downward propagation.
    // If every child is already selected (header was CHECKED), clear them all;
    // otherwise (header was NONE or PARTIAL/indeterminate), select them all.
    fun selectAllFromCompany(company: String) {
        val ids = listItems.value
            ?.filterIsInstance<ListItem.ContactRow>()
            ?.filter { it.company == company }
            ?.map { it.id }
            ?.toSet() ?: return
        val current = _selectedIds.value ?: emptySet()
        val allSelected = ids.isNotEmpty() && ids.all { it in current }
        _selectedIds.value = if (allSelected) current - ids else current + ids
    }

    fun selectAll() {
        val ids = listItems.value
            ?.filterIsInstance<ListItem.ContactRow>()
            ?.map { it.id }
            ?.toSet() ?: emptySet()
        _selectedIds.value = ids
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun getSelectedEmails(): List<String> {
        val selected = _selectedIds.value ?: return emptyList()
        return listItems.value
            ?.filterIsInstance<ListItem.ContactRow>()
            ?.filter { it.id in selected }
            ?.flatMap { it.emails }
            ?.filter { it.isNotBlank() }
            ?.distinct() ?: emptyList()
    }

    suspend fun syncNow() = repository.syncNow()

    fun toggleBookmark(contactId: Long) {
        viewModelScope.launch { repository.toggleBookmark(contactId) }
    }

    fun deleteSelected() {
        val ids = _selectedIds.value?.toList() ?: return
        viewModelScope.launch {
            repository.deleteByIds(ids)
            clearSelection()
        }
    }
}

class ContactListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = ContactRepository.getInstance(context)
        return ContactListViewModel(repo) as T
    }
}
