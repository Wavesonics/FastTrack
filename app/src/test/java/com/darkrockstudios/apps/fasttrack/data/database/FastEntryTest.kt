package com.darkrockstudios.apps.fasttrack.data.database

import com.darkrockstudios.apps.fasttrack.data.Stages
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.hours

class FastEntryTest {

	@Test
	fun testLengthHours() {
		// Test with 0 hours
		val entry0 = FastEntry(start = 0, length = 0)
		assertEquals(0.0, entry0.lengthHours(), 0.001)

		// Test with 1 hour
		val oneHourInMillis = 1.hours.inWholeMilliseconds
		val entry1 = FastEntry(start = 0, length = oneHourInMillis)
		assertEquals(1.0, entry1.lengthHours(), 0.001)

		// Test with 24 hours
		val twentyFourHoursInMillis = 24.hours.inWholeMilliseconds
		val entry24 = FastEntry(start = 0, length = twentyFourHoursInMillis)
		assertEquals(24.0, entry24.lengthHours(), 0.001)
	}

	@Test
	fun testCalculateKetosis_belowThreshold() {
		// Test with 1 hour below ketosis threshold
		val belowThresholdHours = (Stages.PHASE_KETOSIS.hours - 1)
		val belowThresholdMillis = belowThresholdHours.hours.inWholeMilliseconds
		val entry = FastEntry(start = 0, length = belowThresholdMillis)

		assertEquals(0.0, entry.calculateKetosis(), 0.001)
	}

	@Test
	fun testCalculateKetosis_atThreshold() {
		// Test with exactly at ketosis threshold
		val atThresholdHours = Stages.PHASE_KETOSIS.hours
		val atThresholdMillis = atThresholdHours.hours.inWholeMilliseconds
		val entry = FastEntry(start = 0, length = atThresholdMillis)

		assertEquals(0.0, entry.calculateKetosis(), 0.001)
	}

	@Test
	fun testCalculateKetosis_aboveThreshold() {
		// Test with 2 hours above ketosis threshold
		val aboveThresholdHours = (Stages.PHASE_KETOSIS.hours + 2)
		val aboveThresholdMillis = aboveThresholdHours.hours.inWholeMilliseconds
		val entry = FastEntry(start = 0, length = aboveThresholdMillis)

		assertEquals(2.0, entry.calculateKetosis(), 0.001)
	}

	@Test
	fun testCalculateAutophagy_belowThreshold() {
		// Test with 1 hour below autophagy threshold
		val belowThresholdHours = (Stages.PHASE_AUTOPHAGY.hours - 1)
		val belowThresholdMillis = belowThresholdHours.hours.inWholeMilliseconds
		val entry = FastEntry(start = 0, length = belowThresholdMillis)

		assertEquals(0.0, entry.calculateAutophagy(), 0.001)
	}

	@Test
	fun testCalculateAutophagy_atThreshold() {
		// Test with exactly at autophagy threshold
		val atThresholdHours = Stages.PHASE_AUTOPHAGY.hours
		val atThresholdMillis = atThresholdHours.hours.inWholeMilliseconds
		val entry = FastEntry(start = 0, length = atThresholdMillis)

		assertEquals(0.0, entry.calculateAutophagy(), 0.001)
	}

	@Test
	fun testCalculateAutophagy_aboveThreshold() {
		// Test with 6 hours above autophagy threshold
		val aboveThresholdHours = (Stages.PHASE_AUTOPHAGY.hours + 6)
		val aboveThresholdMillis = aboveThresholdHours.hours.inWholeMilliseconds
		val entry = FastEntry(start = 0, length = aboveThresholdMillis)

		assertEquals(6.0, entry.calculateAutophagy(), 0.001)
	}

	@Test
	fun testEdgeCases() {
		// Test with very large values
		val largeHoursInMillis = 100.hours.inWholeMilliseconds
		val largeEntry = FastEntry(start = 0, length = largeHoursInMillis)

		assertEquals(100.0, largeEntry.lengthHours(), 0.001)
		assertEquals(
			100.0 - Stages.PHASE_KETOSIS.hours,
			largeEntry.calculateKetosis(),
			0.001
		) // 100 - ketosis threshold
		assertEquals(
			100.0 - Stages.PHASE_AUTOPHAGY.hours,
			largeEntry.calculateAutophagy(),
			0.001
		) // 100 - autophagy threshold

		// Test with negative values (should not happen in real app, but testing for robustness)
		val negativeHoursInMillis = (-5).hours.inWholeMilliseconds
		val negativeEntry = FastEntry(start = 0, length = negativeHoursInMillis)

		assertEquals(-5.0, negativeEntry.lengthHours(), 0.001)
		assertEquals(0.0, negativeEntry.calculateKetosis(), 0.001)
		assertEquals(0.0, negativeEntry.calculateAutophagy(), 0.001)
	}
}
