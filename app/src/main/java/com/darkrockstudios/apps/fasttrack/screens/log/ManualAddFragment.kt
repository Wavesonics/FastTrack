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
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import com.log4k.w
import kotlinx.android.synthetic.main.manual_add_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import org.koin.android.ext.android.inject
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime
import kotlin.time.hours

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
			// Delay so that it is not jarring to the user
			uiHandler.postDelayed({ updateUi() }, 500)
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

		val startDate = viewModel.startDate
		val startDateTime = viewModel.startDateTime
		if(startDateTime != null)
		{
			val pattern = DateTimeFormatter.ofPattern("d MMM uuuu - HH:mm")
			val dateStr = startDateTime.toJavaLocalDateTime().format(pattern)
			textView_start_display.text = getString(R.string.manual_add_fast_started_on, dateStr)
		}
		else if(startDate != null)
		{
			val pattern = DateTimeFormatter.ofPattern("d MMM uuuu")
			val dateStr = startDate.toJavaLocalDate().format(pattern)
			textView_start_display.text = getString(R.string.manual_add_fast_started_on, dateStr)
		}
		else
		{
			textView_start_display.text = getString(R.string.manual_add_fast_started_on, "")
		}
	}

	private fun addEntry()
	{
		val startDateTime = viewModel.startDateTime
		val length = viewModel.length?.hours?.inMilliseconds?.toLong() ?: 0L
		if(startDateTime != null && length != null && length > 0)
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