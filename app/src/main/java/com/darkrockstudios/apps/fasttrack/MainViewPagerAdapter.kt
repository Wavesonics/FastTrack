package com.darkrockstudios.apps.fasttrack

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.darkrockstudios.apps.fasttrack.screens.fasting.FastingFragment
import com.darkrockstudios.apps.fasttrack.screens.log.LogFragment
import com.darkrockstudios.apps.fasttrack.screens.profile.ProfileFragment
import kotlin.time.ExperimentalTime

class MainViewPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa)
{
	private enum class Screens
	{ FASTING, LOG, PROFILE }

	override fun getItemCount() = Screens.values().size

	@ExperimentalTime
	override fun createFragment(position: Int): Fragment
	{
		return when(position)
		{
			Screens.FASTING.ordinal -> FastingFragment.instance()
			Screens.LOG.ordinal -> LogFragment.instance()
			Screens.PROFILE.ordinal -> ProfileFragment.instance()
			else                    -> throw IllegalArgumentException("MainViewPagerAdapter bad page: $position")
		}
	}
}