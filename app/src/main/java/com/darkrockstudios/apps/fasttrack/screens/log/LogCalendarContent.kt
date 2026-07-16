package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import com.darkrockstudios.apps.fasttrack.utils.gaugeColors
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate as KxLocalDate

@ExperimentalTime
@Composable
fun LogCalendarContent(
    entries: List<FastingLogEntry>,
	selectedDate: KxLocalDate?,
	onDateSelected: (KxLocalDate?) -> Unit,
	onAddForEmptyDay: (KxLocalDate) -> Unit,
    onEdit: (FastingLogEntry) -> Unit,
    onDelete: (FastingLogEntry) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
	// A fast spans every calendar day from its start day through the last day it
	// was still active, so a multi-day fast highlights the whole range (not just
	// its start day). This maps each covered day to the fasts active that day.
	val coverage = remember(entries) {
		val tz = TimeZone.currentSystemDefault()
		val byDay = HashMap<KxLocalDate, MutableList<FastingLogEntry>>()
		val endDateById = HashMap<Int, KxLocalDate>()
		for (e in entries) {
			val startDate = e.start.date
			val endInstant = e.start.toInstant(tz).plus(e.length)
			// Last day the fast was actually active (a fast ending at 00:00 does
			// not claim that day), never earlier than the start day.
			val endDate = maxOf(startDate, (endInstant - 1.milliseconds).toLocalDateTime(tz).date)
			endDateById[e.id] = endDate
			var d = startDate
			while (d <= endDate) {
				byDay.getOrPut(d) { mutableListOf() }.add(e)
				d = d.plus(1, DateTimeUnit.DAY)
			}
		}
		CalendarCoverage(byDay, endDateById)
	}

	val today = remember { java.time.LocalDate.now() }
	val currentMonth = remember { YearMonth.now() }
	val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
	val daysOfWeek = remember(firstDayOfWeek) { daysOfWeek(firstDayOfWeek = firstDayOfWeek) }

	val calendarState = rememberCalendarState(
		startMonth = currentMonth.minusMonths(24),
		endMonth = currentMonth,
		firstVisibleMonth = currentMonth,
		firstDayOfWeek = firstDayOfWeek,
	)

	LazyColumn(
		modifier = modifier.fillMaxSize(),
		contentPadding = contentPadding,
	) {
		item {
			DaysOfWeekRow(daysOfWeek)
		}
		item {
			HorizontalCalendar(
				state = calendarState,
				dayContent = { day ->
					val kxDate = day.date.toKotlinLocalDate()
					val covering = coverage.byDay[kxDate].orEmpty()
					// Pick the longest fast covering this day (handles the rare
					// overlap) and describe where the day sits in that fast's range.
					val band = covering.maxByOrNull { it.length }?.let { chosen ->
						val startDate = chosen.start.date
						val endDate = coverage.endDateById[chosen.id] ?: startDate
						DayBand(
							color = stageColorFor(listOf(chosen)),
							isStart = kxDate == startDate,
							isEnd = kxDate == endDate,
							isSingle = startDate == endDate,
						)
					}
					DayCell(
						day = day,
						isToday = day.date == today,
						isFuture = day.date.isAfter(today),
						band = band,
						isSelected = selectedDate == kxDate,
						onClick = {
							if (covering.isNotEmpty()) {
								// A day within a fast opens its detail dialog.
								onDateSelected(kxDate)
							} else {
								// An empty past/today day offers to log a fast there
								// (future days are disabled, so this never fires for them).
								onAddForEmptyDay(kxDate)
							}
						},
					)
				},
				monthHeader = { month -> MonthHeader(month) },
			)
		}
	}

	val selected = selectedDate
	val selectedEntries = if (selected != null) coverage.byDay[selected].orEmpty() else emptyList()
	if (selected != null && selectedEntries.isNotEmpty()) {
		FastDayDialog(
			date = selected,
			entries = selectedEntries,
			onDismiss = { onDateSelected(null) },
			onEdit = onEdit,
			onDelete = onDelete,
		)
	}
}

@ExperimentalTime
@Composable
private fun FastDayDialog(
	date: KxLocalDate,
    entries: List<FastingLogEntry>,
    onDismiss: () -> Unit,
    onEdit: (FastingLogEntry) -> Unit,
    onDelete: (FastingLogEntry) -> Unit,
) {
	val formatter = remember { DateTimeFormatter.ofPattern("EEE, d MMM uuuu", Locale.getDefault()) }
	val dateLabel = remember(date) { date.toJavaLocalDate().format(formatter) }

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(
			dismissOnBackPress = true,
			dismissOnClickOutside = true,
		),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 8.dp),
		) {
			Text(
				text = dateLabel,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.onSurface,
				textAlign = TextAlign.Center,
				modifier = Modifier
					.fillMaxWidth()
					.padding(bottom = 8.dp),
			)
			entries.forEach { entry ->
				FastEntryItem(
					entry = entry,
					onEdit = {
						onEdit(entry)
						onDismiss()
					},
					onDelete = {
						onDelete(entry)
						onDismiss()
					},
				)
			}
		}
	}
}

