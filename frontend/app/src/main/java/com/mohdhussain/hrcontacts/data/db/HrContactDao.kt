package com.mohdhussain.hrcontacts.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mohdhussain.hrcontacts.data.model.HrContact

@Dao
interface HrContactDao {

    @Query("SELECT * FROM hr_contacts WHERE pendingAction != 'DELETE' ORDER BY company ASC, name ASC")
    fun getAllContacts(): LiveData<List<HrContact>>

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
}
