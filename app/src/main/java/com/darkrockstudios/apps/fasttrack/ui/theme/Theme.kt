package com.darkrockstudios.apps.fasttrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
	primary = Purple500,
	onPrimary = White50,
	primaryContainer = Purple700,
	onPrimaryContainer = White50,
	secondary = Purple200,
	onSecondary = Black900,
	secondaryContainer = PurpleDarker,
	onSecondaryContainer = White50,
	tertiary = Green200,
	onTertiary = Black900,
	tertiaryContainer = Green500,
	onTertiaryContainer = White50,
	error = Red600,
	onError = White50,
	errorContainer = Red600,
	onErrorContainer = White50,
	background = White50,
	onBackground = Black900,
	surface = White50,
	onSurface = Black900,
	surfaceVariant = SubtleBackground,
	onSurfaceVariant = Black900,
	outline = Purple600
)

private val DarkColorScheme = darkColorScheme(
	primary = Purple200,
	onPrimary = Black900,
	primaryContainer = Purple700,
	onPrimaryContainer = White50,
	secondary = BrightPurple,
	onSecondary = Black900,
	secondaryContainer = BrightishPurple,
	onSecondaryContainer = White50,
	tertiary = Green200,
	onTertiary = Black900,
	tertiaryContainer = Green500,
	onTertiaryContainer = White50,
	error = Red200,
	onError = Black900,
	errorContainer = Red600,
	onErrorContainer = White50,
	background = Black800,
	onBackground = White50,
	surface = Black800,
	onSurface = White50,
	surfaceVariant = SubtleBackgroundDark,
	onSurfaceVariant = White50,
	outline = Purple200
)

@Composable
fun FastTrackTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	val colorScheme = if (darkTheme) {
		DarkColorScheme
	} else {
		LightColorScheme
	}

	MaterialTheme(
		colorScheme = colorScheme,
		shapes = Shapes,
		content = content
	)
}
