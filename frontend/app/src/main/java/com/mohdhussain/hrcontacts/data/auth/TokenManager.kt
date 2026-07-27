package com.mohdhussain.hrcontacts.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mohdhussain.hrcontacts.data.remote.dto.UserDto
import com.squareup.moshi.Moshi
import java.security.KeyStore

class TokenManager private constructor(context: Context) {

    private val moshi = Moshi.Builder().build()
    private val userAdapter = moshi.adapter(UserDto::class.java)

    private val prefs: SharedPreferences = openPrefs(context)

    /**
     * The Tink keysets that protect this file live inside [PREFS_NAME] itself, wrapped by a
     * non-exportable AndroidKeyStore master key. If the two ever stop matching, opening the
     * prefs throws and the app dies before drawing a frame — so recover instead of crashing.
     *
     * The mismatch is not hypothetical: a cloud restore or a device-to-device transfer copies
     * auth_prefs.xml but cannot copy the hardware-bound key. The backup rules wired up in
     * AndroidManifest.xml stop that from happening again; this is the safety net for installs
     * that already carry a broken file.
     */
    private fun openPrefs(context: Context): SharedPreferences =
        try {
            createEncryptedPrefs(context)
        } catch (e: Exception) {
            // Deliberately broad: Keystore and Tink surface this as anything from
            // AEADBadTagException to IllegalStateException. Whatever the shape, the only
            // recovery is to discard the unreadable key material — the contacts database is
            // untouched, so the user loses the saved session and signs in again.
            Log.w(TAG, "$PREFS_NAME could not be decrypted; recreating it", e)
            discardKeyMaterial(context)
            createEncryptedPrefs(context)
        }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun discardKeyMaterial(context: Context) {
        context.deleteSharedPreferences(PREFS_NAME)
        runCatching {
            KeyStore.getInstance(ANDROID_KEYSTORE)
                .apply { load(null) }
                .deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        }.onFailure { Log.w(TAG, "Could not delete the master key entry", it) }
    }

    val jwt: String?
        get() = prefs.getString(KEY_JWT, null)

    val user: UserDto?
        get() = prefs.getString(KEY_USER, null)?.let { runCatching { userAdapter.fromJson(it) }.getOrNull() }

    fun isLoggedIn(): Boolean = jwt != null

    val userId: String? get() = user?.id

    fun saveSession(jwt: String, user: UserDto) {
        prefs.edit()
            .putString(KEY_JWT, jwt)
            .putString(KEY_USER, userAdapter.toJson(user))
            .apply()
    }

    /**
     * Replaces the cached user without touching the JWT — used after a profile edit, a bookmark
     * toggle, or a sync refresh, all of which return a fresh [UserDto] against the same session.
     */
    fun updateUser(user: UserDto) {
        prefs.edit().putString(KEY_USER, userAdapter.toJson(user)).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_NAME = "auth_prefs"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
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
