package com.darkrockstudios.apps.fasttrack.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.FastUtils
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.screens.info.InfoActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vansuita.materialabout.builder.AboutBuilder
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import kotlin.time.ExperimentalTime


@ExperimentalTime
class MainActivity: AppCompatActivity()
{
	private val fast by inject<FastUtils>()

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		setSupportActionBar(appActionBar)

		content_view_pager.adapter = MainViewPagerAdapter(this)
		content_view_pager.setPageTransformer(ZoomOutPageTransformer())

		nav_view.setOnNavigationItemSelectedListener(navigationListener)
		content_view_pager.registerOnPageChangeCallback(pageChangeListener)
	}

	private val pageChangeListener = object: ViewPager2.OnPageChangeCallback()
	{
		override fun onPageSelected(position: Int)
		{
			when(position)
			{
				0 ->
				{
					nav_view.menu.findItem(R.id.navigation_fasting).isChecked = true
					supportActionBar?.title = getString(R.string.title_fasting)
				}
				1 ->
				{
					nav_view.menu.findItem(R.id.navigation_log).isChecked = true
					supportActionBar?.title = getString(R.string.title_log)
				}
				2 ->
				{
					nav_view.menu.findItem(R.id.navigation_profile).isChecked = true
					supportActionBar?.title = getString(R.string.title_profile)
				}
			}
		}
	}

	private val navigationListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
		when(menuItem.itemId)
		{
			R.id.navigation_fasting -> content_view_pager.currentItem = 0
			R.id.navigation_log -> content_view_pager.currentItem = 1
			R.id.navigation_profile -> content_view_pager.currentItem = 2
		}
		true
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean
	{
		menuInflater.inflate(R.menu.menu_main, menu);
		return true
	}

	override fun onPrepareOptionsMenu(menu: Menu): Boolean
	{
		menu.findItem(R.id.action_share)?.isEnabled = (fast.getFastStart() != null)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		return when(item.itemId)
		{
			R.id.action_share ->
			{
				shareText()
				true
			}
			R.id.action_info ->
			{
				startActivity(Intent(this, InfoActivity::class.java))
				true
			}
			R.id.action_about ->
			{
				showAbout()
				true
			}
			else              -> false
		}
	}

	private fun shareText()
	{
		val elapsedHours: Int
		val elapsedMinutes: Int

		val elapsedTime = fast.getElapsedFastTime()
		elapsedTime.toComponents { hours, minutes, _, _ ->
			elapsedHours = hours
			elapsedMinutes = minutes
		}

		val curPhase = Stages.getCurrentPhase(elapsedTime)
		val shareText = if(fast.isFasting())
		{
			val energyModeStr = if(curPhase.fatBurning) getString(R.string.fasting_energy_mode_fat) else getString(R.string.fasting_energy_mode_glucose)
			getString(R.string.share_text, elapsedHours, elapsedMinutes, energyModeStr)
		}
		else
		{
			getString(R.string.share_text_past_tense, elapsedHours, elapsedMinutes)
		}

		val sendIntent: Intent = Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_TEXT, shareText)
			type = "text/plain"
		}

		val shareIntent = Intent.createChooser(sendIntent, null)
		startActivity(shareIntent)
	}

	private fun showAbout()
	{
		val view = AboutBuilder.with(this)
				.setPhoto(R.mipmap.profile_picture)
				.setCover(R.mipmap.profile_cover)
				.setName(R.string.about_name)
				.setSubTitle(R.string.about_subtitle)
				.setBrief(R.string.about_brief)
				.setAppIcon(R.drawable.app_icon)
				.setAppName(R.string.app_name)
				.addGitHubLink("Wavesonics")
				.addTwitterLink("Wavesonics")
				.addWebsiteLink("https://adamwbrown.me/")
				.addFiveStarsAction()
				.setVersionNameAsAppSubTitle()
				.addShareAction(R.string.app_name)
				.setWrapScrollView(true)
				.setLinksAnimated(true)
				.setShowAsCard(true)
				.build()

		AlertDialog.Builder(this).setView(view).create().show()
	}
}