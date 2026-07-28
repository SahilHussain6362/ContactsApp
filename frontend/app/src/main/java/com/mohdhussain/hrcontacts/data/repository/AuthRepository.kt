package com.mohdhussain.hrcontacts.data.repository

import android.content.Context
import com.mohdhussain.hrcontacts.data.auth.TokenManager
import com.mohdhussain.hrcontacts.data.remote.ApiService
import com.mohdhussain.hrcontacts.data.remote.RetrofitClient
import com.mohdhussain.hrcontacts.data.remote.dto.EmailTemplateRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.ErrorResponseDto
import com.mohdhussain.hrcontacts.data.remote.dto.GoogleAuthRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.UpdateProfileRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.UserDto
import com.mohdhussain.hrcontacts.data.remote.dto.WhatsappTemplateRequestDto
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

    /** The cached profile. Available offline, which is why the profile screen reads this first. */
    fun currentUser(): UserDto? = tokenManager.user

    fun currentUserId(): String? = tokenManager.userId

    suspend fun loginWithGoogle(idToken: String): Result<Unit> = runCatching {
        val response = api.googleLogin(GoogleAuthRequestDto(idToken))
        tokenManager.saveSession(response.jwt, response.user)
    }.mapAuthFailure()

    /** Refreshes the cached profile from the server and returns it. */
    suspend fun refreshProfile(): Result<UserDto> = runCatching {
        api.getProfile().also { tokenManager.updateUser(it) }
    }.mapAuthFailure()

    suspend fun updateName(name: String): Result<UserDto> = runCatching {
        api.updateProfile(UpdateProfileRequestDto(name.trim())).also { tokenManager.updateUser(it) }
    }.mapAuthFailure()

    /**
     * Template writes. Each returns the caller's whole profile, which is cached on the way through —
     * so the templates the contact screen reads are always the ones the server last confirmed,
     * and they stay readable offline.
     *
     * A null `id` means create; anything else replaces that template. The server rejects a create
     * past its per-type cap with a 409 whose message is fit to show as-is; [mapAuthFailure] is what
     * lifts it out.
     */
    suspend fun saveEmailTemplate(id: String?, heading: String, body: String): Result<UserDto> =
        runCatching {
            val request = EmailTemplateRequestDto(heading.trim(), body.trim())
            val user = if (id == null) {
                api.createEmailTemplate(request)
            } else {
                api.updateEmailTemplate(id, request)
            }
            user.also { tokenManager.updateUser(it) }
        }.mapAuthFailure()

    suspend fun deleteEmailTemplate(id: String): Result<UserDto> = runCatching {
        api.deleteEmailTemplate(id).also { tokenManager.updateUser(it) }
    }.mapAuthFailure()

    suspend fun saveWhatsappTemplate(id: String?, message: String): Result<UserDto> = runCatching {
        val request = WhatsappTemplateRequestDto(message.trim())
        val user = if (id == null) {
            api.createWhatsappTemplate(request)
        } else {
            api.updateWhatsappTemplate(id, request)
        }
        user.also { tokenManager.updateUser(it) }
    }.mapAuthFailure()

    suspend fun deleteWhatsappTemplate(id: String): Result<UserDto> = runCatching {
        api.deleteWhatsappTemplate(id).also { tokenManager.updateUser(it) }
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
