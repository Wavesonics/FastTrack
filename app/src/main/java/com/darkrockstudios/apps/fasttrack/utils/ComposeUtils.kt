package com.darkrockstudios.apps.fasttrack.utils

import android.os.Vibrator
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import java.time.LocalDate as JavaLocalDate

@Composable
fun combinePadding(padding1: PaddingValues, padding2: PaddingValues): PaddingValues {
	val layoutDirection = LocalLayoutDirection.current

	return PaddingValues(
		start = padding1.calculateStartPadding(layoutDirection) + padding2.calculateStartPadding(layoutDirection),
		top = padding1.calculateTopPadding() + padding2.calculateTopPadding(),
		end = padding1.calculateEndPadding(layoutDirection) + padding2.calculateEndPadding(layoutDirection),
		bottom = padding1.calculateBottomPadding() + padding2.calculateBottomPadding()
	)
}

/**
 * Custom implementation of SelectableDates that only allows dates before now (including today)
 */
class PastAndTodaySelectableDates : SelectableDates {
	override fun isSelectableDate(utcTimeMillis: Long): Boolean {
		val today = JavaLocalDate.now()
		val date = JavaLocalDate.ofEpochDay(utcTimeMillis / (24 * 60 * 60 * 1000))
		return !date.isAfter(today)
	}
}

/**
 * Custom implementation of SelectableDates that only allows dates between a minimum date and today
 */
class DateRangeSelectableDates(private val minDateMillis: Long?) : SelectableDates {
	override fun isSelectableDate(utcTimeMillis: Long): Boolean {
		val today = JavaLocalDate.now()
		val date = JavaLocalDate.ofEpochDay(utcTimeMillis / (24 * 60 * 60 * 1000))

		// Date must not be after today
		if (date.isAfter(today)) return false

		// If minDateMillis is specified, date must not be before it
		if (minDateMillis != null) {
			val minDate = JavaLocalDate.ofEpochDay(minDateMillis / (24 * 60 * 60 * 1000))
			if (date.isBefore(minDate)) return false
		}

		return true
	}
}

val MAX_COLUMN_WIDTH = 600.dp

@Composable
fun rememberVibrator(): Vibrator? {
	val context = LocalContext.current
	return remember { context.getSystemService<Vibrator>() }
}