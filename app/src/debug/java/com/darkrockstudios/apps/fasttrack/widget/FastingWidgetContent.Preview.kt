package com.darkrockstudios.apps.fasttrack.widget

import androidx.compose.runtime.Composable
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@ExperimentalGlancePreviewApi
@Preview(widthDp = 250, heightDp = 120)
@Composable
fun FastingWidget_NotFasting_Preview() {
	val fakeRepo = object : ActiveFastRepository {
		override fun isFasting(): Boolean = false
		override fun getElapsedFastTime(): Duration = Duration.ZERO
		override fun getFastStart(): Instant? = null
		override fun getFastEnd(): Instant? = null
		override fun startFast(timeStarted: Instant?) {}
		override fun endFast(timeEnded: Instant?) {}
		override fun debugOverrideFastStart(newStart: Instant) {}
	}

	FastingWidgetContent(fakeRepo)
}

@ExperimentalGlancePreviewApi
@Preview()
@Composable
fun FastingWidget_Fasting16h_Preview() {
	val now = Clock.System.now()
	val start = now - 16.hours

	val fakeRepo = object : ActiveFastRepository {
		override fun isFasting(): Boolean = true
		override fun getElapsedFastTime(): Duration = 16.hours
		override fun getFastStart(): Instant? = start
		override fun getFastEnd(): Instant? = null
		override fun startFast(timeStarted: Instant?) {}
		override fun endFast(timeEnded: Instant?) {}
		override fun debugOverrideFastStart(newStart: Instant) {}
	}

	FastingWidgetContent(fakeRepo)
}
