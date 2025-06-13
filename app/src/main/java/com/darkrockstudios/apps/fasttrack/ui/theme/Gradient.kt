package com.darkrockstudios.apps.fasttrack.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Colors from fast_background.xml (light mode)
val LightGradientStartColor = Color(0xFFE9DBFF) // #E9DBFF
val LightGradientEndColor = Color(0xFFC2D7FF) // #C2D7FF

// Colors from fast_background.xml (dark mode)
val DarkGradientStartColor = Color(0xFF4E447A) // #4E447A
val DarkGradientEndColor = Color(0xFF8E8E8E) // #8E8E8E

/**
 * Extension function to apply the fast background gradient to a Modifier.
 * This gradient matches the ones defined in fast_background.xml:
 *
 * Light mode:
 * - startColor: #E9DBFF
 * - endColor: #C2D7FF
 * - angle: 90 degrees (bottom to top)
 *
 * Dark mode:
 * - startColor: #4E447A
 * - endColor: #8E8E8E
 * - angle: 90 degrees (bottom to top)
 */
fun Modifier.fastBackgroundGradient(): Modifier = composed {
	// Determine if we're in dark mode
	val isDarkTheme = isSystemInDarkTheme()

	// Select appropriate colors based on theme
	val startColor = if (isDarkTheme) DarkGradientStartColor else LightGradientStartColor
	val endColor = if (isDarkTheme) DarkGradientEndColor else LightGradientEndColor

	// Create a vertical gradient (bottom to top) to match angle=90 in XML
	val gradientBrush = Brush.verticalGradient(
		colors = listOf(startColor, endColor)
	)

	// Apply the gradient as background
	this.background(brush = gradientBrush)
}
