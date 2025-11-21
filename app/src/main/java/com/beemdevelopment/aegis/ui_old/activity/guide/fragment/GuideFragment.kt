package com.beemdevelopment.aegis.ui_old.activity.guide.fragment

import android.view.LayoutInflater
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseFragment
import com.beemdevelopment.aegis.databinding.FragmentGuideBinding
import com.beemdevelopment.aegis.model.IntroSplash
import com.beemdevelopment.aegis.ui_old.activity.guide.GuideViewModel
import com.beemdevelopment.aegis.utils.MaxUtils
import com.beemdevelopment.aegis.utils.PermissionUtils
import com.beemdevelopment.aegis.utils.binding.onDeboundClick
import com.beemdevelopment.aegis.utils.binding.setImageAnyNormal
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
        binding.imvSwNotify.onDeboundClick {
            if (hasPerNotify) return@onDeboundClick
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