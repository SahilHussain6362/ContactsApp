package com.mohdhussain.hrcontacts.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

object PendingAction {
    const val NONE = "NONE"
    const val CREATE = "CREATE"
    const val UPDATE = "UPDATE"
    const val DELETE = "DELETE"
}

@Entity(tableName = "hr_contacts")
data class HrContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val company: String,
    val mobile: String,
    val emails: List<String> = emptyList(),
    val linkedinProfile: String = "",
    val serverId: String? = null,
    val updatedAt: Long = 0L,
    val pendingAction: String = PendingAction.CREATE
)
