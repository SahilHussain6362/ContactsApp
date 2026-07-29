package com.mohdhussain.hrcontacts.ui.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.mohdhussain.hrcontacts.data.remote.dto.EmailTemplateDto
import com.mohdhussain.hrcontacts.data.remote.dto.MAX_TEMPLATES_PER_TYPE
import com.mohdhussain.hrcontacts.data.remote.dto.UserDto
import com.mohdhussain.hrcontacts.data.remote.dto.WhatsappTemplateDto
import com.mohdhussain.hrcontacts.data.repository.AuthRepository
import com.mohdhussain.hrcontacts.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val contactRepository: ContactRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Seeded from the cached session so the header renders instantly and still works offline;
    // refresh() then replaces it with whatever the server currently holds.
    private val _user = MutableLiveData(authRepository.currentUser())
    val user: LiveData<UserDto?> = _user

    private val _errors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val errors: SharedFlow<Unit> = _errors

    private val _nameSaved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val nameSaved: SharedFlow<Unit> = _nameSaved

    private val _templateSaved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val templateSaved: SharedFlow<Unit> = _templateSaved

    private val _templateDeleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val templateDeleted: SharedFlow<Unit> = _templateDeleted

    // Carries the server's own wording — a rejected template write explains itself (a cap hit, a
    // template deleted from another device), which a generic failure string could not.
    private val _templateErrors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val templateErrors: SharedFlow<String> = _templateErrors

    /**
     * The templates the user has saved, as two headed sections. Derived from the cached profile, so
     * they render offline and update the moment a write comes back.
     */
    val templateItems: LiveData<List<TemplateListItem>> = _user.map { user ->
        val emails = user?.emailTemplates.orEmpty()
        val whatsapps = user?.whatsappTemplates.orEmpty()
        buildList {
            add(TemplateListItem.Header(TemplateType.EMAIL, emails.size))
            emails.forEach {
                add(TemplateListItem.Row(TemplateType.EMAIL, it.id, it.heading, it.body))
            }
            add(TemplateListItem.Header(TemplateType.WHATSAPP, whatsapps.size))
            whatsapps.forEach {
                add(TemplateListItem.Row(TemplateType.WHATSAPP, it.id, null, it.message))
            }
        }
    }

    init {
        refresh()
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

    /**
     * False once the user holds [MAX_TEMPLATES_PER_TYPE] templates of this type. Checked before the
     * editor opens so the warning arrives instead of a form the save would only reject — the server
     * enforces the same cap regardless.
     */
    fun hasRoomFor(type: TemplateType): Boolean {
        val saved = when (type) {
            TemplateType.EMAIL -> _user.value?.emailTemplates?.size ?: 0
            TemplateType.WHATSAPP -> _user.value?.whatsappTemplates?.size ?: 0
        }
        return saved < MAX_TEMPLATES_PER_TYPE
    }

    fun emailTemplate(id: String): EmailTemplateDto? =
        _user.value?.emailTemplates?.firstOrNull { it.id == id }

    fun whatsappTemplate(id: String): WhatsappTemplateDto? =
        _user.value?.whatsappTemplates?.firstOrNull { it.id == id }

    /** A null [id] creates; anything else replaces that template. */
    fun saveEmailTemplate(id: String?, heading: String, body: String) {
        viewModelScope.launch {
            authRepository.saveEmailTemplate(id, heading, body).publish(_templateSaved)
        }
    }

    fun saveWhatsappTemplate(id: String?, message: String) {
        viewModelScope.launch {
            authRepository.saveWhatsappTemplate(id, message).publish(_templateSaved)
        }
    }

    fun deleteTemplate(type: TemplateType, id: String) {
        viewModelScope.launch {
            when (type) {
                TemplateType.EMAIL -> authRepository.deleteEmailTemplate(id)
                TemplateType.WHATSAPP -> authRepository.deleteWhatsappTemplate(id)
            }.publish(_templateDeleted)
        }
    }

    // Every template write answers with the whole profile, so a success is just a new _user value —
    // templateItems recomputes from there and the list redraws itself.
    private suspend fun Result<UserDto>.publish(success: MutableSharedFlow<Unit>) {
        onSuccess {
            _user.value = it
            success.emit(Unit)
        }.onFailure {
            _templateErrors.emit(it.message.orEmpty())
        }
    }
}

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(
        ContactRepository.getInstance(context),
        AuthRepository.getInstance(context)
    ) as T
}
