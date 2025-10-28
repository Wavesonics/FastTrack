package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.ui.theme.Purple100
import com.darkrockstudios.apps.fasttrack.ui.theme.Purple700

val phaseTextColor_Light = Purple700
val phaseTextColor_Dark = Purple100

@Composable
fun phaseTextColor(): Color {
	val isDark: Boolean = isSystemInDarkTheme()
	return if (isDark) {
		phaseTextColor_Dark
	} else {
		phaseTextColor_Light
	}
}

// Spacing configuration for responsive layout
data class FastingSpacing(
	val small: Dp,
	val medium: Dp,
	val large: Dp,
	val iconSize: Dp,
	val buttonPaddingHorizontal: Dp,
	val buttonPaddingVertical: Dp,
)

val LocalFastingSpacing = staticCompositionLocalOf {
	FastingSpacing(
		small = 4.dp,
		medium = 8.dp,
		large = 16.dp,
		iconSize = 24.dp,
		buttonPaddingHorizontal = 12.dp,
		buttonPaddingVertical = 8.dp,
	)
}

// Typography configuration for responsive layout
data class FastingTypography(
	val stageTitle: @Composable () -> androidx.compose.ui.text.TextStyle,
	val energyMode: @Composable () -> androidx.compose.ui.text.TextStyle,
	val timerText: @Composable () -> androidx.compose.ui.text.TextStyle,
	val timerMilliseconds: @Composable () -> androidx.compose.ui.text.TextStyle,
	val phaseLabel: @Composable () -> androidx.compose.ui.text.TextStyle,
	val phaseTime: @Composable () -> androidx.compose.ui.text.TextStyle,
	val stageDescription: @Composable () -> androidx.compose.ui.text.TextStyle,
	val checkboxLabel: @Composable () -> androidx.compose.ui.text.TextStyle,
)

val LocalFastingTypography = staticCompositionLocalOf<FastingTypography?> { null }

@Composable
fun fastingTypography(): FastingTypography =
	LocalFastingTypography.current ?: FastingTypography(
		stageTitle = { MaterialTheme.typography.headlineMedium },
		energyMode = { MaterialTheme.typography.labelMedium },
		timerText = { MaterialTheme.typography.displayLarge },
		timerMilliseconds = { MaterialTheme.typography.headlineMedium },
		phaseLabel = { MaterialTheme.typography.headlineSmall },
		phaseTime = { MaterialTheme.typography.headlineMedium },
		stageDescription = { MaterialTheme.typography.bodyMedium },
		checkboxLabel = { MaterialTheme.typography.labelLarge },
	)

@Composable
fun fastingSpacing(): FastingSpacing = LocalFastingSpacing.current

@Composable
fun rememberFastingSpacing(isCompact: Boolean): FastingSpacing {
	return remember(isCompact) {
		if (isCompact) {
			FastingSpacing(
				small = 2.dp,
				medium = 4.dp,
				large = 8.dp,
				iconSize = 16.dp,
				buttonPaddingHorizontal = 8.dp,
				buttonPaddingVertical = 4.dp,
			)
		} else {
			FastingSpacing(
				small = 4.dp,
				medium = 8.dp,
				large = 16.dp,
				iconSize = 24.dp,
				buttonPaddingHorizontal = 12.dp,
				buttonPaddingVertical = 8.dp,
			)
		}
	}
}

@Composable
fun rememberFastingTypography(isCompact: Boolean): FastingTypography {
	return remember(isCompact) {
		if (isCompact) {
			FastingTypography(
				stageTitle = { MaterialTheme.typography.headlineSmall },
				energyMode = { MaterialTheme.typography.labelSmall },
				timerText = { MaterialTheme.typography.displayMedium },
				timerMilliseconds = { MaterialTheme.typography.headlineSmall },
				phaseLabel = { MaterialTheme.typography.titleMedium },
				phaseTime = { MaterialTheme.typography.titleLarge },
				stageDescription = { MaterialTheme.typography.bodySmall },
				checkboxLabel = { MaterialTheme.typography.labelMedium },
			)
		} else {
			FastingTypography(
				stageTitle = { MaterialTheme.typography.headlineMedium },
				energyMode = { MaterialTheme.typography.labelMedium },
				timerText = { MaterialTheme.typography.displayLarge },
				timerMilliseconds = { MaterialTheme.typography.headlineMedium },
				phaseLabel = { MaterialTheme.typography.headlineSmall },
				phaseTime = { MaterialTheme.typography.headlineMedium },
				stageDescription = { MaterialTheme.typography.bodyMedium },
				checkboxLabel = { MaterialTheme.typography.labelLarge },
			)
		}
	}
}
