package com.darkrockstudios.apps.fasttrack.data.activefast

import kotlin.time.Duration
import kotlin.time.Instant

interface ActiveFastRepository {
	fun isFasting(): Boolean
	fun getElapsedFastTime(): Duration
	fun getFastStart(): Instant?
	fun getFastEnd(): Instant?
	fun startFast(timeStarted: Instant?)
	fun endFast()
	fun debugOverrideFastStart(newStart: Instant)
}