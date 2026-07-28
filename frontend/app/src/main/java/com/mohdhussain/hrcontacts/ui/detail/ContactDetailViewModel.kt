package com.mohdhussain.hrcontacts.ui.detail

import android.content.Context
import androidx.lifecycle.*
import com.mohdhussain.hrcontacts.data.model.HrContact
import com.mohdhussain.hrcontacts.data.remote.dto.EmailTemplateDto
import com.mohdhussain.hrcontacts.data.remote.dto.WhatsappTemplateDto
import com.mohdhussain.hrcontacts.data.repository.AuthRepository
import com.mohdhussain.hrcontacts.data.repository.ContactRepository
import kotlinx.coroutines.launch

class ContactDetailViewModel(
    private val repository: ContactRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _contact = MutableLiveData<HrContact?>()
    val contact: LiveData<HrContact?> = _contact

    /**
     * The templates offered when reaching out from this contact. Read from the cached profile on
     * each tap rather than held in a field: every template write refreshes that cache, so a
     * template added a moment ago on the profile page is already here, and it still works offline.
     */
    val emailTemplates: List<EmailTemplateDto>
        get() = authRepository.currentUser()?.emailTemplates.orEmpty()

    val whatsappTemplates: List<WhatsappTemplateDto>
        get() = authRepository.currentUser()?.whatsappTemplates.orEmpty()

    fun loadContact(id: Long) {
        repository.getContactById(id).observeForever { contact ->
            _contact.value = contact
        }
    }

    fun toggleBookmark() {
        val id = _contact.value?.id ?: return
        viewModelScope.launch { repository.toggleBookmark(id) }
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
        val repo = ContactRepository.getInstance(context)
        return ContactDetailViewModel(repo, AuthRepository.getInstance(context)) as T
    }
}
