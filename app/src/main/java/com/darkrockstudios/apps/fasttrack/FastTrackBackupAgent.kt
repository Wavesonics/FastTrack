package com.darkrockstudios.apps.fasttrack

import android.app.backup.BackupAgentHelper
import android.app.backup.SharedPreferencesBackupHelper
import android.preference.PreferenceManager

/**
 * This backup agent handles backing up the app's SharedPreferences for
 * older Android versions (pre-Android 6.0) that don't support Auto Backup.
 */
class FastTrackBackupAgent : BackupAgentHelper() {

	private val PREFS_BACKUP_KEY = "app_shared_prefs"

	override fun onCreate() {
		super.onCreate()

		val prefFileName = PreferenceManager.getDefaultSharedPreferencesName(this)
		val helper = SharedPreferencesBackupHelper(this, prefFileName)

		addHelper(PREFS_BACKUP_KEY, helper)
	}
}