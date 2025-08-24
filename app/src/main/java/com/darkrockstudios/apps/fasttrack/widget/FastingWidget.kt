package com.darkrockstudios.apps.fasttrack.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastPreferencesDataSource
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepositoryImpl
import com.darkrockstudios.apps.fasttrack.screens.main.MainActivity
import kotlin.time.Clock

class FastingWidget : GlanceAppWidget() {

	override val previewSizeMode = SizeMode.Responsive(
		setOf(
			// TODO These are just the defaults, should figure out our own
			DpSize(180.dp, 110.dp), // min size
			DpSize(250.dp, 110.dp)  // next breakpoint
		)
	)

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val repository: ActiveFastRepository = ActiveFastRepositoryImpl(
			ActiveFastPreferencesDataSource(context),
			Clock.System
		)
		provideContent {
			FastingWidgetTheme {
				val mainActivityIntent = Intent(context, MainActivity::class.java)
				val onClick = actionStartActivity(mainActivityIntent)

				val startFastIntent = Intent(context, MainActivity::class.java).apply {
					putExtra(MainActivity.START_FAST_EXTRA, true)
				}
				val startFast = actionStartActivity(startFastIntent)

				val stopFastIntent = Intent(context, MainActivity::class.java).apply {
					putExtra(MainActivity.STOP_FAST_EXTRA, true)
				}
				val stopFast = actionStartActivity(stopFastIntent)

				FastingWidgetContent(
					repository,
					onClick,
					startFast,
					stopFast
				)
			}
		}
	}

	override suspend fun providePreview(context: Context, widgetCategory: Int) {
		val repository = FakeActiveFastRepository()

		provideContent {
			FastingWidgetTheme {
				FastingWidgetContent(repository)
			}
		}
	}
}
