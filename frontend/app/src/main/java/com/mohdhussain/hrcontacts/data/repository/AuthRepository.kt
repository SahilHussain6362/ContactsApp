package com.mohdhussain.hrcontacts.data.repository

import android.content.Context
import com.mohdhussain.hrcontacts.data.auth.TokenManager
import com.mohdhussain.hrcontacts.data.remote.ApiService
import com.mohdhussain.hrcontacts.data.remote.RetrofitClient
import com.mohdhussain.hrcontacts.data.remote.dto.ErrorResponseDto
import com.mohdhussain.hrcontacts.data.remote.dto.ForgotPasswordSendOtpRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.GoogleAuthRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.LoginRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.RegisterSendOtpRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.RegisterVerifyRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.ResetPasswordRequestDto
import com.squareup.moshi.Moshi
import retrofit2.HttpException

class AuthException(message: String) : Exception(message)

class AuthRepository(
    private val tokenManager: TokenManager,
    private val api: ApiService = RetrofitClient.apiService
) {
    private val moshi = Moshi.Builder().build()
    private val errorAdapter = moshi.adapter(ErrorResponseDto::class.java)

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    suspend fun loginWithGoogle(idToken: String): Result<Unit> = runCatching {
        val response = api.googleLogin(GoogleAuthRequestDto(idToken))
        tokenManager.saveSession(response.jwt, response.user)
    }.mapAuthFailure()

    suspend fun registerSendOtp(email: String): Result<Unit> = runCatching {
        api.registerSendOtp(RegisterSendOtpRequestDto(email))
        Unit
    }.mapAuthFailure()

    suspend fun registerVerify(email: String, otp: String, password: String, name: String?): Result<Unit> =
        runCatching {
            val response = api.registerVerify(RegisterVerifyRequestDto(email, otp, password, name))
            tokenManager.saveSession(response.jwt, response.user)
        }.mapAuthFailure()

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = api.login(LoginRequestDto(email, password))
        tokenManager.saveSession(response.jwt, response.user)
    }.mapAuthFailure()

    suspend fun forgotPasswordSendOtp(email: String): Result<Unit> = runCatching {
        api.forgotPasswordSendOtp(ForgotPasswordSendOtpRequestDto(email))
        Unit
    }.mapAuthFailure()

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Unit> = runCatching {
        val response = api.resetPassword(ResetPasswordRequestDto(email, otp, newPassword))
        tokenManager.saveSession(response.jwt, response.user)
    }.mapAuthFailure()

    fun logout() = tokenManager.clear()

    // Wraps any failure (network, HTTP, parsing) into an AuthException carrying a message
    // fit to show directly in the UI, preferring the backend's ResponseStatusException reason.
    private fun <T> Result<T>.mapAuthFailure(): Result<T> = recoverCatching { throwable ->
        throw AuthException(extractServerMessage(throwable) ?: DEFAULT_ERROR_MESSAGE)
    }

    private fun extractServerMessage(throwable: Throwable): String? {
        if (throwable !is HttpException) return null
        val body = throwable.response()?.errorBody()?.string() ?: return null
        return runCatching { errorAdapter.fromJson(body)?.message }.getOrNull()
    }

    companion object {
        private const val DEFAULT_ERROR_MESSAGE = "Something went wrong. Please try again."

        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository(TokenManager.getInstance(context)).also { INSTANCE = it }
            }
    }
}
