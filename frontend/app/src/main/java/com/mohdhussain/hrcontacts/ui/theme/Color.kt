package com.mohdhussain.hrcontacts.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * The app's colour source of truth.
 *
 * Both schemes are written out in full — the previous XML night theme overrode only some of the
 * roles it needed, which left every tonal button falling back to the Material baseline lavender in
 * dark mode. Naming every role in both schemes is what stops that class of bug returning.
 *
 * The palette is anchored on the product's existing blue: a cool, institutional hue that reads as
 * trustworthy without becoming a brand statement. Neutrals are very slightly blue-shifted so cards
 * sit calmly against the background instead of vibrating against a pure grey.
 */

// ---------------------------------------------------------------------------------------------
// Light
// ---------------------------------------------------------------------------------------------

private val Primary = Color(0xFF1257A6)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFD8E2FF)
private val OnPrimaryContainer = Color(0xFF001A41)

private val Secondary = Color(0xFF4A5C78)
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFDCE3F2)
private val OnSecondaryContainer = Color(0xFF101C2B)

private val Tertiary = Color(0xFF6F5B00)
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFFFFE08A)
private val OnTertiaryContainer = Color(0xFF231B00)

private val Background = Color(0xFFF7F9FC)
private val OnBackground = Color(0xFF131A22)
private val Surface = Color(0xFFF7F9FC)
private val OnSurface = Color(0xFF131A22)
private val SurfaceVariant = Color(0xFFDFE3EC)
private val OnSurfaceVariant = Color(0xFF434A54)

private val SurfaceContainerLowest = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFFFFFFF)
private val SurfaceContainer = Color(0xFFEFF2F8)
private val SurfaceContainerHigh = Color(0xFFE8ECF4)
private val SurfaceContainerHighest = Color(0xFFE1E6F0)

private val Outline = Color(0xFF737A85)
private val OutlineVariant = Color(0xFFC3C8D2)

private val ErrorLight = Color(0xFFB3261E)
private val OnErrorLight = Color(0xFFFFFFFF)
private val ErrorContainerLight = Color(0xFFF9DEDC)
private val OnErrorContainerLight = Color(0xFF410E0B)

private val InverseSurfaceLight = Color(0xFF283039)
private val InverseOnSurfaceLight = Color(0xFFEFF2F8)
private val InversePrimaryLight = Color(0xFFA9C7FF)

// ---------------------------------------------------------------------------------------------
// Dark
// ---------------------------------------------------------------------------------------------

private val PrimaryDark = Color(0xFFA9C7FF)
private val OnPrimaryDark = Color(0xFF002E68)
private val PrimaryContainerDark = Color(0xFF004494)
private val OnPrimaryContainerDark = Color(0xFFD7E3FF)

private val SecondaryDark = Color(0xFFB7C6E0)
private val OnSecondaryDark = Color(0xFF212F44)
private val SecondaryContainerDark = Color(0xFF33405A)
private val OnSecondaryContainerDark = Color(0xFFDCE3F2)

private val TertiaryDark = Color(0xFFE4C54B)
private val OnTertiaryDark = Color(0xFF3A2F00)
private val TertiaryContainerDark = Color(0xFF544400)
private val OnTertiaryContainerDark = Color(0xFFFFE08A)

private val BackgroundDark = Color(0xFF0F1418)
private val OnBackgroundDark = Color(0xFFE2E6EB)
private val SurfaceDark = Color(0xFF0F1418)
private val OnSurfaceDark = Color(0xFFE2E6EB)
private val SurfaceVariantDark = Color(0xFF414852)
private val OnSurfaceVariantDark = Color(0xFFC1C7D0)

private val SurfaceContainerLowestDark = Color(0xFF0A0E12)
private val SurfaceContainerLowDark = Color(0xFF171C21)
private val SurfaceContainerDark = Color(0xFF1D2328)
private val SurfaceContainerHighDark = Color(0xFF272D33)
private val SurfaceContainerHighestDark = Color(0xFF32383E)

private val OutlineDark = Color(0xFF8B929C)
private val OutlineVariantDark = Color(0xFF414852)

private val ErrorDark = Color(0xFFFFB4AB)
private val OnErrorDark = Color(0xFF690005)
private val ErrorContainerDark = Color(0xFF93000A)
private val OnErrorContainerDark = Color(0xFFFFDAD6)

private val InverseSurfaceDark = Color(0xFFE2E6EB)
private val InverseOnSurfaceDark = Color(0xFF283039)
private val InversePrimaryDark = Color(0xFF1257A6)

val HrLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimaryLight,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceTint = Primary,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    scrim = Color(0xFF000000)
)

val HrDarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    inversePrimary = InversePrimaryDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceTint = PrimaryDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    scrim = Color(0xFF000000)
)

/**
 * Colours that carry a specific product meaning and so have no home in the Material roles:
 * whether a contact is verified, whether it is private, whether it is bookmarked, and WhatsApp's
 * own brand green. Each has a light and a dark value, which is what the old flat colour resources
 * lacked.
 */
data class HrSemanticColors(
    val verifiedContent: Color,
    val verifiedContainer: Color,
    val privateContent: Color,
    val privateContainer: Color,
    val bookmarkActive: Color,
    val bookmarkInactive: Color,
    val whatsapp: Color,
    /** Border for the flat outlined cards the app is built out of. */
    val cardBorder: Color
)

val LightSemanticColors = HrSemanticColors(
    verifiedContent = Color(0xFF0E5C2F),
    verifiedContainer = Color(0xFFCFEBD9),
    privateContent = Color(0xFF7A4100),
    privateContainer = Color(0xFFFFE2BF),
    bookmarkActive = Color(0xFFC77700),
    bookmarkInactive = Color(0xFF8A9199),
    whatsapp = Color(0xFF1FA855),
    cardBorder = OutlineVariant
)

val DarkSemanticColors = HrSemanticColors(
    verifiedContent = Color(0xFF9DDBB4),
    verifiedContainer = Color(0xFF123D26),
    privateContent = Color(0xFFFFCF95),
    privateContainer = Color(0xFF4A2C00),
    bookmarkActive = Color(0xFFFFC24D),
    bookmarkInactive = Color(0xFF838A94),
    whatsapp = Color(0xFF4ADE80),
    cardBorder = OutlineVariantDark
)
