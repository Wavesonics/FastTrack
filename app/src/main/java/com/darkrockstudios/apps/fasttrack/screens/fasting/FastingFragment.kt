package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.darkrockstudios.apps.fasttrack.AlertService
import com.darkrockstudios.apps.fasttrack.FastUtils
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.Utils
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.Phase
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.log4k.e
import com.log4k.i
import com.log4k.w
import kotlinx.android.synthetic.main.fragment_fasting.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.hours


@ExperimentalTime
class FastingFragment: Fragment()
{
	private val uiHandler = Handler(Looper.getMainLooper())
	private val database by inject<AppDatabase>()
	private val fast by inject<FastUtils>()

	@ColorInt
	private var fatburnLabelColor: Int = Color.WHITE
	@ColorInt
	private var ketosisLabelColor: Int = Color.WHITE
	@ColorInt
	private var autophagyLabelColor: Int = Color.WHITE

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setupAlerts()
	}

	override fun onCreateView(
			inflater: LayoutInflater, container: ViewGroup?,
			savedInstanceState: Bundle?
							 ): View? = inflater.inflate(R.layout.fragment_fasting, container, false)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		fast_fab_start.setOnClickListener {
			MaterialAlertDialogBuilder(view.context)
					.setTitle(R.string.confirm_start_fast_title)
					.setPositiveButton(R.string.confirm_start_fast_positive) { _, _ -> startFast() }
					.setNeutralButton(R.string.confirm_start_fast_neutral) { _, _ -> showStartPicker() }
					.setNegativeButton(R.string.confirm_start_fast_negative, null)
					.show()
		}

		fast_fab_stop.setOnClickListener {
			context?.let { ctx ->
				MaterialAlertDialogBuilder(ctx)
						.setTitle(R.string.confirm_end_fast_title)
						.setPositiveButton(R.string.confirm_end_fast_positive) { _, _ -> endFast() }
						.setNegativeButton(R.string.confirm_end_fast_negative, null)
						.show()
			}
		}

		fast_notifications_checkbox.isChecked = storage.getBoolean(Data.KEY_FAST_ALERTS, true)
		fast_notifications_checkbox.setOnCheckedChangeListener { _, isChecked ->
			storage.edit {
				putBoolean(Data.KEY_FAST_ALERTS, isChecked)
			}
			setupAlerts()
		}

		fatburnLabelColor = textview_phase_fatburn_label.currentTextColor
		textview_phase_fatburn_label.setOnClickListener {
			Utils.showInfoDialog(R.string.info_dialog_fat_burn_title, R.string.info_dialog_fat_burn_content, requireContext())
		}

		ketosisLabelColor = textview_phase_ketosis_label.currentTextColor
		textview_phase_ketosis_label.setOnClickListener {
			Utils.showInfoDialog(R.string.info_dialog_ketosis_title, R.string.info_dialog_ketosis_content, requireContext())
		}

		autophagyLabelColor = textview_phase_autophagy_label.currentTextColor
		textview_phase_autophagy_label.setOnClickListener {
			Utils.showInfoDialog(R.string.info_dialog_autophagy_title, R.string.info_dialog_autophagy_content, requireContext())
		}
	}

	private fun setupAlerts()
	{
		val ctx = context ?: return

		val shouldAlert = storage.getBoolean(Data.KEY_FAST_ALERTS, true)

		if(fast.isFasting())
		{
			if(shouldAlert)
			{
				val elapsedTime = fast.getElapsedFastTime()
				AlertService.scheduleAlerts(elapsedTime, ctx)
			}
			// User doesn't want notifications
			else
			{
				AlertService.cancelAlerts(ctx)
			}
		}
		// No notifications if we aren't fasting
		else
		{
			AlertService.cancelAlerts(ctx)
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
		if(fast.isFasting())
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
				.title(getString(R.string.manual_start_title))
				.listener(::onDateTimePicked).display()
	}

	private fun onDateTimePicked(date: Date)
	{
		startFast(date.time)
	}

	private fun startTimerUpdate()
	{
		uiHandler.postDelayed(updater, 10)
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
	}

	private fun updateStage()
	{
		val fastStart = fast.getFastStart()
		textview_stage_title.text = ""
		textview_stage_description.text = ""
		textview_energy_mode.text = ""

		if(fast.isFasting() && fastStart != null)
		{
			val elapsedTime = Clock.System.now().minus(fastStart)
			val elapsedHours = elapsedTime.inHours.toInt()

			var stageIndex = Stages.stage.indexOfLast { it.hours <= elapsedHours }
			if(stageIndex < 0)
			{
				stageIndex = 0
			}

			val stage = Stages.stage[stageIndex]

			val curPhase = Stages.getCurrentPhase(elapsedTime)
			if(curPhase.fatBurning)
			{
				textview_energy_mode.text = getString(R.string.fasting_energy_mode, getString(R.string.fasting_energy_mode_fat))
			}
			else
			{
				textview_energy_mode.text = getString(R.string.fasting_energy_mode, getString(R.string.fasting_energy_mode_glucose))
			}

			textview_stage_title.text = getString(stage.title)
			textview_stage_description.text = getString(stage.description)
		}
	}

	@ExperimentalTime
	private fun updateTimer()
	{
		val fastStart = fast.getFastStart()
		val fastEnd = fast.getFastEnd()

		if(fastStart != null)
		{
			if(fastEnd == null)
			{
				val elapsedTime = Clock.System.now().minus(fastStart)
				elapsedTime.toComponents(::updateTimerView)
				updatePhases(elapsedTime)
			}
			else
			{
				val elapsedTime = fastEnd.minus(fastStart)
				elapsedTime.toComponents(::updateTimerView)
				updatePhases(elapsedTime)
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

		milliseconds.text = "${nanoseconds / 10000000}"
	}

	private fun updatePhases(elapsedTime: Duration)
	{
		val currentStage = Stages.getCurrentPhase(elapsedTime)

		fast_gauge.elapsedHours = elapsedTime.inHours

		// Handle Fat burning
		textview_phase_fatburn_time.setTextColor(fatburnLabelColor)
		updateTimeView(textview_phase_fatburn_time, Stages.PHASE_FAT_BURN, elapsedTime)

		// Handle Ketosis
		if(currentStage.fatBurning)
		{
			updateTimeView(textview_phase_ketosis_time, Stages.PHASE_KETOSIS, elapsedTime)
		}
		else
		{
			textview_phase_ketosis_time.text = "--:--:--"
			textview_phase_ketosis_time.setTextColor(ketosisLabelColor)
		}

		// Handle Autophagy
		if(currentStage.ketosis)
		{
			updateTimeView(textview_phase_autophagy_time, Stages.PHASE_AUTOPHAGY, elapsedTime)
		}
		else
		{
			textview_phase_autophagy_time.text = "--:--:--"
			textview_phase_autophagy_time.setTextColor(autophagyLabelColor)
		}
	}

	private fun updateTimeView(view: TextView, phase: Phase, elapsedTime: Duration)
	{
		val phaseHours = phase.hours

		if(elapsedTime.inHours > phaseHours)
		{
			val timeSince = elapsedTime.minus(phaseHours.hours)
			timeSince.toComponents { hours, minutes, seconds, _ ->
				view.text = "%d:%02d:%02d".format(hours, minutes, seconds)
			}
			view.setTextColor(ContextCompat.getColor(requireContext(), R.color.bright_green))
		}
		else
		{
			val timeSince = elapsedTime.minus(phaseHours.hours)
			timeSince.toComponents { hours, minutes, seconds, _ ->
				view.text = "-%d:%02d:%02d".format(kotlin.math.abs(hours), kotlin.math.abs(minutes), kotlin.math.abs(seconds))
			}
			view.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_600))
		}
	}

	private fun updateButtons()
	{
		val isFasting = fast.isFasting()

		fast_fab_start.isVisible = !isFasting
		fast_fab_stop.isVisible = isFasting
	}

	private val storage by lazy { PreferenceManager.getDefaultSharedPreferences(requireActivity()) }

	private fun startFast(timeStartedMills: Long? = null)
	{
		if(!fast.isFasting())
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
			setupAlerts()

			i("Started fast!")
		}
		else
		{
			w("Cannot start fast with one in progress")
		}
	}

	private fun endFast()
	{
		if(fast.isFasting())
		{
			val now = Clock.System.now()
			val mills = now.toEpochMilliseconds()
			storage.edit { putLong(Data.KEY_FAST_END, mills) }

			GlobalScope.launch { saveFastToLog(fast.getFastStart(), fast.getFastEnd()) }

			i("Fast ended!")

			updateUi()
			setupAlerts()
		}
		else
		{
			w("Cannot end fast, there is none started")
		}
	}

	private suspend fun saveFastToLog(startTime: Instant?, endTime: Instant?)
	{
		if(startTime != null && endTime != null)
		{
			val duration = endTime.minus(startTime)
			val newEntry = FastEntry(start = startTime.toEpochMilliseconds(), length = duration.toLongMilliseconds())
			database.fastDao().insertAll(newEntry)
		}
		else
		{
			e("No start time when ending fast!")
		}
	}
}