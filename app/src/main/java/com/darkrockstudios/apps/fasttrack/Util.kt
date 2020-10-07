package com.darkrockstudios.apps.fasttrack

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder


object Util
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

fun Int.dp2px(resource: Resources, dp: Int): Int
{
	return TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			dp.toFloat(), resource.displayMetrics
									).toInt()
}