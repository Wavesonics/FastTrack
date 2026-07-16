package com.darkrockstudios.apps.fasttrack.utils

import android.text.format.DateFormat
import androidx.compose.runtime.staticCompositionLocalOf
import com.darkrockstudios.apps.fasttrack.data.settings.DateStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * The active [DateStyle] for the current composition. Provided once near the app
 * root and refreshed when the user changes it in Settings — no OS-locale listeners,
 * and every UI date/time reads it here.
 */
val LocalDateStyle = staticCompositionLocalOf { DateStyle.OPTIMIZED_COMPACT }

/**
 * One place that turns instants into user-facing text. Everything is locale-aware:
 * field order comes from the locale (via CLDR skeletons / localized formats), not a
 * hardcoded pattern, so "Jun 1" (en-US), "1 Jun" (en-GB) and "6月1日" (zh) all fall
 * out correctly. The [is24Hour] flag comes from the user's system setting.
 *
 * Formatters are immutable and expensive to build, so they are cached and reused.
 * Machine surfaces (CSV / iCal / JSON export) do NOT use this — they stay ISO.
 */
object AppDateTime {

	private val formatterCache = ConcurrentHashMap<String, DateTimeFormatter>()

	private fun skeletonFormatter(locale: Locale, skeleton: String): DateTimeFormatter =
		formatterCache.getOrPut("s|${locale.toLanguageTag()}|$skeleton") {
			DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
		}

	private fun patternFormatter(locale: Locale, pattern: String): DateTimeFormatter =
		formatterCache.getOrPut("p|${locale.toLanguageTag()}|$pattern") {
			DateTimeFormatter.ofPattern(pattern, locale)
		}

	private fun localizedDateFormatter(locale: Locale, style: FormatStyle): DateTimeFormatter =
		formatterCache.getOrPut("d|${locale.toLanguageTag()}|$style") {
			DateTimeFormatter.ofLocalizedDate(style).withLocale(locale)
		}

	private fun currentYear(): Int = YearMonth.now().year

	private fun dateFormatter(style: DateStyle, year: Int, locale: Locale): DateTimeFormatter =
		when (style) {
			DateStyle.OPTIMIZED_COMPACT ->
				skeletonFormatter(locale, if (year == currentYear()) "MMMd" else "yMMMd")

			DateStyle.OPTIMIZED_WEEKDAY ->
				skeletonFormatter(locale, if (year == currentYear()) "MMMEd" else "yMMMEd")

			DateStyle.SYSTEM_SHORT -> localizedDateFormatter(locale, FormatStyle.SHORT)
			DateStyle.SYSTEM_MEDIUM -> localizedDateFormatter(locale, FormatStyle.MEDIUM)
			DateStyle.SYSTEM_LONG -> localizedDateFormatter(locale, FormatStyle.LONG)
			DateStyle.ISO -> patternFormatter(locale, "uuuu-MM-dd")
		}

	private fun timeFormatter(style: DateStyle, minute: Int, is24Hour: Boolean, locale: Locale): DateTimeFormatter {
		val skeleton = when {
			style == DateStyle.ISO -> return patternFormatter(locale, "HH:mm")
			is24Hour -> "Hm"
			// Optimized styles drop ":00" (12h only; "20" alone would be ambiguous).
			(style == DateStyle.OPTIMIZED_COMPACT || style == DateStyle.OPTIMIZED_WEEKDAY) && minute == 0 -> "h"
			else -> "hm"
		}
		return skeletonFormatter(locale, skeleton)
	}

	private fun datePart(dt: LocalDateTime, style: DateStyle, locale: Locale): String =
		dt.toJavaLocalDateTime().format(dateFormatter(style, dt.year, locale))

	private fun timePart(dt: LocalDateTime, style: DateStyle, is24Hour: Boolean, locale: Locale): String =
		dt.toJavaLocalDateTime().format(timeFormatter(style, dt.minute, is24Hour, locale))

	/** Date only (respects the style; Optimized omits the current year). */
	fun formatDate(dt: LocalDateTime, style: DateStyle, locale: Locale = Locale.getDefault()): String =
		datePart(dt, style, locale)

	/** Date only, from a calendar date. */
	fun formatDate(date: LocalDate, style: DateStyle, locale: Locale = Locale.getDefault()): String =
		date.toJavaLocalDate().format(dateFormatter(style, date.year, locale))

	/** Time only. */
	fun formatTime(dt: LocalDateTime, style: DateStyle, is24Hour: Boolean, locale: Locale = Locale.getDefault()): String =
		timePart(dt, style, is24Hour, locale)

	/** A single date + time, e.g. for a settings sample. */
	fun formatDateTime(
		dt: LocalDateTime,
		style: DateStyle,
		is24Hour: Boolean,
		locale: Locale = Locale.getDefault(),
	): String {
		val date = datePart(dt, style, locale)
		val time = timePart(dt, style, is24Hour, locale)
		return if (style == DateStyle.ISO) "$date $time" else "$date, $time"
	}

	/** Localized month + year for a calendar header, e.g. "June 2025" / "2025年6月". */
	fun formatMonthYear(yearMonth: YearMonth, locale: Locale = Locale.getDefault()): String =
		yearMonth.format(skeletonFormatter(locale, "yMMMM"))

	/**
	 * A fast's window as one heading. Same-day fasts show the date once and the times
	 * as a range; multi-day fasts date both ends. ISO stays fully explicit on each end.
	 */
	fun formatFastRange(
		start: LocalDateTime,
		end: LocalDateTime,
		style: DateStyle,
		is24Hour: Boolean,
		locale: Locale = Locale.getDefault(),
	): String {
		if (style == DateStyle.ISO) {
			return "${formatDateTime(start, style, is24Hour, locale)} → ${formatDateTime(end, style, is24Hour, locale)}"
		}
		val startDate = datePart(start, style, locale)
		val startTime = timePart(start, style, is24Hour, locale)
		val endTime = timePart(end, style, is24Hour, locale)
		return if (start.date == end.date) {
			"$startDate · $startTime – $endTime"
		} else {
			"$startDate, $startTime → ${datePart(end, style, locale)}, $endTime"
		}
	}
}
