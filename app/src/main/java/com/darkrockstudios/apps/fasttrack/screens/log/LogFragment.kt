package com.darkrockstudios.apps.fasttrack.screens.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import com.darkrockstudios.apps.fasttrack.databinding.LogFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LogFragment: Fragment()
{
	companion object
	{
		fun newInstance() = LogFragment()
	}

	private val itemAdapter = ItemAdapter<FastEntryItem>()
	private val fastAdapter = FastAdapter.with(itemAdapter)
	private val database by inject<AppDatabase>()
	private val viewModel by viewModels<LogViewModel>()

	private lateinit var binding: LogFragmentBinding

	override fun onCreateView(
			inflater: LayoutInflater, container: ViewGroup?,
			savedInstanceState: Bundle?): View {
		binding = LogFragmentBinding.inflate(inflater, container, false)
		return binding.root
	}

	@ExperimentalTime
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		binding.logFabManualAdd.setOnClickListener {
			ManualAddFragment.newInstance().show(childFragmentManager, "manual_add")
		}

		binding.logEntries.adapter = fastAdapter
		fastAdapter.onLongClickListener = { _, _, item, _ ->
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
		lifecycleScope.launch(Dispatchers.IO) { database.fastDao().delete(item.fast) }
	}

	private fun updateEntries(entries: List<FastEntry>)
	{
		itemAdapter.clear()
		val items = entries.sortedByDescending { it.start }.map { FastEntryItem(it) }
		itemAdapter.add(items)
		fastAdapter.notifyAdapterDataSetChanged()

		val totalKetosisHours = entries.sumByDouble { it.calculateKetosis() }
		val totalAutophagyHours = entries.sumByDouble { it.calculateAutophagy() }

		binding.totalKetosisValue.text = getString(R.string.log_total_hours, totalKetosisHours.roundToInt())
		binding.totalAutophagyValue.text = getString(R.string.log_total_hours, totalAutophagyHours.roundToInt())
	}
}