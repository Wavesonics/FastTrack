package com.darkrockstudios.apps.fasttrack.utils

import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.format.DateTimeFormatter
import java.util.*

fun Instant.formatAs(
	pattern: String,
	zone: TimeZone = TimeZone.currentSystemDefault(),
	locale: Locale = Locale.getDefault()
): String {
	val ldt = this.toLocalDateTime(zone)
	val formatter = DateTimeFormatter.ofPattern(pattern, locale)
	return ldt.toJavaLocalDateTime().format(formatter)
}

fun Instant.utcToLocal(): Instant =
	toLocalDateTime(TimeZone.UTC).toInstant(TimeZone.currentSystemDefault())