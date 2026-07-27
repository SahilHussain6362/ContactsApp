package com.mohdhussain.hrcontacts.data.remote.dto

import com.squareup.moshi.JsonClass

/** Only the display name is editable — email and provider belong to the identity provider. */
@JsonClass(generateAdapter = true)
data class UpdateProfileRequestDto(
    val name: String
)
