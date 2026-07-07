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

class ForgotPasswordViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _sendOtpResult = MutableSharedFlow<Result<Unit>>(extraBufferCapacity = 1)
    val sendOtpResult: SharedFlow<Result<Unit>> = _sendOtpResult

    private val _resetResult = MutableSharedFlow<Result<Unit>>(extraBufferCapacity = 1)
    val resetResult: SharedFlow<Result<Unit>> = _resetResult

    fun sendOtp(email: String) {
        viewModelScope.launch {
            _loading.value = true
            _sendOtpResult.emit(repository.forgotPasswordSendOtp(email))
            _loading.value = false
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String) {
        viewModelScope.launch {
            _loading.value = true
            _resetResult.emit(repository.resetPassword(email, otp, newPassword))
            _loading.value = false
        }
    }
}

class ForgotPasswordViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ForgotPasswordViewModel(AuthRepository.getInstance(context)) as T
    }
}
