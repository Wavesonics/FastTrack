package com.darkrockstudios.apps.fasttrack.screens.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.darkrockstudios.apps.fasttrack.databinding.InfoFragmentBinding
import com.darkrockstudios.apps.fasttrack.utils.FragArg
import com.darkrockstudios.apps.fasttrack.utils.getRawTextFile
import io.noties.markwon.Markwon

class InfoFragment : Fragment() {
	companion object {
		fun newInstance(@RawRes infoRes: Int) = InfoFragment().apply {
			markdownResource = infoRes
		}
	}

	private var markdownResource: Int by FragArg()

	private val markwon by lazy { Markwon.create(requireContext()) }
	private val viewModel: InfoViewModel by viewModels()

	private lateinit var binding: InfoFragmentBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = InfoFragmentBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val markdown = resources.getRawTextFile(markdownResource)
		markwon.setMarkdown(binding.infoDetails, markdown)
	}
}