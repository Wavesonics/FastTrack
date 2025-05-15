package com.darkrockstudios.apps.fasttrack.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Implementation of App Widget functionality for the Fasting widget.
 * This class is responsible for receiving broadcast events related to the widget.
 */
class FastingWidgetReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = FastingWidget()
}