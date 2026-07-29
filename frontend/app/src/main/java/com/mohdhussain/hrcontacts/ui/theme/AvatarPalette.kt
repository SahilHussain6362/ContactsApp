package com.mohdhussain.hrcontacts.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * The colour pair behind an initial-avatar.
 */
data class AvatarColors(val container: Color, val content: Color)

/**
 * Eight hues, keyed off the contact's name.
 *
 * Every avatar used to be `colorPrimary`, which turned a screenful of contacts into a column of
 * identical blue circles with nothing to scan by. Deriving the hue from the name instead gives each
 * person a stable colour — the same contact is the same colour on every screen and every launch,
 * because [String.hashCode] is specified, not incidental.
 *
 * Both variants are hand-picked rather than generated: each pair clears 4.5:1 so an initial stays
 * legible, which a generated hue rotation cannot promise.
 */
private val LightAvatarColors = listOf(
    AvatarColors(Color(0xFFD8E2FF), Color(0xFF0E3A75)),
    AvatarColors(Color(0xFFC8E9E4), Color(0xFF0B4A45)),
    AvatarColors(Color(0xFFD2EBD4), Color(0xFF14512A)),
    AvatarColors(Color(0xFFFBE3B8), Color(0xFF5C3D00)),
    AvatarColors(Color(0xFFFBD9DE), Color(0xFF6E1F2D)),
    AvatarColors(Color(0xFFE3DBFB), Color(0xFF35216E)),
    AvatarColors(Color(0xFFD9DEF7), Color(0xFF23306E)),
    AvatarColors(Color(0xFFDCE2E8), Color(0xFF2C3944))
)

private val DarkAvatarColors = listOf(
    AvatarColors(Color(0xFF17335C), Color(0xFFB6CDF7)),
    AvatarColors(Color(0xFF12403D), Color(0xFF9FD8D0)),
    AvatarColors(Color(0xFF17402A), Color(0xFFA6D6AE)),
    AvatarColors(Color(0xFF46330A), Color(0xFFEFCC85)),
    AvatarColors(Color(0xFF4A2029), Color(0xFFF0B7C0)),
    AvatarColors(Color(0xFF2E2350), Color(0xFFCFC2F2)),
    AvatarColors(Color(0xFF232C55), Color(0xFFBCC5EF)),
    AvatarColors(Color(0xFF29323B), Color(0xFFBFC9D2))
)

/** The stable palette entry for [name]. Blank names all land on the first hue. */
fun avatarColorsFor(name: String, dark: Boolean): AvatarColors {
    val palette = if (dark) DarkAvatarColors else LightAvatarColors
    val key = name.trim().lowercase()
    if (key.isEmpty()) return palette[0]
    return palette[abs(key.hashCode()) % palette.size]
}

/** The single uppercase character shown in an avatar, or `?` when there is nothing to show. */
fun initialFor(name: String): String =
    name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
