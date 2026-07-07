package com.mohdhussain.hrcontacts.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterVerifyRequestDto(
    val email: String,
    val otp: String,
    val password: String,
    val name: String?
)
