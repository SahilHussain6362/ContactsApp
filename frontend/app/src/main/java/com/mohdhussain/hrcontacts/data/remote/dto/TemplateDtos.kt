package com.mohdhussain.hrcontacts.data.remote.dto

import com.squareup.moshi.JsonClass

/**
 * The user's saved message templates, as they travel inside [UserDto].
 *
 * The two shapes differ on purpose: a mail has a subject line to fill, a WhatsApp chat does not.
 */
@JsonClass(generateAdapter = true)
data class EmailTemplateDto(
    val id: String,
    val heading: String,
    val body: String
)

@JsonClass(generateAdapter = true)
data class WhatsappTemplateDto(
    val id: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class EmailTemplateRequestDto(
    val heading: String,
    val body: String
)

@JsonClass(generateAdapter = true)
data class WhatsappTemplateRequestDto(
    val message: String
)

/** How many templates of one type the server accepts. Mirrors TemplateService.MAX_PER_TYPE. */
const val MAX_TEMPLATES_PER_TYPE = 3

/**
 * A one-line label for a WhatsApp template — its first line, shortened. Used wherever a template
 * has to be named in a list, which for WhatsApp means borrowing from the message itself since
 * there is no heading to use.
 */
fun WhatsappTemplateDto.label(): String {
    val firstLine = message.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
    return if (firstLine.length <= LABEL_MAX_CHARS) {
        firstLine
    } else {
        firstLine.take(LABEL_MAX_CHARS).trimEnd() + "…"
    }
}

private const val LABEL_MAX_CHARS = 40
