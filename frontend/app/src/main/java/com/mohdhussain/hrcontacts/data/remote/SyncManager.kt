package com.mohdhussain.hrcontacts.data.remote

import android.content.Context
import android.util.Log
import com.mohdhussain.hrcontacts.data.auth.TokenManager
import com.mohdhussain.hrcontacts.data.db.HrContactDao
import com.mohdhussain.hrcontacts.data.model.HrContact
import com.mohdhussain.hrcontacts.data.model.PendingAction
import com.mohdhussain.hrcontacts.data.remote.dto.BatchSyncRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.ContactRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.RemoteContact
import com.mohdhussain.hrcontacts.data.remote.dto.SyncAction
import com.mohdhussain.hrcontacts.data.remote.dto.SyncChangeDto
import com.mohdhussain.hrcontacts.data.remote.dto.UserDto
import com.mohdhussain.hrcontacts.util.NetworkUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

class SyncManager(
    private val context: Context,
    private val dao: HrContactDao,
    private val api: ApiService,
    private val syncPrefs: SyncPrefs,
    private val tokenManager: TokenManager
) {
    private val mutex = Mutex()

    suspend fun sync() {
        if (mutex.isLocked) return
        if (!NetworkUtils.isOnline(context)) return
        mutex.withLock {
            try {
                push()
                pull()
                // Last, and deliberately after pull(): reconciling bookmarks needs the rows that
                // pull() may have just inserted, otherwise a bookmark on a contact this device is
                // seeing for the first time has nothing to attach to.
                syncBookmarks()
            } catch (e: Exception) {
                Log.w(TAG, "Sync failed, will retry next trigger", e)
            }
        }
    }

    /**
     * Bookmarks do not travel with the contact documents — they are a per-user set on the user
     * record — so they get their own push/pull round separate from [push] and [pull].
     */
    private suspend fun syncBookmarks() {
        var profile: UserDto? = null

        // Push local toggles first so the profile we read back already reflects them.
        dao.getBookmarkDirtyContacts().forEach { row ->
            val serverId = row.serverId
            if (serverId == null) {
                // Never synced, so there is nothing to bookmark against yet. Leave the row dirty:
                // once its create is accepted it gains a serverId and the next round pushes it.
                return@forEach
            }
            profile = try {
                if (row.bookmarked) api.addBookmark(serverId) else api.removeBookmark(serverId)
            } catch (e: Exception) {
                // Stays dirty and retries on the next trigger. One failure must not abort the
                // whole loop, or a single stale id would wedge every other pending toggle.
                Log.w(TAG, "Bookmark push failed for $serverId", e)
                return@forEach
            }
            dao.clearBookmarkDirty(row.id)
        }

        val latest = profile ?: api.getProfile()
        tokenManager.updateUser(latest)
        applyBookmarkSet(latest.bookmarkedContactIds)
    }

    private suspend fun applyBookmarkSet(serverIds: List<String>) {
        if (serverIds.isEmpty()) {
            dao.clearAllBookmarks()
            return
        }
        dao.markBookmarked(serverIds)
        dao.clearBookmarksOutside(serverIds)
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
            if (remote.deleted) {
                // The server refused to hand the record back — it was deleted, or its owner turned
                // it private after we synced it. Either way this copy is no longer ours to keep,
                // and dropping it stops us re-pushing the edit on every cycle.
                dao.purgeLocal(row.id)
                return@forEachIndexed
            }
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
                    dao.updateContact(remote.toLocalContact(existing = local))
                else -> {
                    if (remote.parsedUpdatedAt() > local.updatedAt) {
                        dao.updateContact(remote.toLocalContact(existing = local))
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
        isPrivate = isPrivate
    )

    /**
     * Builds the local row for a pulled contact. [existing] is the row being replaced, if any:
     * bookmark state is per-user and never present on a contact document, so it has to be carried
     * across by hand. Dropping it here would silently un-bookmark a contact every time the server
     * reported any unrelated edit to it.
     */
    private fun RemoteContact.toLocalContact(existing: HrContact? = null): HrContact = HrContact(
        id = existing?.id ?: 0,
        name = name,
        company = company,
        mobile = mobile ?: "",
        emails = emails ?: emptyList(),
        linkedinProfile = linkedinProfile ?: "",
        verified = verified,
        isPrivate = isPrivate,
        createdBy = createdBy ?: existing?.createdBy,
        bookmarked = existing?.bookmarked ?: false,
        bookmarkDirty = existing?.bookmarkDirty ?: false,
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
