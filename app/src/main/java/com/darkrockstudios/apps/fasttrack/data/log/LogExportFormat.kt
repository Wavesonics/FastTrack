package com.darkrockstudios.apps.fasttrack.data.log

import androidx.annotation.StringRes
import com.darkrockstudios.apps.fasttrack.R

/** Logbook export formats offered to the user. */
enum class LogExportFormat(
	@StringRes val labelRes: Int,
	val extension: String,
	val mimeType: String,
) {
	CSV(R.string.export_format_csv, "csv", "text/csv"),
	ICS(R.string.export_format_ics, "ics", "text/calendar"),
	ACTIVITY_STREAMS(R.string.export_format_activitystreams, "json", "application/activity+json"),
}
