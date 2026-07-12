package com.darkrockstudios.apps.fasttrack.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant


fun Instant.formatAs(
	pattern: String,
	zone: TimeZone = TimeZone.currentSystemDefault(),
	locale: Locale = Locale.getDefault()
): String {
	val ldt = this.toLocalDateTime(zone)
	val formatter = DateTimeFormatter.ofPattern(pattern, locale)
	return ldt.toJavaLocalDateTime().format(formatter)
}

fun LocalDateTime.formatAs(
	pattern: String,
	locale: Locale = Locale.getDefault()
): String {
	val formatter = DateTimeFormatter.ofPattern(pattern, locale)
	return this.toJavaLocalDateTime().format(formatter)
}

fun Instant.utcToLocal(): Instant =
	toLocalDateTime(TimeZone.UTC).toInstant(TimeZone.currentSystemDefault())
