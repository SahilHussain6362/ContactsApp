package com.mohdhussain.hrcontacts.ui.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mohdhussain.hrcontacts.data.model.HrContact
import com.mohdhussain.hrcontacts.data.remote.dto.UserDto
import com.mohdhussain.hrcontacts.data.repository.AuthRepository
import com.mohdhussain.hrcontacts.data.repository.ContactRepository
import com.mohdhussain.hrcontacts.ui.list.ListItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/** Which of the user's two collections the list is showing. */
enum class Collection { BOOKMARKED, ADDED }

class ProfileViewModel(
    private val contactRepository: ContactRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val bookmarked: LiveData<List<HrContact>> = contactRepository.bookmarkedContacts
    private val added: LiveData<List<HrContact>> = contactRepository.myContacts()

    private val _collection = MutableLiveData(Collection.BOOKMARKED)
    val collection: LiveData<Collection> = _collection

    // Seeded from the cached session so the header renders instantly and still works offline;
    // refresh() then replaces it with whatever the server currently holds.
    private val _user = MutableLiveData(authRepository.currentUser())
    val user: LiveData<UserDto?> = _user

    private val _errors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val errors: SharedFlow<Unit> = _errors

    private val _nameSaved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val nameSaved: SharedFlow<Unit> = _nameSaved

    val counts: LiveData<Pair<Int, Int>> = MediatorLiveData<Pair<Int, Int>>().also { mediator ->
        val recompute = { _: Any? ->
            mediator.value = (bookmarked.value?.size ?: 0) to (added.value?.size ?: 0)
        }
        mediator.addSource(bookmarked, recompute)
        mediator.addSource(added, recompute)
    }

    /** The rows for whichever collection is selected. Headerless — these lists are already small. */
    val listItems: LiveData<List<ListItem>> = MediatorLiveData<List<ListItem>>().also { mediator ->
        val recompute = { _: Any? ->
            val source = when (_collection.value ?: Collection.BOOKMARKED) {
                Collection.BOOKMARKED -> bookmarked.value
                Collection.ADDED -> added.value
            }
            mediator.value = source.orEmpty().map { contact ->
                ListItem.ContactRow(
                    id = contact.id,
                    name = contact.name,
                    company = contact.company,
                    mobile = contact.mobile,
                    emails = contact.emails,
                    verified = contact.verified,
                    bookmarked = contact.bookmarked,
                    isSelected = false
                )
            }
        }
        mediator.addSource(bookmarked, recompute)
        mediator.addSource(added, recompute)
        mediator.addSource(_collection, recompute)
    }

    init {
        refresh()
    }

    fun setCollection(collection: Collection) {
        _collection.value = collection
    }

    /**
     * Pulls the profile and runs a sync. Both are best-effort: a failure here leaves the cached
     * header and the local lists on screen rather than emptying the page.
     */
    fun refresh() {
        viewModelScope.launch {
            authRepository.refreshProfile().onSuccess { _user.value = it }
            contactRepository.syncNow()
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            authRepository.updateName(name)
                .onSuccess {
                    _user.value = it
                    _nameSaved.emit(Unit)
                }
                .onFailure { _errors.emit(Unit) }
        }
    }

    fun toggleBookmark(contactId: Long) {
        viewModelScope.launch { contactRepository.toggleBookmark(contactId) }
    }
}

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(
        ContactRepository.getInstance(context),
        AuthRepository.getInstance(context)
    ) as T
}
