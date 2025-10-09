package com.darkrockstudios.apps.fasttrack.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.utils.getColorFor
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun FastingWidgetContent(
	activeFastRepository: ActiveFastRepository = koinInject<ActiveFastRepository>(),
	onClick: Action = actionRunCallback<NoOpAction>(),
	onStartFast: Action = actionRunCallback<NoOpAction>(),
	onStopFast: Action = actionRunCallback<NoOpAction>(),
) {
	val context = getGlanceContext()
	val size = LocalSize.current

	val isFasting = activeFastRepository.isFasting()
	val elapsedTime = remember(isFasting) {
		if (isFasting) {
			activeFastRepository.getElapsedFastTime()
		} else {
			null
		}
	}

	when (size) {
		FastingWidget.smallSquare -> {
			SmallWidgetContent(
				context = context,
				isFasting = isFasting,
				elapsedTime = elapsedTime,
				onClick = onClick,
				onStartFast = onStartFast,
				onStopFast = onStopFast
			)
		}

		FastingWidget.horizontalRect -> {
			MediumWidgetContent(
				context = context,
				isFasting = isFasting,
				elapsedTime = elapsedTime,
				onClick = onClick,
				onStartFast = onStartFast,
				onStopFast = onStopFast
			)
		}

		FastingWidget.largeRect -> {
			FullWidgetContent(
				context = context,
				isFasting = isFasting,
				elapsedTime = elapsedTime,
				onClick = onClick,
				onStartFast = onStartFast,
				onStopFast = onStopFast
			)
		}
	}
}

@Composable
private fun SmallWidgetContent(
	context: android.content.Context,
	isFasting: Boolean,
	elapsedTime: kotlin.time.Duration?,
	onClick: Action,
	onStartFast: Action,
	onStopFast: Action
) {
	Box(
		modifier = GlanceModifier
			.fillMaxSize()
			.appWidgetBackground()
			.background(GlanceTheme.colors.background)
			.padding(8.dp)
			.cornerRadius(16.dp)
			.clickable(if (isFasting) onClick else onStartFast),
		contentAlignment = Alignment.Center
	) {
		if (isFasting && elapsedTime != null) {
			val curPhase = Stages.getCurrentPhase(elapsedTime)
			val phaseColor = androidx.glance.color.ColorProvider(getColorFor(curPhase), getColorFor(curPhase))

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalAlignment = Alignment.CenterVertically
			) {
				// Phase color indicator circle
				Box(
					modifier = GlanceModifier
						.size(24.dp)
						.background(phaseColor)
						.cornerRadius(12.dp),
					content = {}
				)

				Spacer(modifier = GlanceModifier.size(4.dp))

				// Time display
				Text(
					text = stringResource(R.string.app_widget_time_compact, elapsedTime.inWholeHours),
					style = TextStyle(
						fontWeight = FontWeight.Bold,
						color = GlanceTheme.colors.onBackground,
						fontSize = 20.sp
					)
				)
			}
		} else {
			// Not fasting - show a simple start indicator
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "▶️",
					style = TextStyle(
						color = GlanceTheme.colors.onBackground,
						fontSize = 24.sp
					)
				)
				Spacer(modifier = GlanceModifier.size(2.dp))
				Text(
					text = stringResource(R.string.widget_start),
					style = TextStyle(
						fontWeight = FontWeight.Medium,
						color = GlanceTheme.colors.onBackground,
						fontSize = 10.sp
					)
				)
			}
		}
	}
}

@Composable
private fun MediumWidgetContent(
	context: android.content.Context,
	isFasting: Boolean,
	elapsedTime: kotlin.time.Duration?,
	onClick: Action,
	onStartFast: Action,
	onStopFast: Action
) {
	Row(
		modifier = GlanceModifier
			.fillMaxSize()
			.appWidgetBackground()
			.background(GlanceTheme.colors.background)
			.padding(12.dp)
			.cornerRadius(16.dp)
			.clickable(if (isFasting) onClick else onStartFast),
		verticalAlignment = Alignment.CenterVertically,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		if (isFasting && elapsedTime != null) {
			val curPhase = Stages.getCurrentPhase(elapsedTime)
			val phaseColor = androidx.glance.color.ColorProvider(getColorFor(curPhase), getColorFor(curPhase))

			// Phase color indicator
			Box(
				modifier = GlanceModifier
					.size(8.dp, 32.dp)
					.background(phaseColor)
					.cornerRadius(4.dp),
				content = {}
			)

			Spacer(modifier = GlanceModifier.defaultWeight())

			Column {
				// Time display
				Text(
					text = context.getString(R.string.app_widget_time, elapsedTime.inWholeHours),
					style = TextStyle(
						fontWeight = FontWeight.Bold,
						color = GlanceTheme.colors.onBackground,
						fontSize = 24.sp
					)
				)

				Spacer(modifier = GlanceModifier.defaultWeight())

				// Phase title
				Text(
					text = context.getString(curPhase.title),
					style = TextStyle(
						fontWeight = FontWeight.Medium,
						color = GlanceTheme.colors.secondary,
						fontSize = 12.sp
					),
					maxLines = 1
				)
			}
		} else {
			Text(
				text = context.getString(R.string.app_widget_not_fasting),
				style = TextStyle(
					fontWeight = FontWeight.Medium,
					color = GlanceTheme.colors.onBackground,
					fontSize = 14.sp
				)
			)
		}
	}
}

