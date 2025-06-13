package com.darkrockstudios.apps.fasttrack.data.activefast

import kotlinx.datetime.Instant

/**
 * A fake implementation of ActiveFastDataSource for testing purposes.
 * Uses an in-memory data store to simulate storage operations.
 */
class FakeActiveFastDataSource : ActiveFastDataSource {
	private var fastStart: Instant? = null
	private var fastEnd: Instant? = null

	override fun getFastStart(): Instant? = fastStart

	override fun getFastEnd(): Instant? = fastEnd

	override fun setFastStart(time: Instant) {
		fastStart = time
	}

	override fun setFastEnd(time: Instant) {
		fastEnd = time
	}

	override fun clearFastStart() {
		fastStart = null
	}

	override fun clearFastEnd() {
		fastEnd = null
	}

	/**
	 * Clears all data - useful for test setup/teardown
	 */
	fun clear() {
		fastStart = null
		fastEnd = null
	}
}