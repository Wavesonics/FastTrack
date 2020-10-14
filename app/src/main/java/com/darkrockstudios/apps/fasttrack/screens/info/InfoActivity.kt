package com.darkrockstudios.apps.fasttrack.screens.info

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.darkrockstudios.apps.fasttrack.R
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity: AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_info)
		setSupportActionBar(findViewById(R.id.toolbar))

		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setDisplayShowHomeEnabled(true)

		info_pager.adapter = adapter
		tab_layout.setupWithViewPager(info_pager)
	}

	private enum class Pages
	{ INFO, TIPS }

	private val adapter = object: FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
	{
		override fun getCount(): Int = Pages.values().size

		override fun getPageTitle(position: Int): CharSequence
		{
			return when(position)
			{
				Pages.INFO.ordinal -> getString(R.string.info_page_title_info)
				Pages.TIPS.ordinal -> getString(R.string.info_page_title_tips)
				else               -> throw IllegalArgumentException("Bad page given for info title")
			}
		}

		override fun getItem(position: Int): Fragment
		{
			return when(position)
			{
				Pages.INFO.ordinal -> InfoFragment.newInstance(R.raw.info)
				Pages.TIPS.ordinal -> InfoFragment.newInstance(R.raw.tips)
				else               -> throw IllegalArgumentException("Bad page given for info fragment")
			}
		}
	}

	override fun onSupportNavigateUp(): Boolean
	{
		onBackPressed()
		return true
	}
}