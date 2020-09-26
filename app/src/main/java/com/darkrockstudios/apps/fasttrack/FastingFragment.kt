package com.darkrockstudios.apps.fasttrack

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.log4k.d
import com.log4k.i
import com.log4k.w
import kotlinx.android.synthetic.main.fragment_fasting.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*
import kotlin.time.ExperimentalTime


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@ExperimentalTime
class FastingFragment: Fragment()
{
	private val uiHandler = Handler(Looper.getMainLooper())

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		//val dataStore: DataStore<Fast> = requireContext().createDataStore(fileName = "currentFast")
	}

	override fun onCreateView(
			inflater: LayoutInflater, container: ViewGroup?,
			savedInstanceState: Bundle?
							 ): View?
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_fasting, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		button_start_fast.setOnClickListener {
			startFast()
		}

		button_end_fast.setOnClickListener {
			context?.let { ctx ->
				MaterialAlertDialogBuilder(ctx)
						.setTitle(R.string.confirm_end_fast_title)
						.setPositiveButton(R.string.confirm_end_fast_positive) { _, _ -> endFast() }
						.setNegativeButton(R.string.confirm_end_fast_negative, null)
						.show()
			}
		}

		button_start_fast_advanced.setOnClickListener {
			showStartPicker()
		}
	}

	override fun onStart()
	{
		super.onStart()

		startTimerUpdate()
	}

	override fun onResume()
	{
		super.onResume()

		updateUi()
	}

	override fun onStop()
	{
		super.onStop()

		stopTimerUpdate()
	}

	private val updater = Runnable {
		updateUi()
		if(isFasting())
		{
			startTimerUpdate()
		}
	}

	private fun showStartPicker()
	{
		SingleDateAndTimePickerDialog.Builder(context)
				.bottomSheet()
				.curved()
				.minutesStep(15)
				.title("Fasted Started")
				.listener(::onDateTimePicked).display()
	}

	private fun onDateTimePicked(date: Date)
	{
		startFast(date.time)
	}

	private fun startTimerUpdate()
	{
		uiHandler.postDelayed(updater, 1000)
	}

	private fun stopTimerUpdate()
	{
		uiHandler.removeCallbacks(updater)
	}

	private fun updateUi()
	{
		updateButtons()
		updateTimer()
		updateStage()

		textview_title.text = if(isFasting()) getString(R.string.fast_status_fasting) else getString(R.string.fast_status_not_fasting)
	}

	private fun updateStage()
	{
		val fastStart = getFastStart()
		textview_stage.isVisible = (fastStart != null)
		textview_stage.text = ""
		textview_stage_description.text = ""
		textview_next_stage.text = ""

		if(isFasting() && fastStart != null)
		{
			val elapsedHours = Clock.System.now().minus(fastStart).inHours.toInt()

			var stageIndex = Stages.stage.indexOfLast { it.hours <= elapsedHours }
			if(stageIndex < 0)
			{
				stageIndex = 0
			}

			val stage = Stages.stage[stageIndex]

			textview_stage.text = stage.title
			textview_stage_description.text = stage.description

			if(stageIndex + 1 < Stages.stage.size)
			{
				val nextStage = Stages.stage[stageIndex + 1]
				textview_next_stage.text = getString(R.string.next_stage, nextStage.hours)
			}
		}
	}

	@ExperimentalTime
	private fun updateTimer()
	{
		//debug()

		val fastStart = getFastStart()
		val fastEnd = getFastEnd()

		if(fastStart != null)
		{
			if(fastEnd == null)
			{
				val elapsedTime = Clock.System.now().minus(fastStart)
				elapsedTime.toComponents(::updateTimerView)
			}
			else
			{
				val elapsedTime = fastEnd.minus(fastStart)
				elapsedTime.toComponents(::updateTimerView)
			}
		}
		else
		{
			chronometer.text = "0:00:00"
		}
	}

	private fun updateTimerView(hours: Int, minutes: Int, seconds: Int, nanoseconds: Int)
	{
		val secondsStr = "%02d".format(seconds)
		val minutesStr = "%02d".format(minutes)
		chronometer.text = "$hours:$minutesStr:$secondsStr"
	}

	private fun updateButtons()
	{
		val isFasting = isFasting()

		button_start_fast.isVisible = !isFasting
		button_start_fast_advanced.isVisible = !isFasting
		button_end_fast.isVisible = isFasting
	}

	private val storage by lazy { requireActivity().getPreferences(Context.MODE_PRIVATE) }

	private fun debug()
	{
		d("Fast Start: ${getFastStart()}")
		d("Fast End: ${getFastEnd()}")
	}

	private fun isFasting(): Boolean
	{
		val fastStart = storage.getLong(Data.KEY_FAST_START, -1)
		val fastEnd = storage.getLong(Data.KEY_FAST_END, -1)
		return fastStart != -1L && fastEnd == -1L
	}

	private fun getFastStart(): Instant?
	{
		val mills = storage.getLong(Data.KEY_FAST_START, -1)
		return if(mills > -1)
		{
			Instant.fromEpochMilliseconds(mills)
		}
		else
		{
			null
		}
	}

	private fun getFastEnd(): Instant?
	{
		val mills = storage.getLong(Data.KEY_FAST_END, -1)
		return if(mills > -1)
		{
			Instant.fromEpochMilliseconds(mills)
		}
		else
		{
			null
		}
	}

	private fun startFast(timeStartedMills: Long? = null)
	{
		if(!isFasting())
		{
			val mills = if(timeStartedMills == null)
			{
				val now = Clock.System.now()
				now.toEpochMilliseconds()
			}
			else
			{
				timeStartedMills
			}
			storage.edit { putLong(Data.KEY_FAST_START, mills) }
			storage.edit { putLong(Data.KEY_FAST_END, -1) }

			updateUi()
			startTimerUpdate()

			i("Started fast!")
		}
		else
		{
			w("Cannot start fast with one in progress")
		}
	}

	private fun endFast()
	{
		if(isFasting())
		{
			val now = Clock.System.now()
			val mills = now.toEpochMilliseconds()
			storage.edit { putLong(Data.KEY_FAST_END, mills) }
			i("Fast ended!")

			updateUi()
		}
		else
		{
			w("Cannot end fast, there is none started")
		}
	}
}