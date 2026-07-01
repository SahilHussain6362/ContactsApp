package com.mohdhussain.hrcontacts.ui.add

import android.content.Context
import androidx.lifecycle.*
import com.mohdhussain.hrcontacts.data.db.HrContactDatabase
import com.mohdhussain.hrcontacts.data.model.HrContact
import com.mohdhussain.hrcontacts.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AddContactViewModel(private val repository: ContactRepository) : ViewModel() {

    private val _saveResult = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val saveResult: SharedFlow<Boolean> = _saveResult

    val editContact: MutableLiveData<HrContact?> = MutableLiveData(null)

    fun loadContact(id: Long) {
        if (id == -1L) return
        repository.getContactById(id).observeForever { contact ->
            editContact.value = contact
        }
    }

    fun save(name: String, company: String, mobile: String, email: String) {
        viewModelScope.launch {
            val existing = editContact.value
            if (existing != null) {
                repository.updateContact(existing.copy(name = name, company = company, mobile = mobile, email = email))
            } else {
                repository.insertContact(HrContact(name = name, company = company, mobile = mobile, email = email))
            }
            _saveResult.emit(true)
        }
    }
}

class AddContactViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = HrContactDatabase.getDatabase(context)
        val repo = ContactRepository(db.hrContactDao())
        return AddContactViewModel(repo) as T
    }
}
