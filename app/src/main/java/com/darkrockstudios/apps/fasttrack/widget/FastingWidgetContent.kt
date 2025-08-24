package com.darkrockstudios.apps.fasttrack.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
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

	val isFasting = activeFastRepository.isFasting()
	val elapsedTime = remember(isFasting) {
		if (isFasting) {
			activeFastRepository.getElapsedFastTime()
		} else {
			null
		}
	}

	Column(
		modifier = GlanceModifier
			.fillMaxSize()
			.appWidgetBackground()
			.background(GlanceTheme.colors.background)
			.clickable(onClick),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		if (isFasting && elapsedTime != null) {
			val curPhase = Stages.getCurrentPhase(elapsedTime)
			Text(
				modifier = GlanceModifier.background(GlanceTheme.colors.primary).fillMaxWidth().padding(8.dp),
				text = context.getString(R.string.app_widget_fast_in_progress),
				style = TextStyle(
					fontWeight = FontWeight.Bold,
					color = GlanceTheme.colors.onError,
				)
			)

			Spacer(modifier = GlanceModifier.height(4.dp))

			Column(modifier = GlanceModifier.padding(16.dp)) {
				Text(
					text = context.getString(R.string.app_widget_time, elapsedTime.inWholeHours),
					style = TextStyle(
						fontWeight = FontWeight.Bold,
						color = GlanceTheme.colors.onBackground,
					)
				)

				Spacer(modifier = GlanceModifier.height(4.dp))

				Text(
					text = context.getString(curPhase.title),
					style = TextStyle(
						fontWeight = FontWeight.Bold,
						color = GlanceTheme.colors.onBackground,
					),
					modifier = GlanceModifier
						.padding(horizontal = 24.dp, vertical = 12.dp)
				)

				val energyMode = if (curPhase.fatBurning) {
					context.getString(R.string.fasting_energy_mode_fat)
				} else {
					context.getString(R.string.fasting_energy_mode_glucose)
				}

				Text(
					text = context.getString(R.string.fasting_energy_mode, energyMode),
					style = TextStyle(
						color = GlanceTheme.colors.onBackground,
					)
				)

				Spacer(modifier = GlanceModifier.height(16.dp))

				Button(
					text = context.getString(R.string.app_widget_stop_fast_button),
					onClick = onStopFast,
					modifier = GlanceModifier.padding(16.dp)
				)
			}
		} else {
			Text(
				modifier = GlanceModifier.background(GlanceTheme.colors.primary).fillMaxWidth().padding(8.dp),
				text = context.getString(R.string.app_widget_not_fasting),
				style = TextStyle(
					fontWeight = FontWeight.Bold,
					color = GlanceTheme.colors.onError,
				)
			)

			Spacer(modifier = GlanceModifier.height(16.dp))

			Button(
				text = context.getString(R.string.app_widget_start_fast_button),
				onClick = onStartFast,
				modifier = GlanceModifier.padding(16.dp)
			)
		}
	}
}
