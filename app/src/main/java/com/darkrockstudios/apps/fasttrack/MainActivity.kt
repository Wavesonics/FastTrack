package com.darkrockstudios.apps.fasttrack

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vansuita.materialabout.builder.AboutBuilder
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import kotlin.time.ExperimentalTime


@ExperimentalTime
class MainActivity: AppCompatActivity()
{
	private val fast by inject<FastUtils>()
	private val navController by lazy { findNavController(R.id.nav_host_fragment) }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		val navView: BottomNavigationView = findViewById(R.id.nav_view)

		setSupportActionBar(appActionBar)

		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val appBarConfiguration = AppBarConfiguration(
				setOf(
						R.id.navigation_fasting, R.id.navigation_log, R.id.navigation_profile))
		setupActionBarWithNavController(navController, appBarConfiguration)
		navView.setupWithNavController(navController)
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
				navController.navigate(R.id.infoActivity)
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
		val energyModeStr = if(curPhase.fatBurning) getString(R.string.fasting_energy_mode_fat) else getString(R.string.fasting_energy_mode_glucose)

		val shareText = if(fast.isFasting())
		{
			getString(R.string.share_text, elapsedHours, elapsedMinutes, energyModeStr)
		}
		else
		{
			getString(R.string.share_text_past_tense, elapsedHours, elapsedMinutes, energyModeStr)
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
				.addGooglePlayStoreLink("4972909458888823299")
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