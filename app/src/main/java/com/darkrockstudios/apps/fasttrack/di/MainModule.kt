package com.darkrockstudios.apps.fasttrack.di

import androidx.room.Room
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastDataSource
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastPreferencesDataSource
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepositoryImpl
import com.darkrockstudios.apps.fasttrack.data.database.AppDatabase
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogDatabaseDatasource
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogDatasource
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepository
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogRepositoryImpl
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsPreferencesDatasource
import com.darkrockstudios.apps.fasttrack.screens.fasting.FastingViewModel
import com.darkrockstudios.apps.fasttrack.screens.fasting.IFastingViewModel
import com.darkrockstudios.apps.fasttrack.screens.log.ILogViewModel
import com.darkrockstudios.apps.fasttrack.screens.log.LogViewModel
import com.darkrockstudios.apps.fasttrack.screens.log.manualadd.IManualAddViewModel
import com.darkrockstudios.apps.fasttrack.screens.log.manualadd.ManualAddViewModel
import com.darkrockstudios.apps.fasttrack.screens.profile.IProfileViewModel
import com.darkrockstudios.apps.fasttrack.screens.profile.ProfileViewModel
import kotlinx.datetime.Clock
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule = module {
	single {
		Room.databaseBuilder(
			get(),
			AppDatabase::class.java,
			"app-database"
		).build()
	}

	single { Clock.System } bind Clock::class

	singleOf(::SettingsPreferencesDatasource) bind SettingsDatasource::class

	singleOf(::ActiveFastPreferencesDataSource) bind ActiveFastDataSource::class
	singleOf(::ActiveFastRepositoryImpl) bind ActiveFastRepository::class

	singleOf(::FastingLogDatabaseDatasource) bind FastingLogDatasource::class
	singleOf(::FastingLogRepositoryImpl) bind FastingLogRepository::class

	viewModelOf(::FastingViewModel) bind IFastingViewModel::class
	viewModelOf(::LogViewModel) bind ILogViewModel::class
	viewModelOf(::ProfileViewModel) bind IProfileViewModel::class
	viewModelOf(::ManualAddViewModel) bind IManualAddViewModel::class
}
