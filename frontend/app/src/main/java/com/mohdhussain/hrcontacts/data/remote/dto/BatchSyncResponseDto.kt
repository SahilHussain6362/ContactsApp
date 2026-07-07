package com.mohdhussain.hrcontacts.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BatchSyncResponseDto(
    val serverTimestamp: Long,
    val contacts: List<RemoteContact>,
    val deletedIds: List<String>
)
