package com.mohdhussain.hrcontacts.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * A five-step corner scale. Cards land on [Shapes.medium], sheets on [Shapes.extraLarge] (top
 * corners only, applied at the sheet), and anything the user is meant to reach for first — the
 * search field, a primary call to action — takes [HrPill].
 */
val HrShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Fully rounded, for search fields and primary buttons. */
val HrPill = RoundedCornerShape(percent = 50)
