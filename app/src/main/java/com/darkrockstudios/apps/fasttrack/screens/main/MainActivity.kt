package com.darkrockstudios.apps.fasttrack.screens.main

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.bundle.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.darkrockstudios.apps.fasttrack.IntroActivity
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.data.FastUtils
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.databinding.ActivityMainBinding
import com.darkrockstudios.apps.fasttrack.screens.info.InfoActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vansuita.materialabout.builder.AboutBuilder
import org.koin.android.ext.android.inject
import kotlin.time.ExperimentalTime


@ExperimentalTime
class MainActivity: AppCompatActivity()
{
	private val storage by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
	private val fast by inject<FastUtils>()

	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		enableEdgeToEdge()

		handleStartFastExtra(intent)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(binding.appActionBar)

		binding.contentViewPager.adapter = MainViewPagerAdapter(this)
		//binding.contentViewPager.setPageTransformer(ZoomOutPageTransformer())

		binding.navView.setOnNavigationItemSelectedListener(navigationListener)
		binding.contentViewPager.registerOnPageChangeCallback(pageChangeListener)

		ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
			val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
			binding.appActionBar.updatePadding(top = insets.top)
			binding.navView.updatePadding(bottom = insets.bottom)
			windowInsets
		}

		// Show the intro if they haven't seen it
		if(!storage.getBoolean(Data.KEY_INTRO_SEEN, false))
		{
			startActivity(Intent(this, IntroActivity::class.java))
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		handleStartFastExtra(intent)
	}

	private fun handleStartFastExtra(intent: Intent?) {
		if (intent?.getBooleanExtra(START_FAST_EXTRA, false) == true) {
			val startNow = intent.getBooleanExtra(START_FAST_NOW_EXTRA, false)
			supportFragmentManager.setFragmentResult(
				START_FAST_EXTRA,
				bundleOf(START_FAST_EXTRA to true, START_FAST_NOW_EXTRA to startNow)
			)
		} else if (intent?.getBooleanExtra(STOP_FAST_EXTRA, false) == true) {
			supportFragmentManager.setFragmentResult(STOP_FAST_EXTRA, bundleOf(STOP_FAST_EXTRA to true))
		}
	}

	private val pageChangeListener = object: ViewPager2.OnPageChangeCallback()
	{
		override fun onPageSelected(position: Int)
		{
			when(position)
			{
				0 ->
				{
					binding.navView.menu.findItem(R.id.navigation_fasting).isChecked = true
					supportActionBar?.title = getString(R.string.title_fasting)
				}
				1 ->
				{
					binding.navView.menu.findItem(R.id.navigation_log).isChecked = true
					supportActionBar?.title = getString(R.string.title_log)
				}
				2 ->
				{
					binding.navView.menu.findItem(R.id.navigation_profile).isChecked = true
					supportActionBar?.title = getString(R.string.title_profile)
				}
			}
		}
	}

	private val navigationListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
		when(menuItem.itemId)
		{
			R.id.navigation_fasting -> binding.contentViewPager.currentItem = 0
			R.id.navigation_log -> binding.contentViewPager.currentItem = 1
			R.id.navigation_profile -> binding.contentViewPager.currentItem = 2
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
		val elapsedHours: Long
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

	companion object {
		const val START_FAST_EXTRA = "START_FAST"
		const val START_FAST_NOW_EXTRA = "START_FAST_NOW"
		const val STOP_FAST_EXTRA = "STOP_FAST"
	}
}
