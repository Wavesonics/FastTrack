package com.darkrockstudios.apps.fasttrack.screens.info

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.getRawTextFile
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.content_info.*

class InfoActivity: AppCompatActivity()
{
	private val markwon by lazy { Markwon.create(this) }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_info)
		setSupportActionBar(findViewById(R.id.toolbar))

		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setDisplayShowHomeEnabled(true)

		val markdown = resources.getRawTextFile(R.raw.info)
		markwon.setMarkdown(info_details, markdown)
	}

	override fun onSupportNavigateUp(): Boolean
	{
		onBackPressed()
		return true
	}
}