package com.darkrockstudios.apps.fasttrack.data.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class TestClock : Clock {
	var currentTime: Instant = Instant.fromEpochMilliseconds(0)

	override fun now(): Instant = currentTime
}