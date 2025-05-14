package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.text.util.LocalePreferences
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.darkrockstudios.apps.fasttrack.AlertService
import com.darkrockstudios.apps.fasttrack.BuildConfig
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.FastUtils
import com.darkrockstudios.apps.fasttrack.data.Phase
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.database.FastEntry
import com.darkrockstudios.apps.fasttrack.databinding.FragmentFastingBinding
import com.darkrockstudios.apps.fasttrack.utils.Utils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime


@ExperimentalTime
class FastingFragment : Fragment() {
	companion object {
		fun newInstance() = FastingFragment()
	}

	private val uiHandler = Handler(Looper.getMainLooper())
	private val database by inject<AppDatabase>()
	private val fast by inject<FastUtils>()

	@ColorInt
	private var fatburnLabelColor: Int = Color.WHITE

	@ColorInt
	private var ketosisLabelColor: Int = Color.WHITE

	@ColorInt
	private var autophagyLabelColor: Int = Color.WHITE

	private lateinit var binding: FragmentFastingBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setupAlerts()
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentFastingBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.fastFabStart.setOnClickListener {
			MaterialAlertDialogBuilder(view.context)
				.setTitle(R.string.confirm_start_fast_title)
				.setPositiveButton(R.string.confirm_start_fast_positive) { _, _ -> startFast() }
				.setNeutralButton(R.string.confirm_start_fast_neutral) { _, _ -> showStartPicker() }
				.setNegativeButton(R.string.confirm_start_fast_negative, null)
				.show()
		}

		binding.fastFabStop.setOnClickListener {
			context?.let { ctx ->
				MaterialAlertDialogBuilder(ctx)
					.setTitle(R.string.confirm_end_fast_title)
					.setPositiveButton(R.string.confirm_end_fast_positive) { _, _ -> endFast() }
					.setNegativeButton(R.string.confirm_end_fast_negative, null)
					.show()
			}
		}

		if (BuildConfig.DEBUG) {
			binding.fastFabDebug.isVisible = true
			binding.fastFabDebug.setOnClickListener {
				increaseFastingTimeByOneHour()
			}
		}

		binding.fastNotificationsCheckbox.isChecked = storage.getBoolean(Data.KEY_FAST_ALERTS, true)
		binding.fastNotificationsCheckbox.setOnCheckedChangeListener { _, isChecked ->
			storage.edit {
				putBoolean(Data.KEY_FAST_ALERTS, isChecked)
			}
			setupAlerts()
		}

		fatburnLabelColor = binding.textviewPhaseFatburnLabel.currentTextColor
		binding.textviewPhaseFatburnLabel.setOnClickListener {
			Utils.showInfoDialog(
				R.string.info_dialog_fat_burn_title,
				R.string.info_dialog_fat_burn_content,
				requireContext()
			)
		}

		ketosisLabelColor = binding.textviewPhaseKetosisLabel.currentTextColor
		binding.textviewPhaseKetosisLabel.setOnClickListener {
			Utils.showInfoDialog(
				R.string.info_dialog_ketosis_title,
				R.string.info_dialog_ketosis_content,
				requireContext()
			)
		}

