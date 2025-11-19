package com.kuemiin.reversevoice.ui.fragment.permission

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseFragment
import com.kuemiin.reversevoice.databinding.FragmentPermissionBinding
import com.kuemiin.reversevoice.model.IntroSplash
import com.kuemiin.reversevoice.ui.activity.intro.IntroViewModel
import com.kuemiin.reversevoice.ui.fragment.camera.CameraFragment
import com.kuemiin.reversevoice.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import com.kuemiin.reversevoice.utils.MaxUtils
import com.kuemiin.reversevoice.utils.MaxUtils.getBoolean
import com.kuemiin.reversevoice.utils.MaxUtils.isCheck
import com.kuemiin.reversevoice.utils.MaxUtils.logFirebaseEvent
import com.kuemiin.reversevoice.utils.PermissionUtils
import com.kuemiin.reversevoice.utils.binding.onDeboundClick
import com.kuemiin.reversevoice.utils.binding.setEnableViewAlpha
import com.kuemiin.reversevoice.utils.binding.setImageAnyNormal
import com.kuemiin.reversevoice.utils.extension.exhaustive
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class PermisionFragment : BaseFragment<FragmentPermissionBinding>() {

    companion object {
        fun newBundle(isRequestCamera: Boolean, effect: String) = bundleOf().apply {
            putString(Constant.KEY_EXTRA_DATA, effect)
            putBoolean(Constant.KEY_EXTRA_BOOLEAN, isRequestCamera)
        }
    }

    private val data = IntroSplash(
        R.string.continue_txt,
        R.drawable.bg_permisson_record,
        0,
        true,
        isPerMicrophone = true
    )

    private val viewModel by viewModels<PermisisonViewModel>()

    override fun getLayoutId(): Int = R.layout.fragment_permission

    override fun setUp() {
        viewModel.effect = arguments?.getString(Constant.KEY_EXTRA_DATA, "") ?: ""
        viewModel.requestCamera.set(
            arguments?.getBoolean(Constant.KEY_EXTRA_BOOLEAN, false) ?: false
        )
        binding.viewmodel = viewModel
        binding.item = data
        setUpEvent()
        setUpApplyView()

        MaxUtils.loadNativePermission(
            requireActivity(),
            binding.flAdplaceholder,
            binding.shimmerContainer,
            binding.flAdplaceholder
        )
    }

    private fun setUpEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.introEvent.collect { event ->
                    when (event) {
                        PermisisonViewModel.IntroEvent.OnClickContinueReverse -> {
                            setFragmentResult("key_show_reverse", bundleOf("bundleKey" to true))
                            navigateUp()
                        }

                        PermisisonViewModel.IntroEvent.OnClickContinueTryThis -> {
                            navigateTo(
                                R.id.action_permisionFragment_to_tryThisFragment,
                                CameraFragment.newBundle(viewModel.effect)
                            )
                        }

                        PermisisonViewModel.IntroEvent.OnClickContinue -> {
                            if (viewModel.requestCamera.get()) {
                                MaxUtils.showAdsWithCustomCount(requireActivity(),
                                    object : MaxUtils.NextScreenListener {
                                        override fun nextScreen() {
                                            viewModel.onClickContinueTryThis()
                                        }
                                    })
                            } else {
                                MaxUtils.showAdsWithCustomCount(requireActivity(),
                                    object : MaxUtils.NextScreenListener {
                                        override fun nextScreen() {
                                            viewModel.onClickContinueReverse()
                                        }
                                    })
                            }
                        }

                        PermisisonViewModel.IntroEvent.OnClickBackIntro -> {

                        }

                        PermisisonViewModel.IntroEvent.OnClickPerCamera -> {
                            PermissionUtils.requestPermissionCamera(requireActivity(), {
                                setUpApplyView()
                            }, {
                                setUpApplyView()
                            })
                        }

                        PermisisonViewModel.IntroEvent.OnClickPerMicro -> {
                            PermissionUtils.requestPermissionRecord(requireActivity(), false, {
                                setUpApplyView()
                            }, {
                                setUpApplyView()
                            })
                        }
                    }.exhaustive
                }
            }
        }

    }

    private fun setUpApplyView() {
        viewModel.hasAllowCamera.set(
            if (viewModel.requestCamera.get()) PermissionUtils.checkHasPerCamera(
                requireActivity()
            ) else true
        )
        viewModel.hasAllowRecord.set(PermissionUtils.checkHasPerRecord(requireActivity()))
    }

    override fun onResume() {
        super.onResume()
        setUpApplyView()
    }

}