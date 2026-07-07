package com.mohdhussain.hrcontacts.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactRequestDto(
    val name: String,
    val company: String,
    val mobile: String?,
    val emails: List<String>?,
    val linkedinProfile: String?
)
