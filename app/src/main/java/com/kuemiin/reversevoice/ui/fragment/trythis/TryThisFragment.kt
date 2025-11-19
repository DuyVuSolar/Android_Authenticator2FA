package com.kuemiin.reversevoice.ui.fragment.trythis

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import cn.ezandroid.lib.ezfilter.extra.sticker.Bitmap2025QuizFilter.EFFECT_SING_A_SONG
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.kuemiin.base.extension.show
import com.kuemiin.reversevoice.BaseApplication
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseFragment
import com.kuemiin.reversevoice.databinding.FragmentTryThisBinding
import com.kuemiin.reversevoice.ui.activity.main.dpToPx
import com.kuemiin.reversevoice.ui.fragment.camera.CameraFragment
import com.kuemiin.reversevoice.utils.Constant
import com.kuemiin.reversevoice.utils.MaxUtils
import com.kuemiin.reversevoice.utils.MaxUtils.logFirebaseEvent
import com.kuemiin.reversevoice.utils.MaxUtils.AD_UNIT_ID_NATIVE_COLLAP
import com.kuemiin.reversevoice.utils.MaxUtils.ISSHOWINGNATIVE_BOTTOM
import com.kuemiin.reversevoice.utils.MaxUtils.getBoolean
import com.kuemiin.reversevoice.utils.MaxUtils.logRevAdjust
import com.kuemiin.reversevoice.utils.MaxUtils.logRevCC
import com.kuemiin.reversevoice.utils.MaxUtils.populateUnifiedNativeAdView
import com.kuemiin.reversevoice.utils.PermissionUtils
import com.kuemiin.reversevoice.utils.extension.exhaustive
import com.kuemiin.reversevoice.utils.extension.gone
import com.kuemiin.reversevoice.utils.visible
import com.kuemiin.reversevoice.widgets.ui.FadeInThenOutAnimationEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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

    private fun registerNative() {
        viewLifecycleOwner.lifecycleScope.launch {
            val topJob = async { loadNativeSuspend() }
            val bottomJob = async { loadNativeBottomSuspend() }
            awaitAll(topJob, bottomJob)
        }
    }

    private var currentNativeAd: NativeAd? = null

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun loadNativeSuspend(
        incrementCount: Boolean = true
    ): Unit = suspendCancellableCoroutine { cont ->
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {

            if (!getBoolean(requireActivity(), MaxUtils.ISSHOWINGNATIVE_COLLAP, true)) {
                binding.nativeCollap.gone()
                cont.resume(Unit)
                return@launch
            }

            binding.nativeCollap.show()

            binding.btnCollapsible.apply {
                visibility = View.GONE
                isClickable = false
                isFocusable = false
                isEnabled = false
                setOnTouchListener { _, _ -> false }
            }

            binding.btnCollapsible1.gone()

            binding.includeLayout.adGroup.removeAllViews()
            binding.includeLayout.root.apply {
                isClickable = false
                isFocusable = false
                isEnabled = false
            }

            binding.shimmerContainerNativeCollap.post {
                binding.shimmerContainerNativeCollap.show()
                binding.shimmerContainerNativeCollap.startShimmer()
            }

            val adLoader = AdLoader.Builder(requireActivity(), AD_UNIT_ID_NATIVE_COLLAP)
                .forNativeAd { unifiedNativeAd ->

                    currentNativeAd?.destroy()
                    currentNativeAd = unifiedNativeAd

                    binding.includeLayout.root.show()

                    val nativeAdView = layoutInflater.inflate(
                        R.layout.native_admob_ad_collap_trythis,
                        null
                    ) as NativeAdView

                    populateUnifiedNativeAdView(unifiedNativeAd, nativeAdView)

                    binding.includeLayout.adGroup.apply {
                        removeAllViews()
                        addView(nativeAdView)
                        isClickable = false
                        isFocusable = false
                        isEnabled = false
                        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        setOnTouchListener { _, _ -> false }
                    }

                    binding.shimmerContainerNativeCollap.post {
                        binding.shimmerContainerNativeCollap.stopShimmer()
                        binding.shimmerContainerNativeCollap.setShimmer(null)
                        binding.shimmerContainerNativeCollap.gone()
                        binding.shimmerContainerNativeCollap.isClickable = false
                        binding.shimmerContainerNativeCollap.isFocusable = false
                        binding.shimmerContainerNativeCollap.isEnabled = false
                        binding.shimmerContainerNativeCollap.setOnTouchListener { _, _ -> false }
                    }

                    binding.btnCollapsible1.apply {
                        visibility = View.VISIBLE
                        isClickable = true
                        isFocusable = true
                        isEnabled = true
                        setOnClickListener {
                            binding.includeLayout.adGroup.removeAllViews()
                            binding.includeLayout.root.gone()
                            binding.nativeCollap.gone()
                        }
                    }

                    unifiedNativeAd.setOnPaidEventListener { impressionData ->
                        logRevAdjust(impressionData.valueMicros.toDouble())
                        logRevCC(requireActivity(), "native", impressionData.valueMicros.toDouble())
                    }

                    cont.resume(Unit)
                }
                .withAdListener(object : AdListener() {

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        binding.shimmerContainerNativeCollap.post {
                            binding.shimmerContainerNativeCollap.stopShimmer()
                            binding.shimmerContainerNativeCollap.gone()
                        }
                        binding.nativeCollap.gone()
                        cont.resume(Unit)
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        binding.btnCollapsible.show()
                        binding.btnCollapsible1.show()

                        if (MaxUtils.isCheck(requireActivity())) {
                            if (MaxUtils.count % MaxUtils.getCount(requireActivity()) == 0) {
                                binding.btnCollapsible1.apply {
                                    visibility = View.INVISIBLE
                                    isClickable = false
                                    isFocusable = false
                                    isEnabled = false
                                    setOnTouchListener { _, _ -> false }
                                }
                            } else {
                                binding.btnCollapsible1.updateLayoutParams<ViewGroup.LayoutParams> {
                                    width = dpToPx(requireActivity(), 20)
                                    height = dpToPx(requireActivity(), 20)
                                }
                                binding.btnCollapsible1.apply {
                                    visibility = View.VISIBLE
                                    isClickable = true
                                    isFocusable = true
                                    isEnabled = true
                                }
                            }
                            if (incrementCount) MaxUtils.count++
                        } else {
                            binding.btnCollapsible1.updateLayoutParams<ViewGroup.LayoutParams> {
                                width = dpToPx(requireActivity(), 20)
                                height = dpToPx(requireActivity(), 20)
                            }
                            binding.btnCollapsible1.apply {
                                visibility = View.VISIBLE
                                isClickable = true
                                isFocusable = true
                                isEnabled = true
                            }
                        }

                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        binding.btnCollapsible1.apply {
                            visibility = View.VISIBLE
                            isClickable = true
                            isFocusable = true
                            isEnabled = true
                        }
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
                        .build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    private var currentNativeAdBottom: NativeAd? = null

    private suspend fun loadNativeBottomSuspend() = suspendCancellableCoroutine { cont ->
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {

            if (!getBoolean(requireActivity(), ISSHOWINGNATIVE_BOTTOM, true)) {
                binding.nativeCollapBanner.gone()
                binding.bannerViewBottom.gone()
                cont.resume(Unit)
                return@launch
            }

            binding.nativeCollapBanner.show()
            binding.bannerViewBottom.show()
            binding.btnCollapsibleBanner.gone()
            binding.includeLayoutBanner.adGroup.removeAllViews()
            binding.includeLayoutBannerBottom.adGroup.removeAllViews()

            binding.shimmerContainerNativeCollapBanner.post {
                binding.shimmerContainerNativeCollapBanner.show()
                binding.shimmerContainerNativeCollapBanner.startShimmer()
            }

            val adLoader = AdLoader.Builder(requireActivity(), MaxUtils.AD_UNIT_ID_NATIVE_BOTTOM)
                .forNativeAd { unifiedNativeAd ->
                    currentNativeAdBottom?.destroy()
                    currentNativeAdBottom = unifiedNativeAd

                    binding.btnCollapsibleBanner.show()
                    binding.includeLayoutBanner.root.show()
                    binding.includeLayoutBannerBottom.root.show()

                    binding.btnCollapsibleBanner.setOnClickListener {
                        binding.includeLayoutBanner.adGroup.removeAllViews()
                        binding.includeLayoutBanner.root.gone()
                        binding.nativeCollapBanner.gone()
                    }

                    unifiedNativeAd.setOnPaidEventListener { impressionData ->
                        logRevAdjust(impressionData.valueMicros.toDouble())
                        logRevCC(requireActivity(), "native", impressionData.valueMicros.toDouble())
                    }

                    binding.shimmerContainerNativeCollapBanner.post {
                        binding.shimmerContainerNativeCollapBanner.stopShimmer()
                        binding.shimmerContainerNativeCollapBanner.gone()
                    }

                    val layoutRes = R.layout.native_admob_ad_collap_trythis
                    val nativeAdView = layoutInflater.inflate(layoutRes, null) as NativeAdView
                    populateUnifiedNativeAdView(unifiedNativeAd, nativeAdView)
                    binding.includeLayoutBanner.adGroup.removeAllViews()
                    binding.includeLayoutBanner.adGroup.addView(nativeAdView)

                    val layoutResBanner = R.layout.native_banner_ad_trythis
                    val nativeAdViewBanner =
                        layoutInflater.inflate(layoutResBanner, null) as NativeAdView
                    MaxUtils.populateNativeAdViewBanner(unifiedNativeAd, nativeAdViewBanner)
                    binding.includeLayoutBannerBottom.adGroup.removeAllViews()
                    binding.includeLayoutBannerBottom.adGroup.addView(nativeAdViewBanner)

                    cont.resume(Unit)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        binding.shimmerContainerNativeCollapBanner.post {
                            binding.shimmerContainerNativeCollapBanner.stopShimmer()
                            binding.shimmerContainerNativeCollapBanner.gone()
                        }
                        binding.nativeCollapBanner.gone()
                        binding.bannerViewBottom.gone()
                        cont.resume(Unit)
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
                        .build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    override fun setUp() {
        viewModel.typeEffect = arguments?.getString(Constant.KEY_EXTRA_DATA) ?: return
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
                        if(viewModel.typeEffect == EFFECT_SING_A_SONG){
                            requireContext().logFirebaseEvent("click_sing_along_button_try_this")
                        }else{
                            requireContext().logFirebaseEvent("click_the_voice_button_try_this")
                        }
                        navigateTo(R.id.action_tryThisFragment_to_eventSizeFracameraFragment, CameraFragment.newBundle(viewModel.typeEffect))
                    }

                    TryThisViewModel.Event.OnLoadVideoCompleted -> {
                        binding.imvClickPlayPause.visible(200)
                        animationEffect?.go(!viewModel.appPlayer.isPlaying())
                    }

                    TryThisViewModel.Event.OnClickPlayPause -> {
                        animationEffect?.go(!viewModel.appPlayer.isPlaying())
                    }

                    TryThisViewModel.Event.VideoEnd -> {
                        animationEffect?.go(!viewModel.appPlayer.isPlaying())
                    }
                }.exhaustive
            }
        }
    }

    private fun setUpView() {
        animationEffect = FadeInThenOutAnimationEffect(binding.imvClickPlayPause)
    }

    private fun setUpPlayer() {
        binding.imvBack.postDelayed({
            binding.liveStatePlayer = viewModel.appPlayer.liveState
            viewModel.initVideo(binding.playerViewSmall2)
        }, 200)
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    override fun onDestroyView() {
        viewModel.releaseAppPlayer()
        super.onDestroyView()
    }

    override fun onBackPressed() {

    }
}