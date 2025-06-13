package com.darkrockstudios.apps.fasttrack.data.activefast

import com.darkrockstudios.apps.fasttrack.data.utils.TestClock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class ActiveFastRepositoryImplTest {
	private lateinit var fakeDataSource: FakeActiveFastDataSource
	private lateinit var testClock: TestClock
	private lateinit var repository: ActiveFastRepositoryImpl

	@Before
	fun setup() {
		fakeDataSource = FakeActiveFastDataSource()
		testClock = TestClock()
		repository = ActiveFastRepositoryImpl(fakeDataSource, testClock)
	}

	@Test
	fun `isFasting returns false when no fast is started`() {
		assertFalse(repository.isFasting())
	}

	@Test
	fun `isFasting returns true when fast is started but not ended`() {
		val startTime = Instant.fromEpochMilliseconds(1000)
		fakeDataSource.setFastStart(startTime)

		assertTrue(repository.isFasting())
	}

	@Test
	fun `isFasting returns false when fast is started and ended`() {
		val startTime = Instant.fromEpochMilliseconds(1000)
		val endTime = Instant.fromEpochMilliseconds(2000)
		fakeDataSource.setFastStart(startTime)
		fakeDataSource.setFastEnd(endTime)

		assertFalse(repository.isFasting())
	}

	@Test
	fun `getElapsedFastTime returns zero when no fast is started`() {
		assertEquals(0.seconds, repository.getElapsedFastTime())
	}

	@Test
	fun `getElapsedFastTime returns correct duration for ongoing fast`() {
		val startTime = Instant.fromEpochMilliseconds(1000)
		testClock.currentTime = Instant.fromEpochMilliseconds(3000)
		fakeDataSource.setFastStart(startTime)

		assertEquals(2.seconds, repository.getElapsedFastTime())
	}

	@Test
	fun `getElapsedFastTime returns correct duration for completed fast`() {
		val startTime = Instant.fromEpochMilliseconds(1000)
		val endTime = Instant.fromEpochMilliseconds(4000)
		fakeDataSource.setFastStart(startTime)
		fakeDataSource.setFastEnd(endTime)

		assertEquals(3.seconds, repository.getElapsedFastTime())
	}

	@Test
	fun `startFast sets start time and clears end time`() {
		val startTime = Instant.fromEpochMilliseconds(1000)
		repository.startFast(startTime)

		assertEquals(startTime, fakeDataSource.getFastStart())
		assertNull(fakeDataSource.getFastEnd())
	}

	@Test
	fun `endFast sets end time`() {
		val startTime = Instant.fromEpochMilliseconds(1000)
		val endTime = Instant.fromEpochMilliseconds(2000)
		testClock.currentTime = endTime

		fakeDataSource.setFastStart(startTime)
		repository.endFast()

		assertEquals(endTime, fakeDataSource.getFastEnd())
	}

	@Test
	fun `debugOverrideFastStart updates start time`() {
		val newStartTime = Instant.fromEpochMilliseconds(5000)
		repository.debugOverrideFastStart(newStartTime)

		assertEquals(newStartTime, fakeDataSource.getFastStart())
	}
}
