package com.darkrockstudios.apps.fasttrack

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.screens.main.MainActivity
import com.darkrockstudios.apps.fasttrack.utils.getColorFor
import io.github.aakira.napier.Napier
import kotlin.time.Duration

object FastingNotificationManager {
	const val FASTING_CHANNEL_ID = "fasting_status"
	const val NOTIFICATION_ID = 1000

	fun postFastingNotification(
		context: Context,
		elapsedTime: Duration
	) {
		Napier.d("Posting fasting notification")

		// Check if we have notification permission (Android 13+)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ActivityCompat.checkSelfPermission(
					context,
					Manifest.permission.POST_NOTIFICATIONS
				) != PackageManager.PERMISSION_GRANTED
			) {
				Napier.w("Notification permission not granted, cannot post fasting notification")
				return
			}
		}

		createNotificationChannel(context)

		val elapsedHours = elapsedTime.inWholeHours
		val currentPhase = Stages.getCurrentPhase(elapsedTime)

		// Get current stage
		var stageIndex = Stages.stage.indexOfLast { it.hours <= elapsedHours }
		if (stageIndex < 0) {
			stageIndex = 0
		}
		val stage = Stages.stage[stageIndex]

		// Format elapsed time
		val timeText = elapsedTime.toComponents { hours, _, _, _ ->
			"%d".format(hours)
		}

		// Get energy mode
		val energyMode = if (currentPhase.fatBurning) {
			context.getString(R.string.fasting_energy_mode_fat)
		} else {
			context.getString(R.string.fasting_energy_mode_glucose)
		}

		// Build notification content
		val title = context.getString(R.string.notification_fasting_title, timeText)
		val contentText = context.getString(stage.title)
		val expandedText = buildString {
			append(context.getString(stage.title))
			append("\n")
			append(context.getString(R.string.notification_fasting_energy_mode, energyMode))
			append("\n")
			append(context.getString(stage.description))
		}

		val intent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		}
		val pendingIntent = PendingIntent.getActivity(
			context,
			0,
			intent,
			PendingIntent.FLAG_IMMUTABLE
		)

		// Check if we're on API 36+ for progress-centric notification
		val notification = if (Build.VERSION.SDK_INT >= 36) {
			// Scale progress to match equal-sized segments
			// Find which phase we're in and calculate progress within that phase
			val currentPhaseIndex = Stages.phases.indexOfLast { elapsedHours >= it.hours }.coerceAtLeast(0)
			val currentPhase = Stages.phases[currentPhaseIndex]
			val nextPhase = Stages.phases.getOrNull(currentPhaseIndex + 1)

			val phaseStartHours = currentPhase.hours.toFloat()
			val phaseEndHours = nextPhase?.hours?.toFloat() ?: Stages.END_TIME
			val progressInPhase =
				((elapsedHours.toFloat() - phaseStartHours) / (phaseEndHours - phaseStartHours)).coerceIn(0f, 1f)

			// Each segment is equal size (1000 / number of phases)
			val segmentLength = 1000 / Stages.phases.size
			val normalizedProgress =
				((currentPhaseIndex * segmentLength) + (progressInPhase * segmentLength)).toInt().coerceIn(0, 1000)

			// Create progress segments for each fasting stage with equal sizes
			val progressStyle = Notification.ProgressStyle().apply {
				setProgress(normalizedProgress)

				setProgressTrackerIcon(Icon.createWithResource(context, R.drawable.ic_notification_progress))
				setStyledByProgress(false)

				// Progress segments are equal size in pixels when displayed
				val segmentLength = 1000 / Stages.phases.size
				Stages.phases.forEach { phase ->
					addProgressSegment(Notification.ProgressStyle.Segment(segmentLength).apply {
						setColor(getColorFor(phase).toArgb())
					})
				}
			}

			Notification.Builder(context, FASTING_CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_alert)
				.setContentTitle(title)
				.setContentText(contentText)
				.setStyle(progressStyle)
				.setOngoing(true)
				.setContentIntent(pendingIntent)
				.setCategory(Notification.CATEGORY_STATUS)
				.setAutoCancel(false)
				.build()
		} else {
			NotificationCompat.Builder(context, FASTING_CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_alert)
				.setContentTitle(title)
				.setContentText(contentText)
				.setStyle(
					NotificationCompat.BigTextStyle()
						.bigText(expandedText)
						.setBigContentTitle(title)
				)
				.setOngoing(true)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setContentIntent(pendingIntent)
				.setAutoCancel(false)
				.build()
		}

		val notificationManager = context.getSystemService(NotificationManager::class.java)

		Napier.d("Notification channel created: ${notificationManager.getNotificationChannel(FASTING_CHANNEL_ID) != null}")
		Napier.d("Notifications enabled: ${notificationManager.areNotificationsEnabled()}")
		Napier.d("About to notify with ID: $NOTIFICATION_ID")

		notificationManager.notify(NOTIFICATION_ID, notification)

		Napier.d("Notification posted successfully")
	}

	fun cancelFastingNotification(context: Context) {
		Napier.d("Canceling fasting notification")
		val notificationManager = context.getSystemService(NotificationManager::class.java)
		notificationManager.cancel(NOTIFICATION_ID)
	}

	private fun createNotificationChannel(context: Context) {
		val name = context.getString(R.string.notification_fasting_channel_name)
		val descriptionText = context.getString(R.string.notification_fasting_channel_description)
		val importance = NotificationManager.IMPORTANCE_LOW
		val channel = NotificationChannel(FASTING_CHANNEL_ID, name, importance).apply {
			description = descriptionText
			setSound(null, null)
			enableVibration(false)
		}

		val notificationManager = context.getSystemService(NotificationManager::class.java)
		notificationManager.createNotificationChannel(channel)
	}
}
