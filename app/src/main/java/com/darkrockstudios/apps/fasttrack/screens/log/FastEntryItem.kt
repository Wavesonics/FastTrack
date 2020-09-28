package com.darkrockstudios.apps.fasttrack.screens.log

import android.view.LayoutInflater
import android.view.ViewGroup
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import com.darkrockstudios.apps.fasttrack.databinding.FastEntryItemBinding
import com.mikepenz.fastadapter.binding.AbstractBindingItem


class FastEntryItem(val fast: FastEntry): AbstractBindingItem<FastEntryItemBinding>()
{
	override val type: Int = R.id.fast_entry_item

	override fun bindView(binding: FastEntryItemBinding, payloads: List<Any>)
	{
		binding.fastEntryStart.text = "${fast.start}"
		binding.fastEntryLength.text = "${fast.length}"
	}

	override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): FastEntryItemBinding = FastEntryItemBinding.inflate(inflater, parent, false)
}