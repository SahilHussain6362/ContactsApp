package com.mohdhussain.hrcontacts.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mohdhussain.hrcontacts.data.model.HrContact

@Dao
interface HrContactDao {

    @Query("SELECT * FROM hr_contacts WHERE pendingAction != 'DELETE' ORDER BY company ASC, name ASC")
    fun getAllContacts(): LiveData<List<HrContact>>

    @Query("SELECT * FROM hr_contacts WHERE bookmarked = 1 AND pendingAction != 'DELETE' ORDER BY company ASC, name ASC")
    fun getBookmarkedContacts(): LiveData<List<HrContact>>

    // Rows created on this device carry createdBy from the moment they are inserted, so a contact
    // the user just added appears here before the first sync round-trip confirms ownership.
    @Query("SELECT * FROM hr_contacts WHERE createdBy = :userId AND pendingAction != 'DELETE' ORDER BY company ASC, name ASC")
    fun getContactsCreatedBy(userId: String): LiveData<List<HrContact>>

    @Query("SELECT * FROM hr_contacts WHERE id = :id AND pendingAction != 'DELETE'")
    fun getContactById(id: Long): LiveData<HrContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: HrContact): Long

    @Update
    suspend fun updateContact(contact: HrContact)

    @Delete
    suspend fun deleteContact(contact: HrContact)

    @Query("DELETE FROM hr_contacts WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT * FROM hr_contacts WHERE id IN (:ids)")
    suspend fun getContactsByIds(ids: List<Long>): List<HrContact>

    @Query("SELECT * FROM hr_contacts WHERE pendingAction != 'NONE'")
    suspend fun getPendingContacts(): List<HrContact>

    @Query("SELECT * FROM hr_contacts WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: String): HrContact?

    @Query("UPDATE hr_contacts SET serverId = :serverId, updatedAt = :updatedAt, pendingAction = 'NONE' WHERE id = :localId")
    suspend fun markSynced(localId: Long, serverId: String, updatedAt: Long)

    @Query("UPDATE hr_contacts SET pendingAction = 'NONE', updatedAt = :updatedAt WHERE id = :localId")
    suspend fun clearPendingAction(localId: Long, updatedAt: Long)

    @Query("UPDATE hr_contacts SET pendingAction = 'DELETE', updatedAt = :updatedAt WHERE id = :localId")
    suspend fun markPendingDelete(localId: Long, updatedAt: Long)

    @Query("DELETE FROM hr_contacts WHERE id = :localId")
    suspend fun purgeLocal(localId: Long)

    @Query("DELETE FROM hr_contacts WHERE serverId = :serverId")
    suspend fun deleteByServerId(serverId: String)

    @Query("SELECT * FROM hr_contacts WHERE id = :id")
    suspend fun findById(id: Long): HrContact?

    @Query("UPDATE hr_contacts SET bookmarked = :bookmarked, bookmarkDirty = 1 WHERE id = :localId")
    suspend fun setBookmarkLocally(localId: Long, bookmarked: Boolean)

    @Query("SELECT * FROM hr_contacts WHERE bookmarkDirty = 1")
    suspend fun getBookmarkDirtyContacts(): List<HrContact>

    @Query("UPDATE hr_contacts SET bookmarkDirty = 0 WHERE id = :localId")
    suspend fun clearBookmarkDirty(localId: Long)

    // The two halves of applying the server's bookmark set. Rows still flagged bookmarkDirty are
    // excluded from both: their local value is newer than anything the server has told us yet.
    @Query("UPDATE hr_contacts SET bookmarked = 1 WHERE bookmarkDirty = 0 AND bookmarked = 0 AND serverId IN (:serverIds)")
    suspend fun markBookmarked(serverIds: List<String>)

    @Query("UPDATE hr_contacts SET bookmarked = 0 WHERE bookmarkDirty = 0 AND bookmarked = 1 AND (serverId IS NULL OR serverId NOT IN (:serverIds))")
    suspend fun clearBookmarksOutside(serverIds: List<String>)

    /** Separate from [clearBookmarksOutside] because `NOT IN ()` is not valid SQLite. */
    @Query("UPDATE hr_contacts SET bookmarked = 0 WHERE bookmarkDirty = 0 AND bookmarked = 1")
    suspend fun clearAllBookmarks()
}
