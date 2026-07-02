package com.mohdhussain.hrcontacts.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mohdhussain.hrcontacts.data.model.HrContact

@Dao
interface HrContactDao {

    @Query("SELECT * FROM hr_contacts ORDER BY company ASC, name ASC")
    fun getAllContacts(): LiveData<List<HrContact>>

    @Query("SELECT * FROM hr_contacts WHERE id = :id")
    fun getContactById(id: Long): LiveData<HrContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: HrContact): Long

    @Update
    suspend fun updateContact(contact: HrContact)

    @Delete
    suspend fun deleteContact(contact: HrContact)

    @Query("DELETE FROM hr_contacts WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
