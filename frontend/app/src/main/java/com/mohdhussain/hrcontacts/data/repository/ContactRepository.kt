package com.mohdhussain.hrcontacts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.mohdhussain.hrcontacts.data.db.HrContactDao
import com.mohdhussain.hrcontacts.data.db.HrContactDatabase
import com.mohdhussain.hrcontacts.data.model.HrContact
import com.mohdhussain.hrcontacts.data.model.PendingAction
import com.mohdhussain.hrcontacts.data.remote.ApiService
import com.mohdhussain.hrcontacts.data.remote.RetrofitClient
import com.mohdhussain.hrcontacts.data.remote.SyncManager
import com.mohdhussain.hrcontacts.data.remote.SyncPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ContactRepository(
    context: Context,
    private val dao: HrContactDao,
    api: ApiService = RetrofitClient.apiService
) {
    private val syncManager = SyncManager(context.applicationContext, dao, api, SyncPrefs(context))
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allContacts: LiveData<List<HrContact>> = dao.getAllContacts()

    fun getContactById(id: Long): LiveData<HrContact> = dao.getContactById(id)

    suspend fun createLocalContact(
        name: String,
        company: String,
        mobile: String,
        emails: List<String>,
        linkedinProfile: String,
        verified: Boolean
    ): Long {
        val id = dao.insertContact(
            HrContact(
                name = name,
                company = company,
                mobile = mobile,
                emails = emails,
                linkedinProfile = linkedinProfile,
                verified = verified,
                updatedAt = System.currentTimeMillis(),
                pendingAction = PendingAction.CREATE
            )
        )
        requestSync()
        return id
    }

    suspend fun updateLocalContact(
        existing: HrContact,
        name: String,
        company: String,
        mobile: String,
        emails: List<String>,
        linkedinProfile: String,
        verified: Boolean
    ) {
        val nextPendingAction =
            if (existing.pendingAction == PendingAction.CREATE) PendingAction.CREATE else PendingAction.UPDATE
        dao.updateContact(
            existing.copy(
                name = name,
                company = company,
                mobile = mobile,
                emails = emails,
                linkedinProfile = linkedinProfile,
                verified = verified,
                updatedAt = System.currentTimeMillis(),
                pendingAction = nextPendingAction
            )
        )
        requestSync()
    }

    suspend fun deleteContact(contact: HrContact) {
        if (contact.serverId == null) {
            dao.deleteContact(contact)
        } else {
            dao.markPendingDelete(contact.id, System.currentTimeMillis())
        }
        requestSync()
    }

    suspend fun deleteByIds(ids: List<Long>) {
        val contacts = dao.getContactsByIds(ids)
        val (neverSynced, synced) = contacts.partition { it.serverId == null }
        if (neverSynced.isNotEmpty()) {
            dao.deleteByIds(neverSynced.map { it.id })
        }
        val now = System.currentTimeMillis()
        synced.forEach { dao.markPendingDelete(it.id, now) }
        requestSync()
    }

    suspend fun syncNow() = syncManager.sync()

    fun requestSync() {
        syncScope.launch {
            try {
                syncManager.sync()
            } catch (e: Exception) {
                Log.w(TAG, "requestSync failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "ContactRepository"

        @Volatile
        private var INSTANCE: ContactRepository? = null

        fun getInstance(context: Context): ContactRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactRepository(
                    context.applicationContext,
                    HrContactDatabase.getDatabase(context).hrContactDao()
                ).also { INSTANCE = it }
            }
    }
}
