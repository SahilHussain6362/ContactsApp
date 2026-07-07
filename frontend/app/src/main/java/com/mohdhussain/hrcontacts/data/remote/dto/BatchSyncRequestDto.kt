package com.mohdhussain.hrcontacts.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BatchSyncRequestDto(
    val changes: List<SyncChangeDto>
)
