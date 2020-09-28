package com.darkrockstudios.apps.fasttrack.screens.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.darkrockstudios.apps.fasttrack.R

class ProfileFragment: Fragment()
{
	private val viewModel by viewModels<ProfileViewModel>()

	override fun onCreateView(
			inflater: LayoutInflater, container: ViewGroup?,
			savedInstanceState: Bundle?): View?
	{
		return inflater.inflate(R.layout.profile_fragment, container, false)
	}
}