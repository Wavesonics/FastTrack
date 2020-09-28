package com.darkrockstudios.apps.fasttrack.screens.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.android.synthetic.main.log_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LogFragment: Fragment()
{
	private val itemAdapter = ItemAdapter<FastEntryItem>()
	private val fastAdapter = FastAdapter.with(itemAdapter)
	private val database by inject<AppDatabase>()
	private val viewModel by viewModels<LogViewModel>()

	override fun onCreateView(
			inflater: LayoutInflater, container: ViewGroup?,
			savedInstanceState: Bundle?): View?
	{
		return inflater.inflate(R.layout.log_fragment, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		log_entries.adapter = fastAdapter
		fastAdapter.onClickListener = { _, _, item, _ ->
			context?.let { ctx ->
				MaterialAlertDialogBuilder(ctx)
						.setTitle(R.string.confirm_delete_fast_title)
						.setPositiveButton(R.string.confirm_delete_fast_positive) { _, _ -> deleteFast(item) }
						.setNegativeButton(R.string.confirm_delete_fast_negative, null)
						.show()
			}

			true
		}

		database.fastDao().loadAll().observe(viewLifecycleOwner, ::updateEntries)
	}

	private fun deleteFast(item: FastEntryItem)
	{
		GlobalScope.launch { database.fastDao().delete(item.fast) }
	}

	private fun updateEntries(entries: List<FastEntry>)
	{
		itemAdapter.clear()
		val items = entries.sortedByDescending { it.start }.map { FastEntryItem(it) }
		itemAdapter.add(items)
		fastAdapter.notifyAdapterDataSetChanged()
	}
}