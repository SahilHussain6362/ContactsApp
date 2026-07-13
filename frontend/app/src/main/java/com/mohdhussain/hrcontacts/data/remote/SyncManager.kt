package com.mohdhussain.hrcontacts.data.remote

import android.content.Context
import android.util.Log
import com.mohdhussain.hrcontacts.data.db.HrContactDao
import com.mohdhussain.hrcontacts.data.model.HrContact
import com.mohdhussain.hrcontacts.data.model.PendingAction
import com.mohdhussain.hrcontacts.data.remote.dto.BatchSyncRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.ContactRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.RemoteContact
import com.mohdhussain.hrcontacts.data.remote.dto.SyncAction
import com.mohdhussain.hrcontacts.data.remote.dto.SyncChangeDto
import com.mohdhussain.hrcontacts.util.NetworkUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

class SyncManager(
    private val context: Context,
    private val dao: HrContactDao,
    private val api: ApiService,
    private val syncPrefs: SyncPrefs
) {
    private val mutex = Mutex()

    suspend fun sync() {
        if (mutex.isLocked) return
        if (!NetworkUtils.isOnline(context)) return
        mutex.withLock {
            try {
                push()
                pull()
            } catch (e: Exception) {
                Log.w(TAG, "Sync failed, will retry next trigger", e)
            }
        }
    }

    private suspend fun push() {
        val pending = dao.getPendingContacts()
        if (pending.isEmpty()) return

        val nonDeleteRows = pending.filter { it.pendingAction != PendingAction.DELETE }
        val deleteRows = pending.filter { it.pendingAction == PendingAction.DELETE }

        val changes = mutableListOf<SyncChangeDto>()
        nonDeleteRows.forEach { row ->
            changes.add(
                SyncChangeDto(
                    action = if (row.pendingAction == PendingAction.CREATE) SyncAction.CREATE else SyncAction.UPDATE,
                    serverId = row.serverId,
                    contact = row.toRequestDto(),
                    clientUpdatedAt = row.updatedAt
                )
            )
        }
        val syncableDeleteRows = deleteRows.filter { it.serverId != null }
        deleteRows.filter { it.serverId == null }.forEach { dao.purgeLocal(it.id) }
        syncableDeleteRows.forEach { row ->
            changes.add(
                SyncChangeDto(
                    action = SyncAction.DELETE,
                    serverId = row.serverId,
                    contact = null,
                    clientUpdatedAt = row.updatedAt
                )
            )
        }
        if (changes.isEmpty()) return

        val response = api.batchSync(BatchSyncRequestDto(changes))

        nonDeleteRows.forEachIndexed { index, row ->
            val remote = response.contacts.getOrNull(index) ?: return@forEachIndexed
            val existingOwner = dao.getByServerId(remote.id)
            if (existingOwner != null && existingOwner.id != row.id) {
                dao.purgeLocal(row.id)
            } else {
                dao.markSynced(row.id, remote.id, remote.parsedUpdatedAt())
            }
        }

        syncableDeleteRows.forEach { row ->
            if (row.serverId in response.deletedIds) {
                dao.purgeLocal(row.id)
            } else {
                dao.clearPendingAction(row.id, System.currentTimeMillis())
            }
        }

        syncPrefs.lastSyncTimestamp = maxOf(syncPrefs.lastSyncTimestamp, response.serverTimestamp)
    }

    private suspend fun pull() {
        val pullStartedAt = System.currentTimeMillis()
        val remoteChanges = api.getChanges(syncPrefs.lastSyncTimestamp)

        remoteChanges.forEach { remote ->
            val local = dao.getByServerId(remote.id)
            if (remote.deleted) {
                if (local != null) dao.deleteByServerId(remote.id)
                return@forEach
            }

            when {
                local == null -> dao.insertContact(remote.toLocalContact())
                local.pendingAction == PendingAction.NONE ->
                    dao.updateContact(remote.toLocalContact(localId = local.id))
                else -> {
                    if (remote.parsedUpdatedAt() > local.updatedAt) {
                        dao.updateContact(remote.toLocalContact(localId = local.id))
                    }
                    // else: local edit/delete is newer or equal, leave as-is — it re-pushes next cycle.
                }
            }
        }

        syncPrefs.lastSyncTimestamp = maxOf(0L, pullStartedAt - CLOCK_SKEW_BUFFER_MS)
    }

    private fun HrContact.toRequestDto() = ContactRequestDto(
        name = name,
        company = company,
        mobile = mobile.ifBlank { null },
        emails = emails.ifEmpty { null },
        linkedinProfile = linkedinProfile.ifBlank { null },
        verified = verified
    )

    private fun RemoteContact.toLocalContact(localId: Long = 0): HrContact = HrContact(
        id = localId,
        name = name,
        company = company,
        mobile = mobile ?: "",
        emails = emails ?: emptyList(),
        linkedinProfile = linkedinProfile ?: "",
        verified = verified,
        serverId = id,
        updatedAt = parsedUpdatedAt(),
        pendingAction = PendingAction.NONE
    )

    private fun RemoteContact.parsedUpdatedAt(): Long =
        updatedAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
            ?: System.currentTimeMillis()

    companion object {
        private const val TAG = "SyncManager"
        private const val CLOCK_SKEW_BUFFER_MS = 2000L
    }
}
