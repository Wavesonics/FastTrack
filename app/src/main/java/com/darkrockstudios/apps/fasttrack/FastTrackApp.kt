package com.darkrockstudios.apps.fasttrack

import android.app.Application
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.collection.intSetOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Configuration
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.SafeRawSatchelSerializer
import com.darkrockstudios.apps.fasttrack.di.mainModule
import com.darkrockstudios.apps.fasttrack.widget.FastingWidgetReceiver
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File

class FastTrackApp : Application(), Configuration.Provider {
	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreate() {
		super.onCreate()

		Napier.base(DebugAntilog())

		Satchel.init(
			storer = FileSatchelStorer(File(filesDir, Data.STORAGE_PATH)),
			encrypter = BypassSatchelEncrypter,
			serializer = SafeRawSatchelSerializer
		)

		startKoin {
			androidContext(this@FastTrackApp)
			modules(mainModule)
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
			GlobalScope.launch {
				publishWidgetPreviews(this@FastTrackApp)
			}
		}
	}

	@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
	private suspend fun publishWidgetPreviews(context: Context) {
		GlanceAppWidgetManager(context).setWidgetPreviews(
			receiver = FastingWidgetReceiver::class,
			widgetCategories = intSetOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN)
		)
	}

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder()
			.setMinimumLoggingLevel(Log.INFO)
			// Reserve job IDs 1000-2000 for WorkManager (AlertService uses 1-5)
			.setJobSchedulerJobIdRange(1000, 2000)
			.build()
}