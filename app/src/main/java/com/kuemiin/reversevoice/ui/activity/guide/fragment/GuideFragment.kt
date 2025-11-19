package com.kuemiin.reversevoice.ui.activity.guide.fragment

import android.view.LayoutInflater
import com.kuemiin.base.extension.onDebounceClick
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseFragment
import com.kuemiin.reversevoice.databinding.FragmentGuideBinding
import com.kuemiin.reversevoice.model.IntroSplash
import com.kuemiin.reversevoice.ui.activity.guide.GuideViewModel
import com.kuemiin.reversevoice.utils.MaxUtils
import com.kuemiin.reversevoice.utils.PermissionUtils
import com.kuemiin.reversevoice.utils.binding.setImageAnyNormal
import dagger.hilt.android.AndroidEntryPoint

@Suppress("IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class GuideFragment(
    val inflater: LayoutInflater,
    val viewModel: GuideViewModel,
    val data: IntroSplash,
) : BaseFragment<FragmentGuideBinding>() {

    var hasPerNotify = false

    companion object {
        fun newInstance(
            inflater: LayoutInflater,
            viewModel: GuideViewModel,
            data: IntroSplash,
        ) = GuideFragment(inflater, viewModel, data)
    }

    override fun getLayoutId(): Int = R.layout.fragment_guide

    override fun setUp() {
        viewModel.activity = requireActivity()
        binding.viewmodel = viewModel
        binding.item = data
        setUpEvent()
        setApplyView()
        when (data.index) {
            3 -> {
                MaxUtils.loadNativeNotify(
                    requireActivity(),
                    binding.flAdplaceholder,
                    binding.shimmerContainer,
                    binding.flAdplaceholder
                )
            }

            4 -> {
                MaxUtils.loadNativeNotify(
                    requireActivity(),
                    binding.flAdplaceholder,
                    binding.shimmerContainer,
                    binding.flAdplaceholder, true
                )
            }
        }
    }

    private fun setUpEvent() {
        binding.imvSwNotify.onDebounceClick {
            if (hasPerNotify) return@onDebounceClick
            PermissionUtils.requestPermissionNotify(requireActivity(), {
                setApplyView()
            }, {
                setApplyView()
            })
        }
    }

    private fun setApplyView() {
        hasPerNotify = PermissionUtils.checkHasPerNotify(requireActivity())
        binding.imvSwNotify.setImageAnyNormal(if (hasPerNotify) R.drawable.ic_switch_on_green else R.drawable.ic_switch_off_gray)
    }

}