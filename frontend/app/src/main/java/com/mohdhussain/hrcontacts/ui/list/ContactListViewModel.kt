package com.mohdhussain.hrcontacts.ui.list

import android.content.Context
import androidx.lifecycle.*
import com.mohdhussain.hrcontacts.data.model.HrContact
import com.mohdhussain.hrcontacts.data.repository.ContactRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SearchScope { ALL, NAME, COMPANY }

class ContactListViewModel(private val repository: ContactRepository) : ViewModel() {

    init {
        viewModelScope.launch { repository.syncNow() }
    }

    private val allContacts: LiveData<List<HrContact>> = repository.allContacts

    private val _searchQuery = MutableLiveData("")
    private val _searchScope = MutableLiveData(SearchScope.ALL)
    private val _selectedIds = MutableLiveData<Set<Long>>(emptySet())
    private val _isSelectionMode = MutableLiveData(false)

    val selectedIds: LiveData<Set<Long>> = _selectedIds
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    val listItems: LiveData<List<ListItem>> = MediatorLiveData<List<ListItem>>().also { mediator ->
        val recompute = { _: Any? ->
            val query = _searchQuery.value.orEmpty().trim().lowercase()
            val scope = _searchScope.value ?: SearchScope.ALL
            val selected = _selectedIds.value ?: emptySet()
            val contacts = allContacts.value ?: emptyList()

            val filtered = contacts.filter { contact ->
                if (query.isEmpty()) true
                else when (scope) {
                    SearchScope.NAME -> contact.name.lowercase().contains(query)
                    SearchScope.COMPANY -> contact.company.lowercase().contains(query)
                    SearchScope.ALL -> contact.name.lowercase().contains(query) ||
                            contact.company.lowercase().contains(query)
                }
            }

            val items = mutableListOf<ListItem>()
            filtered.groupBy { it.company }.forEach { (company, groupContacts) ->
                val allSelected = groupContacts.isNotEmpty() && groupContacts.all { it.id in selected }
                items.add(ListItem.Header(company, groupContacts.size, allSelected))
                groupContacts.forEach { contact ->
                    items.add(
                        ListItem.ContactRow(
                            id = contact.id,
                            name = contact.name,
                            company = contact.company,
                            mobile = contact.mobile,
                            emails = contact.emails,
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
        mediator.addSource(_selectedIds, recompute)
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

    fun enterSelectionMode(contactId: Long) {
        _isSelectionMode.value = true
        _selectedIds.value = setOf(contactId)
    }

    fun toggleSelection(contactId: Long) {
        val current = _selectedIds.value ?: emptySet()
        _selectedIds.value = if (contactId in current) current - contactId else current + contactId
    }

    fun selectAllFromCompany(company: String) {
        val ids = listItems.value
            ?.filterIsInstance<ListItem.ContactRow>()
            ?.filter { it.company == company }
            ?.map { it.id }
            ?.toSet() ?: return
        val current = _selectedIds.value ?: emptySet()
        val allSelected = ids.all { it in current }
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
