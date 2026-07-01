package com.mohdhussain.hrcontacts.data.repository

import androidx.lifecycle.LiveData
import com.mohdhussain.hrcontacts.data.db.HrContactDao
import com.mohdhussain.hrcontacts.data.model.HrContact

class ContactRepository(private val dao: HrContactDao) {

    val allContacts: LiveData<List<HrContact>> = dao.getAllContacts()

    fun getContactById(id: Long): LiveData<HrContact> = dao.getContactById(id)

    suspend fun insertContact(contact: HrContact): Long = dao.insertContact(contact)

    suspend fun updateContact(contact: HrContact) = dao.updateContact(contact)

    suspend fun deleteContact(contact: HrContact) = dao.deleteContact(contact)

    suspend fun deleteByIds(ids: List<Long>) = dao.deleteByIds(ids)
}
