@file:Suppress("IMPLICIT_CAST_TO_ANY")

package com.kuemiin.reversevoice.ui.fragment.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lhd.visualizer_record.VisualizerRecorder
import com.kuemiin.reversevoice.databinding.FragmentMainBinding
import com.kuemiin.reversevoice.utils.Constant
import com.kuemiin.reversevoice.utils.extension.exhaustive
import com.kuemiin.reversevoice.utils.extension.toast
import kotlinx.coroutines.launch
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseFragment
import com.kuemiin.reversevoice.base.RecordingsAdapter
import com.kuemiin.reversevoice.ui.dialog.delete.DeleteAudioDialog
import com.kuemiin.reversevoice.utils.CommonUtils
import com.kuemiin.reversevoice.utils.FileUtils
import com.kuemiin.reversevoice.utils.MaxUtils
import com.kuemiin.reversevoice.utils.MaxUtils.AD_UNIT_ID_NATIVE_COLLAP
import com.kuemiin.reversevoice.utils.MaxUtils.logRevAdjust
import com.kuemiin.reversevoice.utils.MaxUtils.logRevCC
import com.kuemiin.reversevoice.utils.MaxUtils.populateUnifiedNativeAdView
import com.kuemiin.reversevoice.utils.PermissionUtils
import com.kuemiin.reversevoice.utils.extension.gone
import com.kuemiin.reversevoice.utils.extension.observer
import com.kuemiin.reversevoice.utils.extension.visible
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.GridLayoutManager
import cn.ezandroid.lib.ezfilter.extra.sticker.Bitmap2025QuizFilter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.kuemiin.base.extension.show
import com.kuemiin.reversevoice.model.MyGalleryModel
import com.kuemiin.reversevoice.ui.activity.main.dpToPx
import com.kuemiin.reversevoice.ui.fragment.permission.PermisionFragment
import com.kuemiin.reversevoice.ui.fragment.preview_gallery.PreviewGalleryFragment
import com.kuemiin.reversevoice.ui.fragment.trythis.TryThisFragment
import com.kuemiin.reversevoice.utils.MaxUtils.ISSHOWINGNATIVE_BOTTOM
import com.kuemiin.reversevoice.utils.MaxUtils.getBoolean
import com.kuemiin.reversevoice.utils.MaxUtils.logFirebaseEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>() {

    companion object {
        var isDownloadedRecent = false
    }

    private var wl: PowerManager.WakeLock? = null

    private val viewModel by viewModels<MainFragmentViewModel>()
    private var dialogDelete: DeleteAudioDialog? = null

    private val adapterRecording by lazy {
        RecordingsAdapter(R.layout.item_recording).apply {
            viewmodel = viewModel
            liveState = viewModel.appPlayer.liveState
            isPlayReverse = viewModel.appPlayer.isPlayReverse
            pathPlaying = viewModel.appPlayer.pathPlaying
        }
    }

    private val mAdapterVideo by lazy {
        GalleryAdapter(
            layoutInflater,
            R.layout.item_video_record,
            R.layout.item_video_record//layout native

        ).apply {
            viewmodel = viewModel
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_main

    override fun setUp() {
        hideKeyboard()
        viewModel.activity = requireActivity()
        binding.liveStatePlayer = viewModel.appPlayer.liveState
        binding.isPlayReverse = viewModel.appPlayer.isPlayReverse
        binding.viewmodel = viewModel
        setUpAdapter()
        initEvent()
        registerNative()
    }

    //region native ad
    private var reloadJob: Job? = null

    private fun registerNativeMain() {
        reloadJob?.cancel()
        Log.e("NativeReload", "=== registerNativeMain START ===")

        if (MaxUtils.isCheck(requireActivity())) {
            reloadJob = viewLifecycleOwner.lifecycleScope.launch {

                while (isActive) {
                    try {
                        Log.e(
                            "NativeReload",
                            "Starting parallel load at ${System.currentTimeMillis()}"
                        )
                        delay(1000L * MaxUtils.getTimeReloadCollap(requireActivity()))

                        val topJob = async {
                            Log.e("NativeReload", "Top native load start")
                            loadNativeSuspend()
                            Log.e("NativeReload", "Top native load end")
                        }

                        val bottomJob = async {
                            Log.e("NativeReload", "Bottom native load start")
                            loadNativeBottomSuspend()
                            Log.e("NativeReload", "Bottom native load end")
                        }

                        awaitAll(topJob, bottomJob)
                        Log.e("NativeReload", "Both top & bottom finished, waiting next reload")

//                        delay(1000L * MaxUtils.getTimeReloadCollap(requireActivity()))
                    } catch (e: CancellationException) {
                        Log.e("NativeReload", "Reload job cancelled")
                        throw e
                    } catch (e: Exception) {
                        Log.e("NativeReload", "Exception in reload loop: $e")
                        delay(1000L * MaxUtils.getTimeReloadCollap(requireActivity()))
                    }
                }
            }
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                Log.e("NativeReload", "Load without repeat")
                val topJob = async {
                    Log.e("NativeReload", "Top native load start")
                    loadNativeSuspend()
                    Log.e("NativeReload", "Top native load end")
                }
                val bottomJob = async {
                    Log.e("NativeReload", "Bottom native load start")
                    loadNativeBottomSuspend()
                    Log.e("NativeReload", "Bottom native load end")
                }
                awaitAll(topJob, bottomJob)
            }
        }
    }

    private fun registerNative() {
        Log.e("NativeReload", "=== registerNative called ===")
        viewLifecycleOwner.lifecycleScope.launch {
            reloadJob?.cancelAndJoin()
            Log.e("NativeReload", "Previous reloadJob cancelled")

            val topJob = async {
                Log.e("NativeReload", "Top native load start (manual)")
                loadNativeSuspend()
                Log.e("NativeReload", "Top native load end (manual)")
            }

            val bottomJob = async {
                Log.e("NativeReload", "Bottom native load start (manual)")
                loadNativeBottomSuspend()
                Log.e("NativeReload", "Bottom native load end (manual)")
            }

            awaitAll(topJob, bottomJob)
            Log.e("NativeReload", "Manual load finished, restarting auto reload")
            registerNativeMain()
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
                        R.layout.native_admob_ad_collap,
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

                            if (MaxUtils.isCheck(requireActivity())) {
                                reloadJob?.cancel()
                                registerNativeMain()
                            }
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

                    val layoutRes = R.layout.native_admob_ad_collap
                    val nativeAdView = layoutInflater.inflate(layoutRes, null) as NativeAdView
                    populateUnifiedNativeAdView(unifiedNativeAd, nativeAdView)
                    binding.includeLayoutBanner.adGroup.removeAllViews()
                    binding.includeLayoutBanner.adGroup.addView(nativeAdView)

                    val layoutResBanner = R.layout.native_banner_ad
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

    //    @SuppressLint("ClickableViewAccessibility")
//    private suspend fun loadNativeSuspend(
//        incrementCount: Boolean = true
//    ): Unit = suspendCancellableCoroutine { cont ->
//        if (!getBoolean(requireActivity(), MaxUtils.ISSHOWINGNATIVE_COLLAP, true)) {
//            binding.nativeCollap.gone()
//            cont.resume(Unit)
//            return@suspendCancellableCoroutine
//        }
//
//        binding.nativeCollap.show()
//
//        binding.btnCollapsible.apply {
//            visibility = View.GONE
//            isClickable = false
//            isFocusable = false
//            isEnabled = false
//            setOnTouchListener { _, _ -> false }
//        }
//
//        binding.btnCollapsible1.gone()
//
//        binding.includeLayout.adGroup.apply {
//            removeAllViews()
//            isClickable = false
//            isFocusable = false
//            isEnabled = false
//            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
//            setOnTouchListener { _, _ -> false }
//        }
//        binding.includeLayout.root.apply {
//            isClickable = false
//            isFocusable = false
//            isEnabled = false
//        }
//
//        binding.shimmerContainerNativeCollap.post {
//            binding.shimmerContainerNativeCollap.show()
//            binding.shimmerContainerNativeCollap.startShimmer()
//        }
//
//        val adLoader = AdLoader.Builder(requireActivity(), AD_UNIT_ID_NATIVE_COLLAP)
//            .forNativeAd { unifiedNativeAd ->
//                currentNativeAd?.destroy()
//                currentNativeAd = unifiedNativeAd
//
//                binding.includeLayout.root.show()
//
//                binding.btnCollapsible1.setOnClickListener {
//                    binding.includeLayout.adGroup.removeAllViews()
//                    binding.includeLayout.root.gone()
//                    binding.nativeCollap.gone()
//                }
//
//                unifiedNativeAd.setOnPaidEventListener { impressionData ->
//                    logRevAdjust(impressionData.valueMicros.toDouble())
//                    logRevCC(requireActivity(), "native", impressionData.valueMicros.toDouble())
//                }
//
//                binding.shimmerContainerNativeCollap.post {
//                    binding.shimmerContainerNativeCollap.stopShimmer()
//                    binding.shimmerContainerNativeCollap.setShimmer(null)
//                    binding.shimmerContainerNativeCollap.visibility = View.GONE
//                    binding.shimmerContainerNativeCollap.isClickable = false
//                    binding.shimmerContainerNativeCollap.isFocusable = false
//                    binding.shimmerContainerNativeCollap.isEnabled = false
//                    binding.shimmerContainerNativeCollap.setOnTouchListener { _, _ -> false }
//                }
//
//                val layoutRes = R.layout.native_admob_ad_collap
//                val nativeAdView = layoutInflater.inflate(layoutRes, null) as NativeAdView
//                populateUnifiedNativeAdView(unifiedNativeAd, nativeAdView)
//
//                nativeAdView.apply {
//                    isClickable = false
//                    isFocusable = false
//                    isEnabled = false
//                    setOnTouchListener { _, _ -> false }
//                }
//
//                binding.includeLayout.adGroup.apply {
//                    removeAllViews()
//                    isClickable = false
//                    isFocusable = false
//                    isEnabled = false
//                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
//                    setOnTouchListener { _, _ -> false }
//                    addView(nativeAdView)
//                }
//
//                binding.btnCollapsible1.apply {
//                    visibility = View.VISIBLE
//                    isClickable = true
//                    isFocusable = true
//                }
//
//                cont.resume(Unit)
//            }
//            .withAdListener(object : AdListener() {
//                override fun onAdFailedToLoad(error: LoadAdError) {
//                    binding.shimmerContainerNativeCollap.post {
//                        binding.shimmerContainerNativeCollap.stopShimmer()
//                        binding.shimmerContainerNativeCollap.gone()
//                    }
//                    binding.nativeCollap.gone()
//                    cont.resume(Unit)
//                }
//
//                override fun onAdLoaded() {
//                    super.onAdLoaded()
//                    binding.btnCollapsible.show()
//                    binding.btnCollapsible1.show()
//                    Log.e("AdLoader", "Collap onAdLoaded: Native Loaded")
//                    Log.e(
//                        "AdLoader",
//                        "Collap onAdLoaded: Native Loaded count ${MaxUtils.count}"
//                    )
//                    if (MaxUtils.count % 3 == 0) {
//                        Log.e("AdLoader", "Collap onAdLoaded: Native Loaded show close 1dp")
//
//                        binding.btnCollapsible1.visibility = View.INVISIBLE
//                        binding.btnCollapsible1.apply {
//                            isClickable = false
//                            isFocusable = false
//                            isEnabled = false
//                            setOnTouchListener { _, _ -> false }
//                        }
//                    } else {
//                        Log.e("AdLoader", "Collap onAdLoaded: Native Loaded show close 20dp")
//
//                        binding.btnCollapsible1.updateLayoutParams<ViewGroup.LayoutParams> {
//                            width = dpToPx(requireActivity(), 20)
//                            height = dpToPx(requireActivity(), 20)
//                        }
//                    }
//
//                    if (incrementCount) MaxUtils.count++
//                    Log.e(
//                        "AdLoader",
//                        "Collap onAdLoaded: Native Loaded count after add ${MaxUtils.count}"
//                    )
//                }
//
//                override fun onAdClicked() {
//                    super.onAdClicked()
//                    Log.e("AdLoader", "Collap onAdClicked: Native Clicked")
//
//                    viewLifecycleOwner.lifecycleScope.launch {
//                        delay(1)
//                        loadNativeSuspend(false)
//                    }
//                }
//            })
//            .withNativeAdOptions(
//                NativeAdOptions.Builder()
//                    .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
//                    .build()
//            )
//            .build()
//
//        adLoader.loadAd(AdRequest.Builder().build())
//    }
    //endregion

    private fun setUpAdapter() {
        binding.rcvRecording.adapter = adapterRecording

        val linearLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.rcvItemsVideos.setHasFixedSize(true)
        binding.rcvItemsVideos.adapter = mAdapterVideo
        binding.rcvItemsVideos.layoutManager = linearLayoutManager
    }

    @SuppressLint("NotifyDataSetChanged", "InvalidWakeLockTag")
    private fun initEvent() {
        setFragmentResultListener("key_show_reverse") { _, bundle ->
            val result = bundle.getBoolean("bundleKey")
            if (result) viewModel.isReversing.set(true)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mainEvent.collect { event ->
                    when (event) {
                        //region tab home
                        MainFragmentViewModel.MainEvent.BackToHome -> {
                            MaxUtils.showAdsWithCustomCount(
                                requireActivity(),
                                object : MaxUtils.NextScreenListener {
                                    override fun nextScreen() {
                                        viewModel.isReversing.set(false)
                                    }
                                })
                        }
                        MainFragmentViewModel.MainEvent.OnClickBottom -> {
                            MaxUtils.showAdsWithCustomCount(
                                requireActivity(),
                                object : MaxUtils.NextScreenListener {
                                    override fun nextScreen() {
                                        registerNative()
                                    }
                                })
                            when (viewModel.typeBottomSelected.get()) {
                                Constant.MAIN_TYPE_HOME -> {
                                    requireActivity().logFirebaseEvent("click_home_bottom")
                                }

                                Constant.MAIN_TYPE_RECORDINGS -> {
                                    requireActivity().logFirebaseEvent("click_recording_bottom")
                                }

                                else -> {
                                    requireActivity().logFirebaseEvent("click_settings_bottom")
                                }
                            }
                        }

                        MainFragmentViewModel.MainEvent.NavigateOnClickReverse -> {
                            viewModel.isReversing.set(true)
                        }

                        MainFragmentViewModel.MainEvent.OnClickReverse -> {
                            requireContext().logFirebaseEvent("click_reverse_singing")

                            if (PermissionUtils.checkHasPerRecord(requireActivity())) {
                                MaxUtils.showAdsWithCustomCount(
                                    requireActivity(),
                                    object : MaxUtils.NextScreenListener {
                                        override fun nextScreen() {
                                            viewModel.NavigateOnClickReverse()
                                        }
                                    })
                            } else {
                                navigateTo(
                                    R.id.action_mainFragment_to_permisionFragment,
                                    PermisionFragment.newBundle(false, "")
                                )
                            }
                        }

                        MainFragmentViewModel.MainEvent.OnClickSingAlong -> {
                            requireContext().logFirebaseEvent("click_sing_along")

                            if (PermissionUtils.checkHasPerCameraAndRecord(requireActivity())) {
                                MaxUtils.showAdsWithCustomCount(
                                    requireActivity(),
                                    object : MaxUtils.NextScreenListener {
                                        override fun nextScreen() {
                                            viewModel.NavigateTryThis()
                                        }
                                    })
                            } else {
                                navigateTo(
                                    R.id.action_mainFragment_to_permisionFragment,
                                    PermisionFragment.newBundle(
                                        true,
                                        Bitmap2025QuizFilter.EFFECT_SING_A_SONG
                                    )
                                )
                            }
                        }

                        MainFragmentViewModel.MainEvent.NavigateTryThis -> {
                            navigateTo(
                                R.id.action_mainFragment_to_tryThisFragment,
                                TryThisFragment.newBundle(Bitmap2025QuizFilter.EFFECT_SING_A_SONG)
                            )
                        }

                        MainFragmentViewModel.MainEvent.NavigateOnClickTiktokVoice -> {
                            navigateTo(
                                R.id.action_mainFragment_to_tryThisFragment,
                                TryThisFragment.newBundle(Bitmap2025QuizFilter.EFFECT_TIKTOK_VOICE)
                            )
                        }
                        MainFragmentViewModel.MainEvent.OnClickTiktokVoice -> {
                            requireContext().logFirebaseEvent("click_the_voice")
                            if (PermissionUtils.checkHasPerCameraAndRecord(requireActivity())) {
                                MaxUtils.showAdsWithCustomCount(
                                    requireActivity(),
                                    object : MaxUtils.NextScreenListener {
                                        override fun nextScreen() {
                                            viewModel.NavigateOnClickTiktokVoice()
                                        }
                                    })
                            } else {
                                navigateTo(
                                    R.id.action_mainFragment_to_permisionFragment,
                                    PermisionFragment.newBundle(
                                        true,
                                        Bitmap2025QuizFilter.EFFECT_TIKTOK_VOICE
                                    )
                                )
                            }
                        }

                        MainFragmentViewModel.MainEvent.OnClickStartRecord -> {
                            requireActivity().logFirebaseEvent("click_start_record_home")

                            PermissionUtils.requestPermissionRecord(requireActivity(), true, {
                                viewModel.onClickStartRecordAfterCheckPermission()
                            }, {

                            })
                        }

                        MainFragmentViewModel.MainEvent.OnClickStopRecord -> {
                            requireActivity().logFirebaseEvent("click_stop_record_home")

                            if (viewModel.liveStateRecord.value != VisualizerRecorder.STATE_RECORD_IDLE) {
                                hasPausedRecord = true
                                viewModel.pauseRecord()
                                viewModel.releaseRecord()
                                viewModel.createNewReverseAudio()

                                MaxUtils.showAdsWithCustomCount(
                                    requireActivity(),
                                    object : MaxUtils.NextScreenListener {
                                        override fun nextScreen() {
                                            if (MaxUtils.isCheck(requireActivity())) {
                                                registerNative()
                                            } else {
                                            }
                                        }
                                    })
                            } else {
                            }
                        }

                        MainFragmentViewModel.MainEvent.OnClickPlayRecorded -> {
                            requireActivity().logFirebaseEvent("click_play_recorded_home")
                        }

                        MainFragmentViewModel.MainEvent.OnClickPlayReverse -> {
                            requireActivity().logFirebaseEvent("click_play_reverse_home")
                        }

                        MainFragmentViewModel.MainEvent.ShowLoading -> {
                            if (viewModel.isLoading.get()) {
                                binding.clLoading.visible()
                            } else {
                                binding.clLoading.gone()
                            }
                        }
                        //endregion tab home

                        //region tab recording
                        is MainFragmentViewModel.MainEvent.EndAudioTabRecord -> {
                            if (MaxUtils.isCheck(requireActivity())) {
                                registerNative()
                            } else {
                            }
                        }

                        is MainFragmentViewModel.MainEvent.PauseAudioTabRecord -> {
                            if (MaxUtils.isCheck(requireActivity())) {
                                registerNative()
                            } else {
                            }
                        }

                        is MainFragmentViewModel.MainEvent.NotifyAdapterRecording -> {
                            adapterRecording.submitCategories(viewModel.listAudio)
                        }

                        is MainFragmentViewModel.MainEvent.NavigateOnClickItemVideo -> {
                            navigateTo(
                                R.id.action_mainFragment_to_previewGalleryFragment,
                                PreviewGalleryFragment.newBundle(event.item)
                            )
                        }

                        is MainFragmentViewModel.MainEvent.OnClickItemVideo -> {
                            requireActivity().logFirebaseEvent("click_my_file_video_item")
                            MaxUtils.showAdsWithCustomCount(requireActivity(),
                                object : MaxUtils.NextScreenListener {
                                    override fun nextScreen() {
                                        viewModel.NavigateOnClickItemGallery(event.item)
                                    }
                                })
                        }

                        is MainFragmentViewModel.MainEvent.OnHandleDelete -> {
                            requireActivity().logFirebaseEvent("click_delete_audio_recording")
                            if (event.list.isEmpty()) return@collect
                            else {
                            }
                            dialogDelete?.dismiss()
                            dialogDelete?.cancel()
                            dialogDelete = null
                            dialogDelete = DeleteAudioDialog(requireContext())
                            dialogDelete?.setUpData {
                                FileUtils.deleteFileInternalAudio(event.list.first())
                                viewModel.getListAudio()
                            }
                            dialogDelete?.show()
                        }

                        is MainFragmentViewModel.MainEvent.OnHandleShare -> {
                            requireActivity().logFirebaseEvent("click_share_audio_recording")

                            FileUtils.shareMultiplePath(
                                event.list.map { it.path },
                                requireActivity()
                            )
                        }
                        //endregion audio recording

                        //region tab settings
                        MainFragmentViewModel.MainEvent.OnClickShare -> {
                            requireActivity().logFirebaseEvent("click_share_app_settings")

                            CommonUtils.shareApp(requireActivity())
                        }

                        MainFragmentViewModel.MainEvent.OnClickPolicy -> {
                            requireActivity().logFirebaseEvent("click_privacy_policy_settings")

                            var url =
                                "https://docs.google.com/document/d/1HZxtyU6XqHdzLIDLERUNuo7RGuVY5blPlfspf7OMft0/edit"
                            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                url = "http://$url"
                            }

                            val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                            startActivity(browserIntent)
                        }

                        MainFragmentViewModel.MainEvent.OnClickTermOfUse -> {

                        }

                        MainFragmentViewModel.MainEvent.NotifyAdapterVideo -> {
                            binding.rcvItemsVideos.post {
                                binding.clEmpty.visibility =
                                    if (viewModel.isEmptyVideo.get() && !viewModel.isLoading.get()) View.VISIBLE else View.GONE
                                val list = arrayListOf<MyGalleryModel>()
                                viewModel.listCreation.forEach {
                                    val demo = it.clone()
                                    list.add(demo)
                                }

                                binding.rcvItemsVideos.post {
                                    mAdapterVideo.setData(list)
                                    if (!viewModel.isEmptyVideo.get()) binding.rcvItemsVideos.visible()
                                    binding.rcvItemsVideos.invalidate()
                                }
                            }
                        }
                        //endregion tab settings
                    }.exhaustive
                }
            }
        }

        observer(viewModel.store.playBackSpeed.asLiveData()) {
            val speed = it!!
            viewModel.playBackSpeed.set(speed)
            binding.sbSpeed.progress = (speed * 10f).toInt()
            viewModel.appPlayer.setSpeedAndPitch(
                viewModel.playBackSpeed.get(),
                viewModel.pitchShift.get()
            )
        }

        observer(viewModel.store.pitchShift.asLiveData()) {
            val pitch = it!!
            viewModel.pitchShift.set(pitch)
            binding.sbPitch.progress = (pitch * 10f).toInt()
            viewModel.appPlayer.setSpeedAndPitch(
                viewModel.playBackSpeed.get(),
                viewModel.pitchShift.get()
            )
        }

        observer(viewModel.store.loopPlayback.asLiveData()) {
            viewModel.loopPlayback.set(it!!)
        }

        binding.sbSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                byUser: Boolean
            ) {
                if (byUser) {
                    val progress = (p0?.progress?.toFloat() ?: 10f) / 10f
                    viewModel.playBackSpeed.set(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                viewModel.onChangeSpeed((p0?.progress?.toFloat() ?: 10f) / 10f)
            }
        })

        binding.sbPitch.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                byUser: Boolean
            ) {
                if (byUser) {
                    val progress = (p0?.progress?.toFloat() ?: 10f) / 10f
                    viewModel.pitchShift.set(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                viewModel.onChangePitchShift((p0?.progress?.toFloat() ?: 10f) / 10f)
            }
        })

        val pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag")
        wl?.acquire(10 * 60 * 1000L /*10 minutes*/)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    //region LIFECYCLER

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (viewModel.isRecording.get()) {
            return
        }

        if (viewModel.isSelectMore.get()) {
            viewModel.onClickCancelMore()
            return
        }

        if (doubleBackToExitPressedOnce) {
            requireActivity().finish()
            return
        }
        doubleBackToExitPressedOnce = true
        toast("Tap again to exit")
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        wl?.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reloadJob?.cancel()
    }

    private var hasPausedRecord = false
    override fun onPause() {
        super.onPause()
        if (!hasPausedRecord) viewModel.pauseRecord()
        viewModel.pauseAudio()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkListVideo()
    }

    //endregion
}