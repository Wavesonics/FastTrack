package com.darkrockstudios.apps.fasttrack.data.utils

import kotlin.time.Clock
import kotlin.time.Instant

class TestClock : Clock {
	var currentTime: Instant = Instant.fromEpochMilliseconds(0)

	override fun now(): Instant = currentTime
}