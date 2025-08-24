package com.darkrockstudios.apps.fasttrack.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.darkrockstudios.apps.fasttrack.screens.preview.DummyContext

@Composable
fun getGlanceContext(): Context {
	return if (LocalInspectionMode.current) {
		DummyContext()
	} else {
		LocalContext.current
	}
}

class NoOpAction : ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: ActionParameters
	) {
		// no-op for preview/defaults
	}
}