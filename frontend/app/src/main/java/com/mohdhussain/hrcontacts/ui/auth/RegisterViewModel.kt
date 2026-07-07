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

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _sendOtpResult = MutableSharedFlow<Result<Unit>>(extraBufferCapacity = 1)
    val sendOtpResult: SharedFlow<Result<Unit>> = _sendOtpResult

    private val _verifyResult = MutableSharedFlow<Result<Unit>>(extraBufferCapacity = 1)
    val verifyResult: SharedFlow<Result<Unit>> = _verifyResult

    fun sendOtp(email: String) {
        viewModelScope.launch {
            _loading.value = true
            _sendOtpResult.emit(repository.registerSendOtp(email))
            _loading.value = false
        }
    }

    fun verify(email: String, otp: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _verifyResult.emit(repository.registerVerify(email, otp, password, null))
            _loading.value = false
        }
    }
}

class RegisterViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(AuthRepository.getInstance(context)) as T
    }
}
