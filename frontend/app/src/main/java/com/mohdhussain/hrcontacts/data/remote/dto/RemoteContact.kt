package com.mohdhussain.hrcontacts.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteContact(
    val id: String,
    val name: String,
    val company: String,
    val mobile: String?,
    val emails: List<String>?,
    val linkedinProfile: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val deleted: Boolean
)