		autophagyLabelColor = binding.textviewPhaseAutophagyLabel.currentTextColor
		binding.textviewPhaseAutophagyLabel.setOnClickListener {
			Utils.showInfoDialog(
				R.string.info_dialog_autophagy_title,
				R.string.info_dialog_autophagy_content,
				requireContext()
			)
		}
	}

	private fun setupAlerts() {
		val ctx = context ?: return

		val shouldAlert = storage.getBoolean(Data.KEY_FAST_ALERTS, true)

		if (fast.isFasting()) {
			if (shouldAlert) {
				val elapsedTime = fast.getElapsedFastTime()
				AlertService.scheduleAlerts(elapsedTime, ctx)
			}
			// User doesn't want notifications
			else {
				AlertService.cancelAlerts(ctx)
			}
		}
		// No notifications if we aren't fasting
		else {
			AlertService.cancelAlerts(ctx)
		}
	}

	override fun onStart() {
		super.onStart()

		startTimerUpdate()
	}

	override fun onResume() {
		super.onResume()

		updateUi()
	}

	/**
	 * Increases the fasting time by 1 hour by setting the start time to 1 hour earlier.
	 * This method is only available in debug builds.
	 */
	private fun increaseFastingTimeByOneHour() {
		if (fast.isFasting()) {
			val currentStartTime = storage.getLong(Data.KEY_FAST_START, -1)
			if (currentStartTime > 0) {
				val newStartTime = currentStartTime - (1 * 60 * 60 * 1000)

				storage.edit { putLong(Data.KEY_FAST_START, newStartTime) }

				updateUi()

				Napier.d("Debug: Increased fasting time by 1 hour")
			}
		} else {
			Napier.d("Debug: Cannot increase fasting time when not fasting")
		}
	}

	override fun onStop() {
		super.onStop()

		stopTimerUpdate()
	}

	private val updater = Runnable {
		updateUi()
		if (fast.isFasting()) {
			startTimerUpdate()
		}
	}

	private fun showStartPicker() {

		val datePicker =
			MaterialDatePicker.Builder.datePicker()
				.setTitleText(R.string.start_picker_title)
				.setSelection(MaterialDatePicker.todayInUtcMilliseconds())
				.build()
		datePicker.show(childFragmentManager, "FastStartDatePicker")

		datePicker.addOnPositiveButtonClickListener { selectedDateEpochMs ->

			val selectedDate = Instant.fromEpochMilliseconds(selectedDateEpochMs)
			val selectedDateLocal = selectedDate.toLocalDateTime(TimeZone.UTC)

			val timeFormat = if (shouldUse24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
			val timePicker: MaterialTimePicker = MaterialTimePicker.Builder()
				.setTitleText(R.string.start_picker_title)
				.setTimeFormat(timeFormat)
				.build()

			timePicker.show(childFragmentManager, "FastStartTimePicker")
			timePicker.addOnPositiveButtonClickListener {

				val localDateTime = LocalDateTime(
					year = selectedDateLocal.year,
					month = selectedDateLocal.month,
					dayOfMonth = selectedDateLocal.dayOfMonth,
					hour = timePicker.hour,
					minute = timePicker.minute,
					second = 0,
					nanosecond = 0
				)

				val userTimeZone = TimeZone.currentSystemDefault()
				val selectedDateTime = localDateTime.toInstant(userTimeZone)

				Napier.i("Selected datetime in user timezone ($userTimeZone): $selectedDateTime")
				onDateTimePicked(Date.from(selectedDateTime.toJavaInstant()))
			}
		}
	}

	private fun onDateTimePicked(date: Date) {
		startFast(date.time)
	}

	private fun startTimerUpdate() {
		uiHandler.postDelayed(updater, 10)
	}

	private fun stopTimerUpdate() {
		uiHandler.removeCallbacks(updater)
	}

	private fun updateUi() {
		updateButtons()
		updateTimer()
		updateStage()
	}

	private fun updateStage() {
		val fastStart = fast.getFastStart()
		binding.textviewStageTitle.text = ""
		binding.textviewStageDescription.text = ""
		binding.textviewEnergyMode.text = ""

		if (fast.isFasting() && fastStart != null) {
			val elapsedTime = Clock.System.now().minus(fastStart)
			val elapsedHours = elapsedTime.inWholeHours.toInt()

			var stageIndex = Stages.stage.indexOfLast { it.hours <= elapsedHours }
			if (stageIndex < 0) {
				stageIndex = 0
			}

			val stage = Stages.stage[stageIndex]

			val curPhase = Stages.getCurrentPhase(elapsedTime)
			if (curPhase.fatBurning) {
				binding.textviewEnergyMode.text = getString(
					R.string.fasting_energy_mode,
					getString(R.string.fasting_energy_mode_fat)
				)
			} else {
				binding.textviewEnergyMode.text = getString(
					R.string.fasting_energy_mode,
					getString(R.string.fasting_energy_mode_glucose)
				)
			}

			binding.textviewStageTitle.text = getString(stage.title)
			binding.textviewStageDescription.text = getString(stage.description)
		}
	}

	@ExperimentalTime
	private fun updateTimer() {
		val fastStart = fast.getFastStart()
		val fastEnd = fast.getFastEnd()

		if (fastStart != null) {
			if (fastEnd == null) {
				val elapsedTime = Clock.System.now().minus(fastStart)
				elapsedTime.toComponents(::updateTimerView)
				updatePhases(elapsedTime)
			} else {
				val elapsedTime = fastEnd.minus(fastStart)
				elapsedTime.toComponents(::updateTimerView)
				updatePhases(elapsedTime)
			}
		} else {
			binding.chronometer.text = "0:00:00"
		}
	}

	private fun updateTimerView(hours: Long, minutes: Int, seconds: Int, nanoseconds: Int) {
		val secondsStr = "%02d".format(seconds)
		val minutesStr = "%02d".format(minutes)
		binding.chronometer.text = "$hours:$minutesStr:$secondsStr"

		binding.milliseconds.text = "${nanoseconds / 10000000}"
	}

	private fun updatePhases(elapsedTime: Duration) {
		val currentStage = Stages.getCurrentPhase(elapsedTime)

		binding.fastGauge.elapsedHours = elapsedTime.inWholeHours.toDouble()

		// Handle Fat burning
		binding.textviewPhaseFatburnTime.setTextColor(fatburnLabelColor)
		updateTimeView(binding.textviewPhaseFatburnTime, Stages.PHASE_FAT_BURN, elapsedTime)

		// Handle Ketosis
		if (currentStage.fatBurning) {
			updateTimeView(binding.textviewPhaseKetosisTime, Stages.PHASE_KETOSIS, elapsedTime)
		} else {
			binding.textviewPhaseKetosisTime.text = "--:--:--"
			binding.textviewPhaseKetosisTime.setTextColor(ketosisLabelColor)
		}

		// Handle Autophagy
		if (currentStage.ketosis) {
			updateTimeView(binding.textviewPhaseAutophagyTime, Stages.PHASE_AUTOPHAGY, elapsedTime)
		} else {
			binding.textviewPhaseAutophagyTime.text = "--:--:--"
			binding.textviewPhaseAutophagyTime.setTextColor(autophagyLabelColor)
		}
	}

	private fun updateTimeView(view: TextView, phase: Phase, elapsedTime: Duration) {
		val phaseHours = phase.hours

		if (elapsedTime.toDouble(DurationUnit.HOURS) > phaseHours) {
			val timeSince = elapsedTime.minus(phaseHours.hours)
			timeSince.toComponents { hours, minutes, seconds, _ ->
				view.text = "%d:%02d:%02d".format(hours, minutes, seconds)
			}
			view.setTextColor(ContextCompat.getColor(requireContext(), R.color.bright_green))
		} else {
			val timeSince = elapsedTime.minus(phaseHours.hours)
			timeSince.toComponents { hours, minutes, seconds, _ ->
				view.text = "-%d:%02d:%02d".format(
					abs(hours),
					abs(minutes),
					abs(seconds)
				)
			}
			view.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_600))
		}
	}

	private fun updateButtons() {
		val isFasting = fast.isFasting()

		binding.fastFabStart.isVisible = !isFasting
		binding.fastFabStop.isVisible = isFasting
	}

	private val storage by lazy { PreferenceManager.getDefaultSharedPreferences(requireActivity()) }

	private fun startFast(timeStartedMills: Long? = null) {
		if (!fast.isFasting()) {
			val mills = if (timeStartedMills == null) {
				val now = Clock.System.now()
				now.toEpochMilliseconds()
			} else {
				timeStartedMills
			}

			storage.edit { putLong(Data.KEY_FAST_START, mills) }
			storage.edit { putLong(Data.KEY_FAST_END, -1) }

			updateUi()
			startTimerUpdate()
			setupAlerts()

			Napier.i("Started fast!")
		} else {
			Napier.w("Cannot start fast with one in progress")
		}
	}

	private fun endFast() {
		if (fast.isFasting()) {
			val now = Clock.System.now()
			val mills = now.toEpochMilliseconds()
			storage.edit { putLong(Data.KEY_FAST_END, mills) }

			GlobalScope.launch { saveFastToLog(fast.getFastStart(), fast.getFastEnd()) }

			Napier.i("Fast ended!")

			updateUi()
			setupAlerts()

			// Show the celebration!
			binding.konfettiOverlay.let { kview ->
				kview.build()
					.addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.RED, Color.BLUE)
					.setDirection(0.0, 359.0)
					.setSpeed(2f, 5f)
					.setFadeOutEnabled(true)
					.setTimeToLive(2000L)
					.addShapes(Shape.Square, Shape.Circle)
					.addSizes(Size(12))
					.setPosition(-50f, kview.width + 50f, -50f, -50f)
					.streamFor(300, 4000L)
			}
		} else {
			Napier.w("Cannot end fast, there is none started")
		}
	}

	private suspend fun saveFastToLog(startTime: Instant?, endTime: Instant?) {
		if (startTime != null && endTime != null) {
			val duration = endTime.minus(startTime)
			val newEntry = FastEntry(
				start = startTime.toEpochMilliseconds(),
				length = duration.inWholeMilliseconds
			)
			database.fastDao().insertAll(newEntry)
		} else {
			Napier.e("No start time when ending fast!")
		}
	}

	private fun shouldUse24HourFormat(context: Context): Boolean {
		return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
			when (LocalePreferences.getHourCycle()) {
				LocalePreferences.HourCycle.H11, LocalePreferences.HourCycle.H12 -> false
				LocalePreferences.HourCycle.H23, LocalePreferences.HourCycle.H24 -> true
				else -> {
					Locale.getDefault() != Locale.US
				}
			}
		} else {
			DateFormat.is24HourFormat(context)
		}
	}
}
