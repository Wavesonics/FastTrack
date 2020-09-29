package com.darkrockstudios.apps.fasttrack.screens.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import com.log4k.w
import kotlinx.android.synthetic.main.manual_add_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.android.ext.android.inject

class ManualAddFragment: DialogFragment()
{
	companion object
	{
		fun newInstance() = ManualAddFragment()
	}

	private val viewModel by viewModels<ManualAddViewModel>()
	private val database by inject<AppDatabase>()

	override fun onCreateView(
			inflater: LayoutInflater, container: ViewGroup?,
			savedInstanceState: Bundle?): View?
	{
		return inflater.inflate(R.layout.manual_add_fragment, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		manual_add_close_button.setOnClickListener {
			dismissAllowingStateLoss()
		}

		manual_add_date.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
			viewModel.startDate = LocalDate(year, monthOfYear, dayOfMonth)
			updateUi()
		}

		manual_add_time.setOnTimeChangedListener { _, hourOfDay, minute ->
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

		textInput_manual_add_length.addTextChangedListener { editable ->
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

		manual_add_button_complete.setOnClickListener {
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
				manual_add_date.isVisible = true
				manual_add_time.isVisible = false
				manual_add_button_complete.isEnabled = false
			}
			viewModel.startDateTime == null ->
			{
				manual_add_date.isVisible = false
				manual_add_time.isVisible = true
				manual_add_button_complete.isEnabled = false
			}
			viewModel.length == null        ->
			{
				manual_add_date.isVisible = false
				manual_add_time.isVisible = true
				manual_add_button_complete.isEnabled = false
			}
			else                            ->
			{
				manual_add_date.isVisible = false
				manual_add_time.isVisible = true
				manual_add_button_complete.isEnabled = true
			}
		}
	}

	private fun addEntry()
	{
		val startDateTime = viewModel.startDateTime
		val length = viewModel.length
		if(startDateTime != null && length != null && length > 0L)
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