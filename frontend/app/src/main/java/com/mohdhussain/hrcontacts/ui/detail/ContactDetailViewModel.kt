package com.mohdhussain.hrcontacts.ui.detail

import android.content.Context
import androidx.lifecycle.*
import com.mohdhussain.hrcontacts.data.db.HrContactDatabase
import com.mohdhussain.hrcontacts.data.model.HrContact
import com.mohdhussain.hrcontacts.data.repository.ContactRepository
import kotlinx.coroutines.launch

class ContactDetailViewModel(private val repository: ContactRepository) : ViewModel() {

    private val _contact = MutableLiveData<HrContact?>()
    val contact: LiveData<HrContact?> = _contact

    fun loadContact(id: Long) {
        repository.getContactById(id).observeForever { contact ->
            _contact.value = contact
        }
    }

    fun deleteContact(onDeleted: () -> Unit) {
        val c = _contact.value ?: return
        viewModelScope.launch {
            repository.deleteContact(c)
            onDeleted()
        }
    }
}

class ContactDetailViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = HrContactDatabase.getDatabase(context)
        val repo = ContactRepository(db.hrContactDao())
        return ContactDetailViewModel(repo) as T
    }
}
