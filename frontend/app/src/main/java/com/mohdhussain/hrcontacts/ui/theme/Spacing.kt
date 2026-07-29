package com.mohdhussain.hrcontacts.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The spacing scale. Every gap in the app comes from here, so a change to the rhythm is one edit
 * rather than a search across screens.
 */
object Spacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
}

/**
 * Fixed sizes that need to agree across components.
 *
 * [MinTouchTarget] is the accessibility floor: the old bookmark and template buttons were 40dp,
 * below the 48dp every tappable thing is now built to.
 */
object Sizes {
    val MinTouchTarget: Dp = 48.dp
    val AvatarSmall: Dp = 40.dp
    val AvatarMedium: Dp = 48.dp
    val AvatarLarge: Dp = 56.dp
    val AvatarXLarge: Dp = 80.dp
    val Icon: Dp = 24.dp
    val IconSmall: Dp = 18.dp
    val CardBorder: Dp = 1.dp
    /** Line length cap, so text does not stretch across a tablet. */
    val ContentMaxWidth: Dp = 640.dp
    /** Width at which the contact list earns a second column. */
    val TwoColumnBreakpoint: Dp = 600.dp
    /** …and a third. */
    val ThreeColumnBreakpoint: Dp = 900.dp
}
