package com.darkrockstudios.apps.fasttrack

import android.content.Context
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