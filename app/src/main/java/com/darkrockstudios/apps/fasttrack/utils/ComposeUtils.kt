package com.darkrockstudios.apps.fasttrack.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
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