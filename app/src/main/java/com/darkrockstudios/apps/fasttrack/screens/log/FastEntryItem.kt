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
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds


class FastEntryItem(val fast: FastEntry): AbstractBindingItem<FastEntryItemBinding>()
{
	override val type: Int = R.id.fast_entry_item

	@ExperimentalTime
	override fun bindView(binding: FastEntryItemBinding, payloads: List<Any>)
	{
		val start = Instant.fromEpochMilliseconds(fast.start)

		val hours = fast.length.milliseconds.inHours

		val startDate = start.toLocalDateTime(TimeZone.currentSystemDefault())
		val dateStr = startDate.toJavaLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

		binding.fastEntryStart.text = "Started: $dateStr"
		binding.fastEntryLength.text = "Length: %.1f hours".format(hours)
	}

	override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): FastEntryItemBinding = FastEntryItemBinding.inflate(inflater, parent, false)
}