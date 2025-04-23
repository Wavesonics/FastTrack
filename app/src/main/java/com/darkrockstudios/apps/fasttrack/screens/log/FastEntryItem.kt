package com.darkrockstudios.apps.fasttrack.screens.log

import android.view.LayoutInflater
import android.view.ViewGroup
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import com.darkrockstudios.apps.fasttrack.databinding.FastEntryItemBinding
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime


class FastEntryItem(val fast: FastEntry): AbstractBindingItem<FastEntryItemBinding>()
{
	override val type: Int = R.id.fast_entry_item

	@ExperimentalTime
	override fun bindView(binding: FastEntryItemBinding, payloads: List<Any>)
	{
		val ctx = binding.fastEntryStart.context

		val start = Instant.fromEpochMilliseconds(fast.start)
		val hours = fast.length.milliseconds.inWholeHours
		val ketosisHours = fast.calculateKetosis().roundToInt()
		val autophagyHours = fast.calculateAutophagy().roundToInt()

		val startDate = start.toLocalDateTime(TimeZone.currentSystemDefault())
		val pattern = DateTimeFormatter.ofPattern("d MMM uuuu - HH:mm")
		val dateStr = startDate.toJavaLocalDateTime().format(pattern)

		binding.fastEntryStart.text = ctx.getString(R.string.log_entry_started, dateStr)
		binding.fastEntryLength.text = ctx.getString(R.string.log_entry_length, hours)
		binding.fastEntryKetosis.text = ctx.getString(R.string.log_entry_ketosis, ketosisHours)
		binding.fastEntryAutophagy.text = ctx.getString(R.string.log_entry_autophagy, autophagyHours)
	}

	override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?) = FastEntryItemBinding.inflate(inflater, parent, false)
}