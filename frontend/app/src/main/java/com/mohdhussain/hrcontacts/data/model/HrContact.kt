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
    // Read-only mirror of the server flag: the app never sends it, only displays it.
    val verified: Boolean = false,
    // Set by whoever created the contact. The server only ever sends private contacts to their
    // creator, so anything sitting in this table with isPrivate = true belongs to this user.
    val isPrivate: Boolean = false,
    /**
     * Id of the user who created the contact, mirrored from the server. Set locally at creation
     * time too, so a contact the user just added shows up as theirs before the first sync
     * confirms it. Null for contacts authored before the server tracked ownership.
     */
    val createdBy: String? = null,
    /** Whether the signed-in user has bookmarked this contact. Mirrors the server's per-user set. */
    val bookmarked: Boolean = false,
    /**
     * Set when [bookmarked] has been toggled locally but not yet accepted by the server. Sync
     * pushes these rows, and the pull step refuses to overwrite them — otherwise a bookmark
     * made offline would be reverted by the stale set that comes back on the next reconcile.
     */
    val bookmarkDirty: Boolean = false,
    val serverId: String? = null,
    val updatedAt: Long = 0L,
    val pendingAction: String = PendingAction.CREATE
)
