package com.mohdhussain.hrcontacts.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SyncChangeDto(
    val action: String,
    val serverId: String?,
    val contact: ContactRequestDto?,
    val clientUpdatedAt: Long?
)

object SyncAction {
    const val CREATE = "CREATE"
    const val UPDATE = "UPDATE"
    const val DELETE = "DELETE"
}
