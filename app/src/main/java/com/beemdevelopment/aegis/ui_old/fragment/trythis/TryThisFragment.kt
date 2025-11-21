package com.beemdevelopment.aegis.ui_old.fragment.trythis

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseFragment
import com.beemdevelopment.aegis.databinding.FragmentTryThisBinding
import com.beemdevelopment.aegis.utils.Constant
import com.beemdevelopment.aegis.utils.MaxUtils
import com.beemdevelopment.aegis.utils.PermissionUtils
import com.beemdevelopment.aegis.utils.extension.exhaustive
import com.beemdevelopment.aegis.utils.visible
import com.beemdevelopment.aegis.widgets.ui.FadeInThenOutAnimationEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("DEPRECATION", "IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class TryThisFragment : BaseFragment<FragmentTryThisBinding>() {

    companion object {
        fun newBundle(effect: String) = bundleOf().apply {
            putString(Constant.KEY_EXTRA_DATA, effect)
        }
    }

    private var animationEffect: FadeInThenOutAnimationEffect? = null


    private val viewModel: TryThisViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.fragment_try_this

    override fun setUp() {
        binding.viewmodel = viewModel
        viewModel.activity = requireActivity()
//        registerNative()
        setUpEvent()
        setUpView()
        setUpPlayer()
    }

    private fun setUpEvent() {
        lifecycleScope.launchWhenStarted {
            viewModel.homeEvent.collect { event ->
                when (event) {
                    TryThisViewModel.Event.OnClickBack -> {
                        MaxUtils.showAdsWithCustomCount(requireActivity(),
                            object : MaxUtils.NextScreenListener {
                                override fun nextScreen() {
                                    viewModel.OnNavigateUp()
                                }
                            })
                    }

                    TryThisViewModel.Event.OnNavigateUp -> {
                        navigateUp()
                    }

                    TryThisViewModel.Event.OnClickTryThis -> {
                        val handle = {
                            PermissionUtils.setIsPermissionCamera(requireActivity(), false)
                            PermissionUtils.checkHasPerCameraAndRecord(requireActivity())
                            MaxUtils.showAdsWithCustomCount(requireActivity(),
                                object : MaxUtils.NextScreenListener {
                                    override fun nextScreen() {
                                        viewModel.NavigateOnClickTryThis()
                                    }
                                })
                        }
                        PermissionUtils.checkPermissionFull(requireActivity(), {
                            handle()
                        }, {
                            handle()
                        }, false)
                    }

                    TryThisViewModel.Event.NavigateOnClickTryThis -> {
                    }

                    TryThisViewModel.Event.OnLoadVideoCompleted -> {
                        binding.imvClickPlayPause.visible(200)
                    }

                    TryThisViewModel.Event.OnClickPlayPause -> {
                    }

                    TryThisViewModel.Event.VideoEnd -> {
                    }
                }.exhaustive
            }
        }
    }

    private fun setUpView() {
        animationEffect = FadeInThenOutAnimationEffect(binding.imvClickPlayPause)
    }

    private fun setUpPlayer() {

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onBackPressed() {

    }
}