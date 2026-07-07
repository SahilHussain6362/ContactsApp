package com.mohdhussain.hrcontacts.data.remote

import android.content.Context

class SyncPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()

    companion object {
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }
}
