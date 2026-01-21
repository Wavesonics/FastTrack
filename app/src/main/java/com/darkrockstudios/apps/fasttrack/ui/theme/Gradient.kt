package com.darkrockstudios.apps.fasttrack.ui.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Colors from fast_background.xml (light mode)
val LightGradientStartColor = Color(0xFFE9DBFF) // #E9DBFF
val LightGradientEndColor = Color(0xFFC2D7FF) // #C2D7FF

// Colors from fast_background.xml (dark mode) - darker variant
val DarkGradientStartColor = Color(0xFF2A1F3D) // Darker purple
val DarkGradientEndColor = Color(0xFF1A1A1A) // Darker gray

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
fun Modifier.fastBackgroundGradient(show: Boolean): Modifier = composed {
	if (show) {
		val isDarkTheme = LocalDarkTheme.current

		val startColor = if (isDarkTheme) DarkGradientStartColor else LightGradientStartColor
		val endColor = if (isDarkTheme) DarkGradientEndColor else LightGradientEndColor

		val gradientBrush = Brush.verticalGradient(
			colors = listOf(startColor, endColor)
		)

		this.background(brush = gradientBrush)
	} else {
		this
	}
}
