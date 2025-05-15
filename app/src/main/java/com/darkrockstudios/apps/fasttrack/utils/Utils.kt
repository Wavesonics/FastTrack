package com.darkrockstudios.apps.fasttrack.utils

import android.content.Context
import android.content.res.Resources
import android.text.format.DateFormat
import android.util.TypedValue
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.text.util.LocalePreferences
import com.darkrockstudios.apps.fasttrack.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*


object Utils
{
	fun showInfoDialog(@StringRes titleRes: Int, @StringRes contentRes: Int, context: Context)
	{
		MaterialAlertDialogBuilder(context)
				.setTitle(titleRes)
				.setMessage(contentRes)
				.setPositiveButton(R.string.info_dialog_positive, null)
				.show()
	}
}

fun Resources.dp2px(dp: Int): Int
{
	return TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			dp.toFloat(), displayMetrics
									).toInt()
}

fun Int.dp2px(resource: Resources): Int
{
	return TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			this.toFloat(), resource.displayMetrics
									).toInt()
}

fun Resources.getRawTextFile(@RawRes id: Int) =
	openRawResource(id).bufferedReader().use { it.readText() }

fun shouldUse24HourFormat(context: Context): Boolean {
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