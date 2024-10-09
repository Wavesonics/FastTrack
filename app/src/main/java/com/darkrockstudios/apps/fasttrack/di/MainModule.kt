package com.darkrockstudios.apps.fasttrack.di

import androidx.room.Room
import com.darkrockstudios.apps.fasttrack.data.FastUtils
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val mainModule = module {
	single {
		Room.databaseBuilder(
				get(),
				AppDatabase::class.java,
				"app-database"
							).build()
	}
	singleOf(::FastUtils)
}