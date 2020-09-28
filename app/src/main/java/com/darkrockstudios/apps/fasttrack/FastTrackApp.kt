package com.darkrockstudios.apps.fasttrack

import android.app.Application
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.di.mainModule
import com.log4k.Level
import com.log4k.Log4k
import com.log4k.android.AndroidAppender
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File

class FastTrackApp: Application()
{
	override fun onCreate()
	{
		super.onCreate()

		Log4k.add(Level.Verbose, ".*", AndroidAppender())

		Satchel.init(
				storer = FileSatchelStorer(File(filesDir, Data.STORAGE_PATH)),
				encrypter = BypassSatchelEncrypter,
				serializer = RawSatchelSerializer
					)

		startKoin {
			androidContext(this@FastTrackApp)
			modules(mainModule)
		}
	}
}