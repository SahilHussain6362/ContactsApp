package com.mohdhussain.hrcontacts.ui.add

import android.content.Context
import androidx.lifecycle.*
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

    fun save(name: String, company: String, mobile: String, emails: List<String>, linkedinProfile: String) {
        val resolvedName = name.ifEmpty { "Anonymous" }
        viewModelScope.launch {
            val existing = editContact.value
            if (existing != null) {
                repository.updateLocalContact(existing, resolvedName, company, mobile, emails, linkedinProfile)
            } else {
                repository.createLocalContact(resolvedName, company, mobile, emails, linkedinProfile)
            }
            _saveResult.emit(true)
        }
    }
}

class AddContactViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = ContactRepository.getInstance(context)
        return AddContactViewModel(repo) as T
    }
}
