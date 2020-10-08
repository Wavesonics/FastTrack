package com.darkrockstudios.apps.fasttrack.di

import androidx.room.Room
import com.darkrockstudios.apps.fasttrack.FastUtils
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import org.koin.dsl.module
import org.koin.experimental.builder.single

val mainModule = module {
	single {
		Room.databaseBuilder(
				get(),
				AppDatabase::class.java,
				"app-database"
							).build()
	}
	single<FastUtils>()
}