package com.darkrockstudios.apps.fasttrack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.work.Configuration
import com.darkrockstudios.apps.fasttrack.data.Phase
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.screens.main.MainActivity
import io.github.aakira.napier.Napier
import org.koin.android.ext.android.inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class AlertService : JobService(), Configuration.Provider {
	private val repository: ActiveFastRepository by inject()
	private val settingsDatasource: SettingsDatasource by inject()

	override fun onStartJob(params: JobParameters): Boolean {
		when (params.jobId) {
			JobId.FAT_BURN.id -> {
				postNotification(
					R.string.notification_fat_burn_title,
					R.string.notification_fat_burn_content,
					NOTIFICATION_ID_FAT_BURN
				)
				updateFastingNotification()
			}

			JobId.KETOSIS.id -> {
				postNotification(
					R.string.notification_ketosis_title,
					R.string.notification_ketosis_content,
					NOTIFICATION_ID_KETOSIS
				)
				updateFastingNotification()
			}

			JobId.AUTOPHAGY.id -> {
				postNotification(
					R.string.notification_autophagy_title,
					R.string.notification_autophagy_content,
					NOTIFICATION_ID_AUTOPHAGY
				)
				updateFastingNotification()
			}

			JobId.OPTIMAL_AUTOPHAGY.id -> {
				postNotification(
					R.string.notification_optimal_autophagy_title,
					R.string.notification_optimal_autophagy_content,
					NOTIFICATION_ID_OPTIMAL_AUTOPHAGY
				)
				updateFastingNotification()
			}

			JobId.HOURLY_UPDATE.id -> {
				// Hourly update job: update the fasting notification
				updateFastingNotification()

				// Schedule the next hourly update if still fasting
				if (repository.isFasting() && settingsDatasource.getShowFastingNotification()) {
					scheduleHourlyUpdate(this)
				}
			}
		}

		jobFinished(params, false)

		return false
	}

	override fun onStopJob(params: JobParameters?): Boolean {
		return false
	}

	private fun postNotification(
		@StringRes title: Int,
		@StringRes content: Int,
		notificationId: Int
	) {
		createNotificationChannel()

		val contentStr = getString(content)

		val intent = Intent(this, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		}
		val pendingIntent: PendingIntent =
			PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

		val builder = NotificationCompat.Builder(this, ALERTS_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_alert)
			.setContentTitle(getString(title))
			.setContentText(contentStr)
			.setStyle(
				NotificationCompat.BigTextStyle()
					.bigText(contentStr)
			)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setContentIntent(pendingIntent)
			.setAutoCancel(true)

		val notificationManager = getSystemService(NotificationManager::class.java)
		notificationManager.notify(NOTIFICATION_TAG, notificationId, builder.build())
	}

	private fun createNotificationChannel() {
		val name = getString(R.string.notification_channel_name)
		val descriptionText = getString(R.string.notification_channel_description)
		val importance = NotificationManager.IMPORTANCE_DEFAULT
		val channel = NotificationChannel(ALERTS_CHANNEL_ID, name, importance).apply {
			description = descriptionText
		}
		// Register the channel with the system
		val notificationManager = getSystemService(NotificationManager::class.java)
		notificationManager.createNotificationChannel(channel)
	}

	private fun updateFastingNotification() {
		if (repository.isFasting() && settingsDatasource.getShowFastingNotification()) {
			val elapsedTime = repository.getElapsedFastTime()
			FastingNotificationManager.postFastingNotification(this, elapsedTime)
		}
	}

	companion object {
		const val ALERTS_CHANNEL_ID = "fast_alerts"
		const val NOTIFICATION_TAG = "alerts"

		const val NOTIFICATION_ID_FAT_BURN = 1
		const val NOTIFICATION_ID_KETOSIS = 2
		const val NOTIFICATION_ID_AUTOPHAGY = 3
		const val NOTIFICATION_ID_OPTIMAL_AUTOPHAGY = 4

		enum class JobId(val id: Int) {
			FAT_BURN(1),
			KETOSIS(2),
			AUTOPHAGY(3),
			OPTIMAL_AUTOPHAGY(4),
			HOURLY_UPDATE(5),
		}

		private val LEEWAY = 30.minutes

		fun cancelAlerts(context: Context) {
			val jobScheduler = context.getSystemService(JobScheduler::class.java)
			// Cancel stage alerts
			jobScheduler.cancel(JobId.FAT_BURN.id)
			jobScheduler.cancel(JobId.KETOSIS.id)
			jobScheduler.cancel(JobId.AUTOPHAGY.id)
			jobScheduler.cancel(JobId.OPTIMAL_AUTOPHAGY.id)
			// Cancel hourly updates
			cancelHourlyUpdates(context)
		}

		fun cancelHourlyUpdates(context: Context) {
			val jobScheduler = context.getSystemService(JobScheduler::class.java)
			jobScheduler.cancel(JobId.HOURLY_UPDATE.id)
		}

		fun scheduleAlerts(elapsedTime: Duration, context: Context) {
			val elapsedHours = elapsedTime.inWholeHours

			JobId.entries.forEach { jobId ->
				when (jobId) {
					JobId.FAT_BURN -> schedulePhase(
						elapsedHours,
						Stages.PHASE_FAT_BURN,
						jobId.id,
						context
					)

					JobId.KETOSIS -> schedulePhase(
						elapsedHours,
						Stages.PHASE_KETOSIS,
						jobId.id,
						context
					)

					JobId.AUTOPHAGY -> schedulePhase(
						elapsedHours,
						Stages.PHASE_AUTOPHAGY,
						jobId.id,
						context
					)

					JobId.OPTIMAL_AUTOPHAGY -> schedulePhase(
						elapsedHours,
						Stages.PHASE_OPTIMAL_AUTOPHAGY,
						jobId.id,
						context
					)

					JobId.HOURLY_UPDATE -> {
						scheduleHourlyUpdate(context)
					}
				}
			}
		}

		private fun schedulePhase(elapsedHours: Long, phase: Phase, jobId: Int, context: Context) {
			if (elapsedHours < phase.hours) {
				val msUntil = phase.hours.minus(elapsedHours).hours.inWholeMilliseconds
				scheduleAlert(jobId, msUntil, context)
			}
		}

		private fun scheduleAlert(jobId: Int, millsUntil: Long, context: Context) {
			val jobScheduler = context.getSystemService(JobScheduler::class.java)
			if (jobScheduler.getPendingJob(jobId) == null) {
				val builder =
					JobInfo.Builder(jobId, ComponentName(context, AlertService::class.java))
				val job = builder
					.setPersisted(true)
					.setMinimumLatency(millsUntil)
					// Allow the device to decide with some leeway so that this can be batched with other jobs
					.setOverrideDeadline(millsUntil + LEEWAY.inWholeMilliseconds).build()

				jobScheduler.schedule(job)
			} else {
				Napier.w("Tried to schedule Alert Job, but it was already scheduled.")
			}
		}

		fun scheduleHourlyUpdate(context: Context) {
			val jobScheduler = context.getSystemService(JobScheduler::class.java)
			val jobId = JobId.HOURLY_UPDATE.id

			// Cancel any existing hourly update job first
			jobScheduler.cancel(jobId)

			val msUntil = 1.hours.inWholeMilliseconds

			val builder = JobInfo.Builder(jobId, ComponentName(context, AlertService::class.java))
			val job = builder
				.setPersisted(true)
				.setMinimumLatency(msUntil)
				.setOverrideDeadline(msUntil + LEEWAY.inWholeMilliseconds)
				.build()

			jobScheduler.schedule(job)
			Napier.d("Scheduled next hourly update in 1 hour (jobId: $jobId)")
		}
	}

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder()
			.setMinimumLoggingLevel(Log.INFO)
			// Reserve job IDs 1000-2000 for WorkManager (AlertService uses 1-5)
			.setJobSchedulerJobIdRange(1000, 2000)
			.build()
}
