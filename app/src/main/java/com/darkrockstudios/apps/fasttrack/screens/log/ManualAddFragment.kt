package com.darkrockstudios.apps.fasttrack.screens.log

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import com.darkrockstudios.apps.fasttrack.databinding.ManualAddFragmentBinding
import io.github.aakira.napier.Napier.w
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ManualAddFragment: DialogFragment()
{
	companion object
	{
		fun newInstance() = ManualAddFragment()
	}

	private val uiHandler = Handler(Looper.getMainLooper())
	private val viewModel by viewModels<ManualAddViewModel>()
	private val database by inject<AppDatabase>()

	private lateinit var binding: ManualAddFragmentBinding

	override fun onCreateView(
			inflater: LayoutInflater,
			container: ViewGroup?,
			savedInstanceState: Bundle?): View {
		binding = ManualAddFragmentBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		binding.manualAddCloseButton.setOnClickListener {
			dismissAllowingStateLoss()
		}

		binding.manualAddDate.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
			viewModel.startDate = LocalDate(year, monthOfYear + 1, dayOfMonth)
			// Delay so that it is not jarring to the user
			uiHandler.postDelayed({ updateUi() }, 500)
		}

		binding.manualAddTime.setOnTimeChangedListener { _, hourOfDay, minute ->
			viewModel.startDate?.let { startDate ->
				viewModel.startDateTime = LocalDateTime(
						year = startDate.year,
						month = startDate.month,
						dayOfMonth = startDate.dayOfMonth,
						hour = hourOfDay,
						minute = minute,
						nanosecond = 0)

				updateUi()
			}
		}

		binding.textInputManualAddLength.addTextChangedListener { editable ->
			if(editable != null)
			{
				val text = editable.toString()
				try
				{
					viewModel.length = text.toLong()
				}
				catch(e: NumberFormatException)
				{
					w("Failed to parse length input")
				}
			}

			updateUi()
		}

		binding.manualAddButtonComplete.setOnClickListener {
			addEntry()
		}
	}

	override fun onResume()
	{
		super.onResume()

		updateUi()
	}

	private fun updateUi()
	{
		when
		{
			viewModel.startDate == null     ->
			{
				binding.manualAddDate.isVisible = true
				binding.manualAddTime.isVisible = false
				binding.manualAddButtonComplete.isEnabled = false
			}
			viewModel.startDateTime == null ->
			{
				binding.manualAddDate.isVisible = false
				binding.manualAddTime.isVisible = true
				binding.manualAddButtonComplete.isEnabled = false
			}
			viewModel.length == null        ->
			{
				binding.manualAddDate.isVisible = false
				binding.manualAddTime.isVisible = true
				binding.manualAddButtonComplete.isEnabled = false
			}
			else                            ->
			{
				binding.manualAddDate.isVisible = false
				binding.manualAddTime.isVisible = true
				binding.manualAddButtonComplete.isEnabled = true
			}
		}
	}

	private fun addEntry()
	{
		val startDateTime = viewModel.startDateTime
		val length = viewModel.length?.hours?.inWholeMilliseconds ?: 0L
		if(startDateTime != null && length > 0)
		{
			GlobalScope.launch(Dispatchers.IO) {
				val tz = TimeZone.currentSystemDefault()
				val startInstant = startDateTime.toInstant(tz)
				val startMills = startInstant.toEpochMilliseconds()

				val newEntry = FastEntry(start = startMills, length = length)
				database.fastDao().insertAll(newEntry)

				withContext(Dispatchers.Main) {
					dismissAllowingStateLoss()
				}
			}
		}
	}
}