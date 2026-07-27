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
    val verified: Boolean,
    // Defaulted so a build pointed at a server that predates the flag still parses.
    val isPrivate: Boolean = false,
    // Null for contacts the server created before it tracked ownership.
    val createdBy: String? = null,
    val createdAt: String?,
    val updatedAt: String?,
    val deleted: Boolean
)
