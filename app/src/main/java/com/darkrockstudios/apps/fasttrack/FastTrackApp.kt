package com.darkrockstudios.apps.fasttrack

import android.app.Application
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.SafeRawSatchelSerializer
import com.darkrockstudios.apps.fasttrack.di.mainModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File

class FastTrackApp: Application()
{
	override fun onCreate()
	{
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
	}
}