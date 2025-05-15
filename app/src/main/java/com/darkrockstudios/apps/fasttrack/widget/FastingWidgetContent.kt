package com.darkrockstudios.apps.fasttrack.widget

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.screens.main.MainActivity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
fun FastingWidgetContent() {
	val context = LocalContext.current

	val isFasting = isFasting(context)
	val elapsedTime = if (isFasting) {
		getElapsedFastTime(context)
	} else {
		null
	}

	val mainActivityIntent = Intent(context, MainActivity::class.java)

	val startFastIntent = Intent(context, MainActivity::class.java).apply {
		putExtra(MainActivity.START_FAST_EXTRA, true)
	}

	Column(
		modifier = GlanceModifier
			.fillMaxSize()
			.appWidgetBackground()
			.background(GlanceTheme.colors.background)
			.clickable(actionStartActivity(mainActivityIntent)),
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
				onClick = actionStartActivity(startFastIntent),
				modifier = GlanceModifier.padding(16.dp)
			)
		}
	}
}

/**
 * Check if a fast is currently in progress.
 */
private fun isFasting(context: Context): Boolean {
	val storage = PreferenceManager.getDefaultSharedPreferences(context)
	val fastStart = storage.getLong(Data.KEY_FAST_START, -1)
	val fastEnd = storage.getLong(Data.KEY_FAST_END, -1)

	return fastStart > 0 && fastEnd <= 0
}

/**
 * Get the elapsed time of the current fast.
 */
private fun getElapsedFastTime(context: Context): Duration? {
	val storage = PreferenceManager.getDefaultSharedPreferences(context)
	val fastStart = storage.getLong(Data.KEY_FAST_START, -1)

	return if (fastStart > 0) {
		val startInstant = Instant.fromEpochMilliseconds(fastStart)
		val now = Clock.System.now()
		now - startInstant
	} else {
		null
	}
}
