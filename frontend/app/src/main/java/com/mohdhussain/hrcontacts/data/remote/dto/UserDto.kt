package com.mohdhussain.hrcontacts.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val name: String?,
    val email: String,
    val provider: String,
    val createdAt: String?,
    // Defaulted because sessions persisted before bookmarks existed decode without this field.
    val bookmarkedContactIds: List<String> = emptyList()
)
