package com.darkrockstudios.apps.fasttrack.data.log

import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration

data class FastingLogEntry(
	val id: Int,
	val start: LocalDateTime,
	val length: Duration
)
