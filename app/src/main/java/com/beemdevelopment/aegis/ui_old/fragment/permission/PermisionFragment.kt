package com.beemdevelopment.aegis.ui_old.fragment.permission

import android.annotation.SuppressLint
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseFragment
import com.beemdevelopment.aegis.databinding.FragmentPermissionBinding
import com.beemdevelopment.aegis.model.IntroSplash
import com.beemdevelopment.aegis.ui_old.fragment.camera.CameraFragment
import com.beemdevelopment.aegis.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import com.beemdevelopment.aegis.utils.MaxUtils
import com.beemdevelopment.aegis.utils.PermissionUtils
import com.beemdevelopment.aegis.utils.extension.exhaustive
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