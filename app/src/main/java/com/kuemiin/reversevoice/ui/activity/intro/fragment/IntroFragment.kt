package com.kuemiin.reversevoice.ui.activity.intro.fragment

import android.view.LayoutInflater
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseFragment
import com.kuemiin.reversevoice.databinding.FragmentIntroBinding
import com.kuemiin.reversevoice.model.IntroSplash
import com.kuemiin.reversevoice.ui.activity.intro.IntroViewModel
import com.kuemiin.reversevoice.utils.MaxUtils
import dagger.hilt.android.AndroidEntryPoint

@Suppress("IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class IntroFragment(
    val inflater: LayoutInflater,
    val viewModel: IntroViewModel,
    val data: IntroSplash,
) : BaseFragment<FragmentIntroBinding>() {

    companion object {
        fun newInstance(
            inflater: LayoutInflater,
            viewModel: IntroViewModel,
            data: IntroSplash,
        ) = IntroFragment(inflater, viewModel, data)
    }

    override fun getLayoutId(): Int = R.layout.fragment_intro

    override fun setUp() {
        viewModel.activity = requireActivity()
        binding.viewmodel = viewModel
        binding.item = data
        setUpEvent()
        setUpAdapter()
        when (data.index) {
            0 -> {
                MaxUtils.loadNative123(
                    requireActivity(),
                    binding.flAdplaceholder,
                    binding.shimmerContainer,
                    binding.flAdplaceholder, 7
                )
            }

            4 -> {
                MaxUtils.loadNative123(
                    requireActivity(),
                    binding.flAdplaceholder,
                    binding.shimmerContainer,
                    binding.flAdplaceholder, 8
                )
            }

            3 -> {
                MaxUtils.loadNative123(
                    requireActivity(),
                    binding.flAdplaceholderFull,
                    binding.shimmerContainerFull,
                    binding.flAdplaceholderFull, 6
                )
            }
        }
    }

    private fun setUpEvent() {

    }

    private fun setUpAdapter() {

    }

}