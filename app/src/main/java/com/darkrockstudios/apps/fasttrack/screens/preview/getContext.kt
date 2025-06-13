package com.darkrockstudios.apps.fasttrack.screens.preview

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
fun getContext(): Context {
	return if (LocalInspectionMode.current) {
		DummyContext()
	} else {
		LocalContext.current
	}
}