@Composable
private fun FullWidgetContent(
	context: android.content.Context,
	isFasting: Boolean,
	elapsedTime: kotlin.time.Duration?,
	onClick: Action,
	onStartFast: Action,
	onStopFast: Action
) {
	Column(
		modifier = GlanceModifier
			.fillMaxSize()
			.appWidgetBackground()
			.background(GlanceTheme.colors.background)
			.padding(16.dp)
			.cornerRadius(16.dp)
			.clickable(onClick),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalAlignment = Alignment.CenterVertically
	) {
		if (isFasting && elapsedTime != null) {
			val curPhase = Stages.getCurrentPhase(elapsedTime)
			val phaseColor = androidx.glance.color.ColorProvider(getColorFor(curPhase), getColorFor(curPhase))

			// Status header
			Text(
				text = context.getString(R.string.app_widget_fast_in_progress),
				style = TextStyle(
					fontWeight = FontWeight.Medium,
					color = GlanceTheme.colors.primary,
					fontSize = 12.sp
				),
				modifier = GlanceModifier.padding(bottom = 8.dp)
			)

			// Phase color indicator box
			Box(
				modifier = GlanceModifier
					.size(64.dp, 8.dp)
					.background(phaseColor)
					.cornerRadius(4.dp)
					.padding(bottom = 12.dp),
				content = {}
			)

			// Large time display
			Text(
				text = context.getString(R.string.app_widget_time, elapsedTime.inWholeHours),
				style = TextStyle(
					fontWeight = FontWeight.Bold,
					color = GlanceTheme.colors.onBackground,
					fontSize = 36.sp
				),
				modifier = GlanceModifier.padding(bottom = 4.dp)
			)

			// Phase title with color accent
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = GlanceModifier.padding(bottom = 8.dp)
			) {
				Box(
					modifier = GlanceModifier
						.size(12.dp)
						.background(phaseColor)
						.cornerRadius(6.dp)
						.padding(end = 16.dp),
					content = {}
				)
				Text(
					text = context.getString(curPhase.title),
					style = TextStyle(
						fontWeight = FontWeight.Bold,
						color = GlanceTheme.colors.secondary,
						fontSize = 16.sp
					),
					maxLines = 1
				)
			}

			// Energy mode badge
			val energyMode = if (curPhase.fatBurning) {
				context.getString(R.string.fasting_energy_mode_fat)
			} else {
				context.getString(R.string.fasting_energy_mode_glucose)
			}

			Text(
				text = context.getString(R.string.fasting_energy_mode, energyMode),
				style = TextStyle(
					color = GlanceTheme.colors.onSurface,
					fontSize = 12.sp
				),
				modifier = GlanceModifier
					.padding(horizontal = 12.dp, vertical = 6.dp)
					.background(GlanceTheme.colors.surface)
					.cornerRadius(8.dp)
			)

			Spacer(modifier = GlanceModifier.defaultWeight())

			// Stop button
			Button(
				text = context.getString(R.string.app_widget_stop_fast_button),
				onClick = onStopFast,
				modifier = GlanceModifier.fillMaxWidth()
			)
		} else {
			// Not fasting state
			Spacer(modifier = GlanceModifier.defaultWeight())

			Text(
				text = context.getString(R.string.app_widget_not_fasting),
				style = TextStyle(
					fontWeight = FontWeight.Medium,
					color = GlanceTheme.colors.onBackground,
					fontSize = 18.sp
				),
				modifier = GlanceModifier.padding(bottom = 16.dp)
			)

			Button(
				text = context.getString(R.string.app_widget_start_fast_button),
				onClick = onStartFast,
				modifier = GlanceModifier.fillMaxWidth()
			)

			Spacer(modifier = GlanceModifier.defaultWeight())
		}
	}
}
