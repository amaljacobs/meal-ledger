package com.amaljacobs.mealledger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = LedgerMint,
    onPrimary = LedgerInk,
    primaryContainer = LedgerMintContainer,
    onPrimaryContainer = LedgerMint,
    secondary = LedgerBlue,
    onSecondary = LedgerInk,
    secondaryContainer = LedgerBlueContainer,
    onSecondaryContainer = LedgerBlue,
    tertiary = LedgerAmber,
    onTertiary = LedgerInk,
    tertiaryContainer = LedgerAmberContainer,
    onTertiaryContainer = LedgerAmber,
    background = LedgerInk,
    onBackground = LedgerText,
    surface = LedgerSurface,
    onSurface = LedgerText,
    surfaceVariant = LedgerSurfaceRaised,
    onSurfaceVariant = LedgerMutedText,
    surfaceContainerHighest = LedgerSurfaceBright,
    outline = LedgerOutline,
    outlineVariant = LedgerOutline.copy(alpha = 0.65f),
    error = LedgerError,
    onError = LedgerInk,
    errorContainer = LedgerErrorContainer,
    onErrorContainer = LedgerError,
)

val LedgerShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(18.dp),
)

@Composable
fun MealLedgerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = LedgerShapes,
        content = content
    )
}
