package com.mohdhussain.hrcontacts.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mohdhussain.hrcontacts.data.remote.dto.UserDto
import com.squareup.moshi.Moshi

class TokenManager private constructor(context: Context) {

    private val moshi = Moshi.Builder().build()
    private val userAdapter = moshi.adapter(UserDto::class.java)

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    val jwt: String?
        get() = prefs.getString(KEY_JWT, null)

    val user: UserDto?
        get() = prefs.getString(KEY_USER, null)?.let { runCatching { userAdapter.fromJson(it) }.getOrNull() }

    fun isLoggedIn(): Boolean = jwt != null

    fun saveSession(jwt: String, user: UserDto) {
        prefs.edit()
            .putString(KEY_JWT, jwt)
            .putString(KEY_USER, userAdapter.toJson(user))
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_JWT = "jwt"
        private const val KEY_USER = "user"

        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
    }
}
