package com.darkrockstudios.apps.fasttrack

import android.app.Application
import com.log4k.Level
import com.log4k.Log4k
import com.log4k.android.AndroidAppender

class FastTrackApp: Application()
{
	override fun onCreate()
	{
		super.onCreate()

		Log4k.add(Level.Verbose, ".*", AndroidAppender())
	}
}