package com.mohdhussain.hrcontacts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.mohdhussain.hrcontacts.data.auth.TokenManager
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
    private val api: ApiService = RetrofitClient.apiService,
    private val tokenManager: TokenManager = TokenManager.getInstance(context)
) {
    private val syncManager = SyncManager(
        context.applicationContext, dao, api, SyncPrefs(context), tokenManager
    )
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allContacts: LiveData<List<HrContact>> = dao.getAllContacts()

    val bookmarkedContacts: LiveData<List<HrContact>> = dao.getBookmarkedContacts()

    /**
     * Contacts the signed-in user authored. Resolved per call rather than cached on this
     * singleton, so signing in as a different user in the same process cannot leave the list
     * bound to the previous user's id. With no session the query gets an id no row can match,
     * which yields an empty list instead of everybody's contacts.
     */
    fun myContacts(): LiveData<List<HrContact>> =
        dao.getContactsCreatedBy(tokenManager.userId ?: NO_USER)

    fun getContactById(id: Long): LiveData<HrContact> = dao.getContactById(id)

    /**
     * Flips the bookmark locally and lets sync carry it to the server, so the row re-renders
     * immediately and the change survives being made offline. Contacts that have never synced
     * have no server id to bookmark against — the flag rides along once the create is accepted.
     */
    suspend fun toggleBookmark(localId: Long) {
        val contact = dao.findById(localId) ?: return
        dao.setBookmarkLocally(localId, !contact.bookmarked)
        requestSync()
    }

    suspend fun createLocalContact(
        name: String,
        company: String,
        mobile: String,
        emails: List<String>,
        linkedinProfile: String,
        isPrivate: Boolean
    ): Long {
        val id = dao.insertContact(
            HrContact(
                name = name,
                company = company,
                mobile = mobile,
                emails = emails,
                linkedinProfile = linkedinProfile,
                // verified is left at its default: only the server can promote a contact, and the
                // real value arrives on the next pull.
                isPrivate = isPrivate,
                // Stamped locally so the contact shows up under "Added by me" straight away;
                // the server confirms the same value on the next pull.
                createdBy = tokenManager.userId,
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
        isPrivate: Boolean
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
                // existing.verified carries through untouched — an edit here must not disturb it.
                isPrivate = isPrivate,
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

        // Sentinel for "no signed-in user". Room needs a concrete argument, and a real Mongo
        // ObjectId is never this value, so the createdBy query matches nothing.
        private const val NO_USER = "__no_user__"

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