@Composable
private fun DaysOfWeekRow(daysOfWeek: List<java.time.DayOfWeek>) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 4.dp)
	) {
		daysOfWeek.forEach { dow ->
			Text(
				text = dow.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale),
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				textAlign = TextAlign.Center,
				modifier = Modifier.weight(1f)
			)
		}
	}
}

@Composable
private fun MonthHeader(month: CalendarMonth) {
	val formatter = remember { DateTimeFormatter.ofPattern("MMMM uuuu", Locale.getDefault()) }
	Text(
		text = month.yearMonth.format(formatter),
		style = MaterialTheme.typography.titleMedium,
		color = MaterialTheme.colorScheme.onSurface,
		fontWeight = FontWeight.SemiBold,
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp),
		textAlign = TextAlign.Center
	)
}

@Composable
private fun DayCell(
	day: CalendarDay,
	isToday: Boolean,
	isFuture: Boolean,
	band: DayBand?,
	isSelected: Boolean,
	onClick: () -> Unit,
) {
	val inMonth = day.position == DayPosition.MonthDate
	// Only past/today fasts can be logged, so future days are greyed out and inert.
	val enabled = inMonth && !isFuture
	val cover = if (inMonth) band else null

	val stageColor = cover?.color ?: Color.Transparent
	// Middle days are a light connecting bar; the true start/end are stronger circles.
	val barColor = stageColor.copy(alpha = 0.22f)
	val isEndpoint = cover != null && (cover.isStart || cover.isEnd || cover.isSingle)
	val endpointFill = if (isEndpoint) stageColor.copy(alpha = 0.45f) else Color.Transparent

	val borderColor = when {
		isSelected -> MaterialTheme.colorScheme.primary
		isToday && inMonth -> MaterialTheme.colorScheme.outline
		else -> Color.Transparent
	}
	val borderWidth = if (isSelected) 2.dp else 1.dp

	val dayTextColor = when {
		!inMonth || isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
		else -> MaterialTheme.colorScheme.onSurface
	}

	Box(
		modifier = Modifier
			.aspectRatio(1f)
			.clickable(enabled = enabled, onClick = onClick),
		contentAlignment = Alignment.Center,
	) {
		// Connecting band, drawn edge-to-edge behind the day token so it joins the
		// neighbouring cells into one continuous bar. Each side is filled only when
		// the range continues that way; at week wraps this yields a clean flat edge.
		if (cover != null && !cover.isSingle) {
			if (!cover.isStart) {
				Box(
					modifier = Modifier
						.align(Alignment.CenterStart)
						.fillMaxWidth(0.5f)
						.fillMaxHeight(0.72f)
						.background(barColor),
				)
			}
			if (!cover.isEnd) {
				Box(
					modifier = Modifier
						.align(Alignment.CenterEnd)
						.fillMaxWidth(0.5f)
						.fillMaxHeight(0.72f)
						.background(barColor),
				)
			}
		}

		// The day token itself: a filled circle at range endpoints (and single-day
		// fasts), plus the today/selected ring, with the date number on top.
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(2.dp)
				.clip(CircleShape)
				.background(endpointFill, CircleShape)
				.border(borderWidth, borderColor, CircleShape),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = day.date.dayOfMonth.toString(),
				style = MaterialTheme.typography.bodyMedium,
				color = dayTextColor,
				fontWeight = if (isToday && inMonth) FontWeight.Bold else FontWeight.Normal,
			)
		}
	}
}

/** Per-day coverage precomputed for the visible entries. */
private class CalendarCoverage(
	val byDay: Map<KxLocalDate, List<FastingLogEntry>>,
	val endDateById: Map<Int, KxLocalDate>,
)

/** Where a given day sits within the fast that covers it. */
private data class DayBand(
	val color: Color,
	val isStart: Boolean,
	val isEnd: Boolean,
	val isSingle: Boolean,
)

@ExperimentalTime
private fun stageColorFor(entries: List<FastingLogEntry>): Color {
	val longest = entries.maxByOrNull { it.length } ?: return Color.Transparent
	val lenHours = longest.length.toDouble(DurationUnit.HOURS)
	val stage = Stages.phases.lastOrNull { lenHours >= it.hours } ?: Stages.phases.first()
	val stageIndex = Stages.phases.indexOf(stage).coerceAtLeast(0)
	return gaugeColors.getOrElse(stageIndex) { Color.Transparent }
}
