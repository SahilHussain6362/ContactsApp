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
    val bookmarkedContactIds: List<String> = emptyList(),
    // Same reasoning: a session cached before templates existed has neither of these keys.
    val emailTemplates: List<EmailTemplateDto> = emptyList(),
    val whatsappTemplates: List<WhatsappTemplateDto> = emptyList()
)
