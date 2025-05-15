package com.darkrockstudios.apps.fasttrack.widget

import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.material.ColorProviders

object FastingWidgetColorScheme {
	private val purple500 = Color(0xFF6200ee)
	private val purple200 = Color(0xFFA15BF8)
	private val purple700 = Color(0xFF3700b3)
	private val purple600 = Color(0xFF4b01d0)
	private val white = Color(0xFFFFFFFF)
	private val black = Color(0xFF000000)
	private val gray = Color(0xFFE0E0E0)
	private val darkGray = Color(0xFF303030)
	private val subtleBackground = Color(0xFFE0E8FF)
	private val subtleBackgroundDark = Color(0xFF36195A)
	private val brightPurple = Color(0xFFC39DFF)
	private val brightishPurple = Color(0xFF8A5AC6)
	private val green500 = Color(0xFF018786)
	private val red600 = Color(0xFFb00020)
	private val red200 = Color(0xFFcf6679)

	val colors = ColorProviders(
		light = Colors(
			primary = purple500,
			onPrimary = gray,
			secondary = brightPurple,
			onSecondary = darkGray,
			background = subtleBackground,
			onBackground = black,
			surface = white,
			onSurface = black,
			error = red600,
			onError = white,
			primaryVariant = purple700,
			secondaryVariant = green500,
			isLight = true
		),
		dark = Colors(
			primary = purple200,
			onPrimary = darkGray,
			secondary = brightishPurple,
			onSecondary = gray,
			background = subtleBackgroundDark,
			onBackground = white,
			surface = Color(0xFF121212), // black_800
			onSurface = white,
			error = red200,
			onError = white,
			primaryVariant = purple600,
			secondaryVariant = brightishPurple,
			isLight = false
		)
	)
}

@Composable
fun FastingWidgetTheme(content: @Composable () -> Unit) {
	GlanceTheme(
		colors = FastingWidgetColorScheme.colors,
		content = content
	)
}
