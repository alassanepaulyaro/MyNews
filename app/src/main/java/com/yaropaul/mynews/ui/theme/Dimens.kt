package com.yaropaul.mynews.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralised dimension constants used across composables.
 * Keeps padding and sizing consistent without scattering magic numbers.
 */
object Dimens {
    // Screen-level padding
    val ScreenPaddingHorizontal = 16.dp
    val ScreenPaddingVertical = 12.dp

    // Generic spacing
    val SmallSpacing = 8.dp
    val ContentSpacing = 16.dp
    val LargeSpacing = 32.dp

    // Images
    val ImageHeight = 240.dp
    val CardImageHeight = 200.dp

    // Cards
    val CardCornerRadius = 12.dp
    val CardElevation = 2.dp
    val CardContentPadding = 12.dp

    // Pills (CategoryPillTag)
    val PillPaddingH = 8.dp
    val PillPaddingV = 3.dp
    val PillCornerRadius = 50.dp

    // Top bar / navigation
    val TopBarPadding = 4.dp

    // Detail screen
    val DetailBodySpacing = 12.dp

    // Misc
    val DividerThickness = 0.5.dp
    val ErrorPadding = 32.dp
}
