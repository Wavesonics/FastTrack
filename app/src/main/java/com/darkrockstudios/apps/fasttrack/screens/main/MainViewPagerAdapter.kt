package com.darkrockstudios.apps.fasttrack.screens.main

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
			Screens.FASTING.ordinal -> FastingFragment.newInstance()
			Screens.LOG.ordinal -> LogFragment.newInstance()
			Screens.PROFILE.ordinal -> ProfileFragment.newInstance()
			else                    -> throw IllegalArgumentException("MainViewPagerAdapter bad page: $position")
		}
	}
}