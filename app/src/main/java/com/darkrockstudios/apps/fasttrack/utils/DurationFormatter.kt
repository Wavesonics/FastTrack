package com.darkrockstudios.apps.fasttrack.utils

import android.content.Context
import com.darkrockstudios.apps.fasttrack.R
import kotlin.time.Duration

/**
 * Humanized durations are written anywhere in FastTrack.
 *
 * - under a minute:  "Just started" (localized)
 * - under an hour:   "37m"
 * - under a day:     "5h 12m"  (or "5h" when the minutes are zero)
 * - a day and more:  "2d 12h"  (or "2d" when the hours are zero)
 * - [showTotalHours]: "60h 30m" instead of days — the dial's toggled format
 * - [withMinutes] = false: hour-granular ("39h", "1d 15h") for surfaces that
 *   refresh hourly (widget, notification) where minutes would mislead
 */
fun formatDuration(
	context: Context,
	duration: Duration,
	showTotalHours: Boolean = false,
	withMinutes: Boolean = true,
): String {
	val totalMinutes = duration.inWholeMinutes.coerceAtLeast(0)
	val hours = totalMinutes / 60
	val minutes = totalMinutes % 60
	return when {
		withMinutes && totalMinutes < 1 -> context.getString(R.string.duration_less_than_minute)
		withMinutes && hours == 0L -> "${minutes}m"
		// Hour-granular surfaces (notification/widget) in the first hour: "0h" would
		// mislead, so read it as "<1h" until the first whole hour is reached.
		!withMinutes && hours == 0L -> "<1h"
		hours < 24 || showTotalHours ->
			if (minutes == 0L || !withMinutes) "${hours}h" else "${hours}h ${minutes}m"

		else -> {
			val days = hours / 24
			val remHours = hours % 24
			if (remHours == 0L) "${days}d" else "${days}d ${remHours}h"
		}
	}
}

/**
 * Full, minute-precise humanized duration for the logbook CSV, e.g.
 * "5d 3h 27m", "3h 27m", "27m". Days and hours appear only when a larger unit
 * requires them; minutes are always the terminal unit. Machine-neutral units
 * (matches the app's d/h/m convention), independent of locale.
 */
fun formatDurationFull(duration: Duration): String {
	val totalMinutes = duration.inWholeMinutes.coerceAtLeast(0)
	val days = totalMinutes / 1440
	val hours = (totalMinutes % 1440) / 60
	val minutes = totalMinutes % 60
	return when {
		days > 0 -> "${days}d ${hours}h ${minutes}m"
		hours > 0 -> "${hours}h ${minutes}m"
		else -> "${minutes}m"
	}
}
