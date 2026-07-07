package com.mohdhussain.hrcontacts.ui.auth

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mohdhussain.hrcontacts.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _loginResult = MutableSharedFlow<Result<Unit>>(extraBufferCapacity = 1)
    val loginResult: SharedFlow<Result<Unit>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _loginResult.emit(repository.login(email, password))
            _loading.value = false
        }
    }
}

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(AuthRepository.getInstance(context)) as T
    }
}
