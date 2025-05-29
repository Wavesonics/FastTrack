package com.darkrockstudios.apps.fasttrack.complication

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.wear.complications.data.*
import androidx.wear.complications.datasource.ComplicationRequest
import androidx.wear.complications.datasource.SuspendingComplicationDataSourceService
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.screens.main.MainActivity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


/**
 * Provider service for the Fasting complication on Wear OS.
 * This service exposes fasting data to Wear OS watch faces.
 */
class FastingComplicationProviderService : SuspendingComplicationDataSourceService() {
	@OptIn(ExperimentalTime::class)
	override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
		val context = this
		val isFasting = isFasting(context)

		// Create a pending intent that opens the main activity
		val intent = Intent(context, MainActivity::class.java)
		val pendingIntent = PendingIntent.getActivity(
			context, 0, intent, PendingIntent.FLAG_IMMUTABLE
		)

		val complicationData = when (request.complicationType) {
			ComplicationType.SHORT_TEXT -> {
				if (isFasting) {
					val elapsedTime = getElapsedFastTime(context)
					if (elapsedTime != null) {
						ShortTextComplicationData.Builder(
							text = PlainComplicationText.Builder(
								context.getString(R.string.app_widget_time, elapsedTime.inWholeHours)
							).build(),
							contentDescription = PlainComplicationText.Builder(
								context.getString(R.string.app_widget_fast_in_progress)
							).build()
						)
							.setTapAction(pendingIntent)
							.build()
					} else {
						NoDataComplicationData()
					}
				} else {
					ShortTextComplicationData.Builder(
						text = PlainComplicationText.Builder(
							context.getString(R.string.app_widget_not_fasting)
						).build(),
						contentDescription = PlainComplicationText.Builder(
							context.getString(R.string.app_widget_not_fasting)
						).build()
					)
						.setTapAction(pendingIntent)
						.build()
				}
			}

			ComplicationType.LONG_TEXT -> {
				if (isFasting) {
					val elapsedTime = getElapsedFastTime(context)
					if (elapsedTime != null) {
						LongTextComplicationData.Builder(
							text = PlainComplicationText.Builder(
								context.getString(R.string.app_widget_time, elapsedTime.inWholeHours)
							).build(),
							contentDescription = PlainComplicationText.Builder(
								context.getString(R.string.app_widget_fast_in_progress)
							).build()
						)
							.setTapAction(pendingIntent)
							.build()
					} else {
						NoDataComplicationData()
					}
				} else {
					LongTextComplicationData.Builder(
						text = PlainComplicationText.Builder(
							context.getString(R.string.app_widget_not_fasting)
						).build(),
						contentDescription = PlainComplicationText.Builder(
							context.getString(R.string.app_widget_not_fasting)
						).build()
					)
						.setTapAction(pendingIntent)
						.build()
				}
			}

			ComplicationType.RANGED_VALUE -> {
				RangedValueComplicationData.Builder(
					contentDescription = PlainComplicationText.Builder(
						getString(R.string.app_widget_fast_in_progress)
					).build(),
					min = 0f,
					max = 100f,
					value = 50f,
				)
					.build()
			}

			else -> NoDataComplicationData()
		}

		return complicationData
	}

	@OptIn(ExperimentalTime::class)
	override fun getPreviewData(type: ComplicationType): ComplicationData? {
		return when (type) {
			ComplicationType.SHORT_TEXT -> {
				ShortTextComplicationData.Builder(
					text = PlainComplicationText.Builder("16h").build(),
					contentDescription = PlainComplicationText.Builder(
						getString(R.string.app_widget_fast_in_progress)
					).build()
				)
					.build()
			}

			ComplicationType.LONG_TEXT -> {
				LongTextComplicationData.Builder(
					text = PlainComplicationText.Builder("16h").build(),
					contentDescription = PlainComplicationText.Builder(
						getString(R.string.app_widget_fast_in_progress)
					).build()
				)
					.build()
			}

			ComplicationType.RANGED_VALUE -> {
				RangedValueComplicationData.Builder(
					contentDescription = PlainComplicationText.Builder(
						getString(R.string.app_widget_fast_in_progress)
					).build(),
					min = 0f,
					max = 100f,
					value = 50f,
				)
					.build()
			}

			else -> NoDataComplicationData()
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
}
