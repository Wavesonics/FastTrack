package com.darkrockstudios.apps.fasttrack.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent

class FastingWidget : GlanceAppWidget() {

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		provideContent {
			FastingWidgetTheme {
				FastingWidgetContent()
			}
		}
	}
}
