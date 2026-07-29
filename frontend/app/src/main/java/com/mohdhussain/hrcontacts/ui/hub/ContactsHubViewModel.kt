package com.mohdhussain.hrcontacts.ui.hub

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mohdhussain.hrcontacts.data.repository.ContactRepository
import kotlinx.coroutines.launch

/** Just the two folder counts — the contacts themselves are read by [com.mohdhussain.hrcontacts.ui.list.ContactListViewModel] once a folder is opened. */
class ContactsHubViewModel(private val repository: ContactRepository) : ViewModel() {

    private val yourContacts = repository.myContacts()
    private val bookmarks = repository.bookmarkedContacts

    val counts: LiveData<Pair<Int, Int>> = MediatorLiveData<Pair<Int, Int>>().also { mediator ->
        val recompute = { _: Any? ->
            mediator.value = (yourContacts.value?.size ?: 0) to (bookmarks.value?.size ?: 0)
        }
        mediator.addSource(yourContacts, recompute)
        mediator.addSource(bookmarks, recompute)
    }

    init {
        viewModelScope.launch { repository.syncNow() }
    }
}

class ContactsHubViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ContactsHubViewModel(ContactRepository.getInstance(context)) as T
}
