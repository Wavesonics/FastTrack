package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate as KxLocalDate

@ExperimentalTime
@Composable
fun LogCalendarContent(
	entries: List<FastingLogEntry>,
	selectedDate: KxLocalDate?,
	onDateSelected: (KxLocalDate?) -> Unit,
	onEdit: (FastingLogEntry) -> Unit,
	onDelete: (FastingLogEntry) -> Unit,
	contentPadding: PaddingValues,
	modifier: Modifier = Modifier,
) {
	val entriesByDate = remember(entries) {
		entries.groupBy { it.start.date }
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
					val dayEntries = entriesByDate[kxDate].orEmpty()
					DayCell(
						day = day,
						isToday = day.date == today,
						entries = dayEntries,
						isSelected = selectedDate == kxDate,
						onClick = { onDateSelected(kxDate) },
					)
				},
				monthHeader = { month -> MonthHeader(month) },
			)
		}
	}

	val selected = selectedDate
	val selectedEntries = if (selected != null) entriesByDate[selected].orEmpty() else emptyList()
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
				text = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
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

@ExperimentalTime
@Composable
private fun DayCell(
	day: CalendarDay,
	isToday: Boolean,
	entries: List<FastingLogEntry>,
	isSelected: Boolean,
	onClick: () -> Unit,
) {
	val inMonth = day.position == DayPosition.MonthDate
	val hasEntries = entries.isNotEmpty() && inMonth

	val stageColor = if (hasEntries) stageColorFor(entries) else Color.Transparent
	val bgColor = if (hasEntries) stageColor.copy(alpha = 0.45f) else Color.Transparent

	val borderColor = when {
		isSelected -> MaterialTheme.colorScheme.primary
		isToday && inMonth -> MaterialTheme.colorScheme.outline
		else -> Color.Transparent
	}
	val borderWidth = if (isSelected) 2.dp else 1.dp

	val dayTextColor = when {
		!inMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
		else -> MaterialTheme.colorScheme.onSurface
	}

	Box(
		modifier = Modifier
			.aspectRatio(1f)
			.padding(2.dp)
			.clip(CircleShape)
			.background(bgColor, CircleShape)
			.border(borderWidth, borderColor, CircleShape)
			.clickable(enabled = inMonth, onClick = onClick),
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

@ExperimentalTime
private fun stageColorFor(entries: List<FastingLogEntry>): Color {
	val longest = entries.maxByOrNull { it.length } ?: return Color.Transparent
	val lenHours = longest.length.toDouble(DurationUnit.HOURS)
	val stage = Stages.phases.lastOrNull { lenHours >= it.hours } ?: Stages.phases.first()
	val stageIndex = Stages.phases.indexOf(stage).coerceAtLeast(0)
	return gaugeColors.getOrElse(stageIndex) { Color.Transparent }
}
