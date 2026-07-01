package com.mohdhussain.hrcontacts.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hr_contacts")
data class HrContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val company: String,
    val mobile: String,
    val email: String
)
