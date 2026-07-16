package com.darkrockstudios.apps.fasttrack.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Utility class to update the Fasting widget when fasting state changes
 */
object WidgetUpdater {
	// One long-lived scope reused across calls, rather than allocating a fresh,
	// unmanaged CoroutineScope on every fast start/stop/tick.
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	/**
	 * Updates all instances of the Fasting widget
	 */
	fun updateWidgets(context: Context) {
		scope.launch {
			val manager = GlanceAppWidgetManager(context)
			val glanceIds = manager.getGlanceIds(FastingWidget::class.java)
			glanceIds.forEach { glanceId ->
				updateWidget(context, glanceId)
			}
		}
	}

	private suspend fun updateWidget(context: Context, glanceId: GlanceId) {
		// Force the widget to update by calling the FastingWidget's provideGlance method
		FastingWidget().update(context, glanceId)
	}
}