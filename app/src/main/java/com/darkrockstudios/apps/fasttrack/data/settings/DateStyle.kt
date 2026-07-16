package com.darkrockstudios.apps.fasttrack.data.settings

/**
 * How dates and times are presented throughout the app. Each style is locale-aware
 * (field order and hour cycle follow the user's locale / system 24-hour setting);
 * only the level of detail differs. See [com.darkrockstudios.apps.fasttrack.utils.AppDateTime].
 */
enum class DateStyle {
	/** Tightest one-line form: no weekday, current year omitted, minutes dropped at :00 (12h). */
	OPTIMIZED_COMPACT,

	/** Compact, but keeps the short weekday. */
	OPTIMIZED_WEEKDAY,

	/** The OS locale's short date + short time. */
	SYSTEM_SHORT,

	/** The OS locale's medium date + short time. */
	SYSTEM_MEDIUM,

	/** The OS locale's long date + short time. */
	SYSTEM_LONG,

	/** Year-first, unambiguous, sortable (2025-06-01 20:05). */
	ISO;

	companion object {
		fun fromName(name: String?): DateStyle =
			entries.firstOrNull { it.name == name } ?: OPTIMIZED_COMPACT
	}
}
