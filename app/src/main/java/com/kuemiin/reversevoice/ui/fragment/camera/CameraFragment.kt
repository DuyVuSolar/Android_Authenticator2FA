package com.kuemiin.reversevoice.ui.fragment.camera


import android.annotation.SuppressLint
import android.content.Context.CAMERA_SERVICE
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.camera.core.CameraSelector
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import cn.ezandroid.lib.ezfilter.EZFilter
import cn.ezandroid.lib.ezfilter.camera.CameraBuilder
import cn.ezandroid.lib.ezfilter.core.RenderPipeline
import cn.ezandroid.lib.ezfilter.core.environment.FitViewHelper
import cn.ezandroid.lib.ezfilter.extra.sticker.Bitmap2025QuizFilter.EFFECT_SING_A_SONG
import cn.ezandroid.lib.ezfilter.extra.sticker.Bitmap2025QuizFilter.EFFECT_TIKTOK_VOICE
import cn.ezandroid.lib.ezfilter.media.record.IRecordListener
import cn.ezandroid.lib.ezfilter.media.record.RecordableRender
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.kuemiin.base.extension.show
import com.kuemiin.reversevoice.BaseApplication.Companion.cameraFacing
import com.kuemiin.reversevoice.BaseApplication.Companion.effectFilter
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseFragment
import com.kuemiin.reversevoice.databinding.FragmentCameraBinding
import com.kuemiin.reversevoice.model.EncoderModel
import com.kuemiin.reversevoice.receiver.NetworkStateMonitor
import com.kuemiin.reversevoice.ui.activity.main.MainActivity
import com.kuemiin.reversevoice.ui.activity.main.dpToPx
import com.kuemiin.reversevoice.ui.fragment.camera.CameraViewModel.Companion.TYPE_TIMER_ZERO
import com.kuemiin.reversevoice.ui.fragment.camera.render.CongratRender
import com.kuemiin.reversevoice.ui.fragment.camera.render.DegreeRender
import com.kuemiin.reversevoice.ui.fragment.camera.render.QuestionQuizRender
import com.kuemiin.reversevoice.ui.fragment.camera.render.TiktokVoiceRender
import com.kuemiin.reversevoice.ui.fragment.camera.render.Timer321Render
import com.kuemiin.reversevoice.ui.fragment.proceessing.ProcessingFragment
import com.kuemiin.reversevoice.utils.Constant
import com.kuemiin.reversevoice.utils.Constant.timeRecord
import com.kuemiin.reversevoice.utils.FileUtils
import com.kuemiin.reversevoice.utils.MaxUtils
import com.kuemiin.reversevoice.utils.MaxUtils.AD_UNIT_ID_NATIVE_COLLAP
import com.kuemiin.reversevoice.utils.MaxUtils.ISSHOWINGNATIVE_BOTTOM
import com.kuemiin.reversevoice.utils.MaxUtils.getBoolean
import com.kuemiin.reversevoice.utils.MaxUtils.logRevAdjust
import com.kuemiin.reversevoice.utils.MaxUtils.logRevCC
import com.kuemiin.reversevoice.utils.MaxUtils.populateUnifiedNativeAdView
import com.kuemiin.reversevoice.utils.MaxUtils.logFirebaseEvent
import com.kuemiin.reversevoice.utils.NetworkUtils
import com.kuemiin.reversevoice.utils.PermissionUtils
import com.kuemiin.reversevoice.utils.extension.exhaustive
import com.kuemiin.reversevoice.utils.extension.gone
import com.kuemiin.reversevoice.utils.extension.observer
import com.kuemiin.reversevoice.utils.extension.toast
import com.kuemiin.reversevoice.utils.loge
import com.kuemiin.reversevoice.utils.player.AudioHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.system.exitProcess

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("DEPRECATION")
@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>() {

    companion object {
        fun newBundle(effect: String) = bundleOf().apply {
            putString(Constant.KEY_EXTRA_DATA, effect)
        }
    }

    private val viewModel: CameraViewModel by viewModels()

    private var hasCountTimer = false
    private var hasSetRandomWord = false
    private var audioHelper: AudioHelper? = null
    private var isOpenCamera: Boolean = false
    private var filemp4: File? = null
    private var mCameraDevice: CameraDevice? = null
    private var mRenderPipeline: RenderPipeline? = null
    private var mSupportRecord: RecordableRender? = null

    override fun isDarkTheme(): Boolean = false

    override fun getLayoutId(): Int = R.layout.fragment_camera

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
        if (PermissionUtils.isHasPermissionCamera(requireContext()) && !PermissionUtils.checkHasPerCameraAndRecord(
                requireActivity()
            )
        ) {
            restartApp()
            return
        }
        viewModel.typeFilter = arguments?.getString(Constant.KEY_EXTRA_DATA, EFFECT_SING_A_SONG) ?: EFFECT_SING_A_SONG
        timeRecord = viewModel.timeLimit.get()
        binding.viewmodel = viewModel
        effectFilter = viewModel.typeFilter
        viewModel.listQuiz = viewModel.getListQuizQuestion()
        viewModel.activity = requireActivity()
        setUpEvent()

        viewModel.isFrontCamera.set(cameraFacing == CameraSelector.LENS_FACING_FRONT)
        viewModel.mCurrentCameraId =
            if (viewModel.isFrontCamera.get()) Camera.CameraInfo.CAMERA_FACING_FRONT else Camera.CameraInfo.CAMERA_FACING_BACK

        binding.viewmodel = viewModel
        binding.renderView.setScaleType(FitViewHelper.ScaleType.CENTER_CROP)
        binding.imvHeader.gone()

        binding.renderView.postDelayed({
            if (!isDetached) openCamera(viewModel.mCurrentCameraId)
        }, 100)

        audioHelper = AudioHelper {
            if (it && viewModel.isRecording.get() && viewModel.timeRecoding.get() > 1) {
                try {
                    toast("Saving!")
                    saveRecord()
                } catch (e: Exception) {
                    loge("DUYVD123 --- $e")
                }
            }
        }
        audioHelper?.requestAudio()
        registerNative()
    }

    private fun setUpEvent() {
        viewModel.setViewInternetState(binding.imvNoInternet, binding.imvRestoredInternet)

        lifecycleScope.launchWhenStarted {
            viewModel.cameraEvent.collect { event ->
                when (event) {
                    CameraViewModel.Event.OnClickOpenPermission -> {
                        PermissionUtils.checkPermissionFull(requireActivity(), {
                            if (PermissionUtils.checkHasPerCameraAndRecord(requireActivity())) {
                                restartFragment()
                            } else {
                                PermissionUtils.setIsPermissionCamera(requireActivity(), true)
                            }
                        }, {
                        })
                    }

                    CameraViewModel.Event.OnClickChangeTimeLimit -> {

                    }

                    CameraViewModel.Event.OnClickChangeWordCamera -> {
                        if(hasSetRandomWord && viewModel.typeFilter == EFFECT_SING_A_SONG){
                            hasSetRandomWord = false
                            totalTimeRandom = 0
                            startRenderWord()
                        }else{}

                    }

                    CameraViewModel.Event.OnClickBack -> {
                        if (viewModel.isCountingTimeStart.get()) {
                            viewModel.isCountingTimeStart.set(false)
                            return@collect
                        }

                        if (viewModel.isRecording.get()) {
                            return@collect
                        }
                        binding.clLoading.gone()
                        releaseCamera()
                        viewModel.cameraProvider?.unbindAll()
                        viewModel.backgroundExecutor?.shutdown()
                        viewModel.poseLandmarkerHelper?.clearPoseLandmarker()
                        viewModel.poseLandmarkerHelper = null
                        effectFilter = EFFECT_SING_A_SONG
                        cameraFacing = CameraSelector.LENS_FACING_FRONT

                        MaxUtils.showAdsWithCustomCount(
                            requireActivity(),
                            object : MaxUtils.NextScreenListener {
                                override fun nextScreen() {
                                    viewModel.NavigateUp()
                                }
                            })
                    }

                    CameraViewModel.Event.NavigateUp -> {
                        navigateUp()
                    }

                    is CameraViewModel.Event.OnClickRecord -> {
                        requireContext().logFirebaseEvent("click_video_btn_start")
                        startTimerToRecord(event.isShowToastFullMemory)
                    }

                    is CameraViewModel.Event.NavigateOnShowPreview -> {
                        navigateTo(
                            R.id.action_cameraFragment_to_processingFragment,
                            ProcessingFragment.newBundle(event.encoderModel)
                        )
                    }

                    is CameraViewModel.Event.OnShowPreview -> {
                        viewModel.viewModelScope.launch {
                            try {
                                viewModel.cameraProvider?.unbindAll()
                                viewModel.backgroundExecutor?.shutdown()
                                viewModel.backgroundExecutor?.awaitTermination(
                                    Long.MAX_VALUE,
                                    TimeUnit.NANOSECONDS
                                )
                                viewModel.poseLandmarkerHelper?.clearPoseLandmarker()
                                viewModel.poseLandmarkerHelper = null
                                releaseCamera()
                            } catch (e: Exception) {
                                loge("DUYVD123 --- $e")
                            }
                        }


                        val pathVideo = event.file.absolutePath
                        val encoder = EncoderModel(
                            pathVideo,
                            viewModel.getSizeFromResolution().width,
                            viewModel.getSizeFromResolution().height,
                            viewModel.typeFilter,
                        )
                        MaxUtils.showAdsWithCustomCount(
                            requireActivity(),
                            object : MaxUtils.NextScreenListener {
                                override fun nextScreen() {
                                    viewModel.NavigateOnShowPreview(encoder)
                                }
                            })
                    }

                    CameraViewModel.Event.HandleInitFilter -> {
                        val previewWidth = viewModel.getSizeFromResolution().height
                        val previewHeight = viewModel.getSizeFromResolution().width
                        if (!viewModel.isInitingFilter.get()) {
                            if (mWordsRender == null && viewModel.typeFilter == EFFECT_SING_A_SONG) {
                                val width = previewWidth//width preview
                                val widthTargetQuestion = width * 0.72f
                                mWordsRender = QuestionQuizRender(
                                    requireContext(),
                                    widthTargetQuestion.toInt(),
                                    viewModel.typeFilter
                                )
                                mRenderPipeline?.addFilterRender(mWordsRender)
                            }

                            val width = previewWidth//width preview

                            if (mTiktokVoiceRender1 == null && viewModel.typeFilter == EFFECT_TIKTOK_VOICE) {
                                mTiktokVoiceRender1 = TiktokVoiceRender(
                                    requireContext(),
                                    (width * 0.25f).toInt(),
                                    viewModel.typeFilter
                                )
                                mRenderPipeline?.addFilterRender(mTiktokVoiceRender1)

                                mTiktokVoiceRender2 = TiktokVoiceRender(
                                    requireContext(),
                                    (width * 0.25f).toInt(),
                                    viewModel.typeFilter
                                )
                                mRenderPipeline?.addFilterRender(mTiktokVoiceRender2)

                                mTiktokVoiceRender3 = TiktokVoiceRender(
                                    requireContext(),
                                    (width * 0.25f).toInt(),
                                    viewModel.typeFilter
                                )
                                mRenderPipeline?.addFilterRender(mTiktokVoiceRender3)

                                mDegreeRender = DegreeRender(
                                    requireContext(),
                                    (width * 0.2f).toInt(),
                                    viewModel.typeFilter
                                )

                                mCongratRender = CongratRender(
                                    requireContext(),
                                    (previewHeight),
                                    viewModel.typeFilter
                                )

                                val widthTargetTimer = width * 0.45f
                                mTimer321Render = Timer321Render(
                                    requireContext(),
                                    widthTargetTimer.toInt(),
                                    viewModel.typeFilter
                                )
                                mRenderPipeline?.addFilterRender(mTimer321Render)

                            }


                            xQuestion = previewWidth / 2f
                            yQuestion = previewHeight / 3f
                            mWordsRender?.setPosition(xQuestion.toInt(), yQuestion.toInt())
                            currentWord = Random.nextInt(viewModel.listQuiz.size - 1)
                            mWordsRender?.currentQuiz = currentWord


                            mTimer321Render?.setPosition(
                                xQuestion.toInt(),
                                previewHeight - yQuestion.toInt()
                            )
                            mTimer321Render?.currentQuiz = currentIndexTimer

                            mTiktokVoiceRender1?.setPosition(
                                (xQuestion.toInt() - width * 0.25f).toInt(),
                                yQuestion.toInt()
                            )
                            mTiktokVoiceRender1?.currentQuiz = 0

                            mTiktokVoiceRender2?.setPosition(xQuestion.toInt(), yQuestion.toInt())
                            mTiktokVoiceRender2?.currentQuiz = 0

                            mTiktokVoiceRender3?.setPosition(
                                (xQuestion.toInt() + width * 0.25f).toInt(),
                                yQuestion.toInt()
                            )
                            mTiktokVoiceRender3?.currentQuiz = 0

                            mCongratRender?.setPosition(
                                xQuestion.toInt(),
                                (previewHeight / 1.3).toInt()
                            )
                            mCongratRender?.currentQuiz = 0

                            mDegreeRender?.setPosition(
                                (previewWidth * 0.85f).toInt(),
                                (previewHeight * 0.75f).toInt()
                            )
                            mDegreeRender?.currentQuiz = 0


                        } else {
                        }
                    }

                    CameraViewModel.Event.OnClickCancelWatermark -> {

                    }
                }.exhaustive
            }
        }
    }

    fun checkNetworkFromHome() {
        if (binding.imvNoInternet.isVisible || (binding.imvNoInternet.isGone && !NetworkUtils.isNetworkConnected(
                requireContext()
            ))
        ) {
            observerInternet()
        } else if (NetworkUtils.isNetworkConnected(requireContext()) && binding.imvNoInternet.isVisible) {
            observerInternet()
        }
    }

    private fun observerInternet() {
        observer(NetworkStateMonitor.isConnectNetwork) {
            it?.let {
                if (viewModel.firstNoInternet == null) {
                    viewModel.firstNoInternet = !it
                    if (!it) {
                        viewModel.isStateNoInternet.set(true)
                        viewModel.handleStateViewNetwork()
                    }
                } else {
                    viewModel.isStateNoInternet.set(!it)
                    viewModel.handleStateViewNetwork()
                }

                viewModel.isNoInternet.set(!it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if ((PermissionUtils.isGotoSetting && PermissionUtils.checkHasPerCameraAndRecord(
                requireActivity()
            )) || isOpenCamera
        ) {
            openCamera(viewModel.mCurrentCameraId)
        } else if (isReleasePauseApp) {
            isReleasePauseApp = false
            restartFragment()
        }
//        viewModel.cameraX?.cameraControl?.enableTorch(viewModel.isAllowOnFlash())
        viewModel.isAllowPermission.set(PermissionUtils.checkHasPerCameraAndRecord(requireActivity()))
        if (NetworkUtils.isNetworkConnected(requireContext())) {
            viewModel.hideAllViewStateNetwork()
        }

        checkNetworkFromHome()
    }

    private var isReleasePauseApp = false
    override fun onPause() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
        if (viewModel.isCountingTimeStart.get()) {
            viewModel.isCountingTimeStart.set(false)
        }
        if (viewModel.isRecording.get()) {
            if (viewModel.timeRecoding.get() > 1) {
                saveRecord()
                isReleasePauseApp = true
            } else {
                releaseCamera()
                isReleasePauseApp = true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startTimerToRecord(showToastFullMemory: Boolean) {
        if (viewModel.isRecording.get()) {
            if (viewModel.timeRecoding.get() >= 1) {
                if (showToastFullMemory) {
                    toast("Memory is full")
                }
                stopRecording()
            }
        } else if (viewModel.typeTimer.get() == TYPE_TIMER_ZERO) {
            startRecord(showToastFullMemory)
        } else {
            viewModel.handleTimerToStartRecord {
                requireActivity().runOnUiThread {
                    binding.tvCounting.text = viewModel.timeRemainToStart.toString()
                }
                if (it == 0 && viewModel.isCountingTimeStart.get()) {
                    viewModel.isCountingTimeStart.set(false)
                    viewModel.timeRemainToStart = 0
                    startRecord(showToastFullMemory)
                }
            }
        }
    }

    private fun startRecord(isShowToastFullMemory: Boolean) {
        if (mSupportRecord != null) {
            if (viewModel.isRecording.get()) {
                if (viewModel.timeRecoding.get() >= 1) {
                    if (isShowToastFullMemory) {
                        toast("Memory is full")
                    }
                    stopRecording()
                }
            } else {
                if (NetworkStateMonitor.isOnline(requireContext())) {
                    viewModel.isLoading.set(false)
                    if (FileUtils.checkAvailableStoreToRecordDownload()) {
                        startRecording()
                        viewModel.isShowListFilter.set(false)
                    } else {
                        toast("Memory is full")
                    }

                } else {
                    viewModel.isNoInternet.set(true)
                }
            }
        } else {
            viewModel.isLoading.set(false)
        }
    }

    //region set position filter
    private var mTiktokVoiceRender1: TiktokVoiceRender? = null
    private var mTiktokVoiceRender2: TiktokVoiceRender? = null
    private var mTiktokVoiceRender3: TiktokVoiceRender? = null
    private var mCongratRender: CongratRender? = null

    private var mDegreeRender: DegreeRender? = null
    private var currentIndexDegree = 0

    private var mTimer321Render: Timer321Render? = null
    private val maxCountTimer = 30
    private var currentIndexTimer = maxCountTimer
    private var timeDelayTimer = 100L// 10frame * 100L -> 1second

    private var mWordsRender: QuestionQuizRender? = null
    private var currentWord = 0
    private var timeDelayNextQuestion = 60L

    private var xQuestion: Float = 0f//vi tri X cua question filter
    private var yQuestion: Float = 0f//vi tri Y cua question filter

//endregion

    @SuppressLint("MissingPermission")
    private fun openCamera(id: Int) {
        try {
            if (isDetached) {
                return
            }
            if (PermissionUtils.checkHasPerCameraAndRecord(requireActivity())) {
                PermissionUtils.setIsPermissionCamera(requireActivity(), true)
                viewModel.enableStartRecord.set(false)
                setShowStickmanWhenOpenCamera()
                viewModel.mCameraManager =
                    requireContext().getSystemService(CAMERA_SERVICE) as CameraManager

                try {
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        viewModel.isAllowPermission.set(true)
                        viewModel.isLoading.set(true)
                        try {
                            viewModel.mCameraManager?.openCamera(
                                "" + id,
                                mStateCallback,
                                Handler(Looper.getMainLooper())
                            )
                        } catch (e: CameraAccessException) {
                            restartFragment()
                        }
                    }
                } catch (e: Exception) {
                    loge("DUYVD123 --- $e")
                    try {
                        viewModel.isLoading.set(false)
                        viewModel.isFrontCamera.set(cameraFacing == CameraSelector.LENS_FACING_FRONT)
                        viewModel.cameraProvider?.unbindAll()
                        viewModel.backgroundExecutor?.shutdown()
                        viewModel.backgroundExecutor?.awaitTermination(
                            Long.MAX_VALUE,
                            TimeUnit.NANOSECONDS
                        )
                        viewModel.poseLandmarkerHelper?.clearPoseLandmarker()
                        viewModel.poseLandmarkerHelper = null
                        releaseCamera()
                        binding.renderView.postDelayed(
                            { openCamera(viewModel.mCurrentCameraId) },
                            1000L
                        )
                    } catch (e: InterruptedException) {
                        viewModel.isLoading.set(false)
                        loge("DUYVD123 --- $e")
                    }
                }
            } else {
                viewModel.isAllowPermission.set(false)
            }
        } catch (e: Exception) {
            loge("DUYVD123 --- $e")
        }
    }

    private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) { // Open the camera
            viewModel.cameraOpened = true
            viewModel.setResolution()
            mCameraDevice = camera
            filemp4 =
                File(requireContext().filesDir, System.currentTimeMillis().toString() + ".mp4")
            mRenderPipeline = EZFilter.input(
                mCameraDevice,
                viewModel.getSizeFromResolution(),
                object : CameraBuilder.SurfaceCallback {
                    override fun callback(surfaceTexture: SurfaceTexture?) {
                        viewModel.mSurfaceTexture = surfaceTexture
                        viewModel.createExecutor(requireActivity()) { results ->
                            if (results.results.isEmpty() || results.results[0].faceLandmarks()
                                    .isEmpty()
                            ) return@createExecutor
                            try {
                                results.results[0].let { faceLandmarkerResult ->

                                }
                            } catch (e: Exception) {
                                loge("DUYVD123 --- $e")
                            }
                        }
                    }
                })
                .enableRecord(
                    filemp4!!.absolutePath,
                    recordVideo = true,
                    recordAudio = true
                ) // Support recording as video
                ?.into(binding.renderView)

            for (render in mRenderPipeline!!.endPointRenders) {
                if (render is RecordableRender) {
                    mSupportRecord = render
                }
            }

            viewModel.handleInitFilter()//khi da handle bitmap nhưng camera chua mo, thi se check de hien thi filter lai
            mSupportRecord?.setRecordListener(object : IRecordListener {
                override fun onStart() {
                    viewModel.isRecording.set(true)
                    handleRenderRecording()
                    viewModel.setStartTimeRecording()
                    if (viewModel.isRecording.get()) {
                        startRenderAll()
                    }
                }

                override fun onStop() {
                    viewModel.isSavedRecent.set(true)
                    viewModel.isRecording.set(false)
                    handleRenderRecording()
                    viewModel.setStopTimeRecording()
                }

                override fun onFinish() {
                    viewModel.isSavedRecent.set(true)
                    viewModel.isRecording.set(false)
                    handleRenderRecording()
                    binding.imvHeader.postDelayed({
                        viewModel.showPreview(filemp4!!)
                        viewModel.isLoading.set(false)
                    }, 1000)

                    binding.tvCounting.postDelayed({
                        viewModel.timeRecoding.set(0f)
                    }, 1000)
                }
            })

            binding.tvCounting.postDelayed({
                viewModel.isLoading.set(false)
            }, 200)

            binding.tvCounting.postDelayed({
                viewModel.enableStartRecord.set(true)
            }, 500)
        }

        override fun onDisconnected(camera: CameraDevice) { // onDisconnected
            if (null != mCameraDevice) {
                mCameraDevice!!.close()
            }
        }

        override fun onError(camera: CameraDevice, error: Int) { //
            viewModel.cameraOpened = false
            toast("Failed to start the camera")
        }
    }

    private fun handleRenderRecording() {
        mWordsRender?.isRecording = viewModel.isRecording.get()
    }

    @OptIn(DelicateCoroutinesApi::class)
    var totalTimeRandom = 0
    var maxTimeRandom = (3000f / timeDelayNextQuestion).toInt()
    private fun startRenderAll() {
       startRenderWord()
        if (viewModel.typeFilter == EFFECT_TIKTOK_VOICE) {

            if (!hasCountTimer) {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    while (currentIndexTimer > 1 && !hasCountTimer) {
                        currentIndexTimer--
                        delay(timeDelayTimer)
                        mTimer321Render?.currentQuiz = currentIndexTimer

                        if (currentIndexTimer <= 1) {
                            hasCountTimer = true
                            mRenderPipeline?.removeFilterRender(mTimer321Render)
                            mRenderPipeline?.addFilterRender(mDegreeRender)

                            viewModel.viewModelScope.launch(Dispatchers.IO) {
                                if (viewModel.isRecording.get()) {
                                    while (currentIndexDegree < 180) {
                                        currentIndexDegree ++
                                        delay(((viewModel.timeLimit.get() - 3) * 1000f / 360 * 2f).toLong())
                                        mDegreeRender?.currentQuiz = currentIndexDegree
                                    }
                                }
                            }
                        }
                    }
                }
            }

            viewModel.viewModelScope.launch(Dispatchers.IO) {
                if (viewModel.isRecording.get()) {
                    val timeDelayReturn = 4000L
                    delay(8000)//delay 8s de ghe xoay lai
                    val random = Random.nextInt(0, 3)
                    if(random == 0){//0 ghe quay lai ti le 1/6
                        delay(timeDelayReturn)
                    }else{
                        val randomCase = Random.nextInt(1, 6) // 1, 2, 3,4 se quay lai, 5 : khong quay lai
                        if(randomCase >= 5){//3 ghe quay lai, case nay it nhat 1/6
                            listOf(mTiktokVoiceRender1, mTiktokVoiceRender2, mTiktokVoiceRender3).shuffled()//1 ghe quay lai, 2 ghe quay lai
                                .forEach {//total 4000 milis 4s
                                    it?.currentQuiz = Random.nextInt(1, 3)//1, 2 la gioi tinh
                                    delay(1333)
                                }
                        }else{// 1 ghe quay lai hoac 2 ghe quay lai
                            if(randomCase in 1..3){//1 ghe quay lai, case nay nhieu nhat, 50%
                                listOf(mTiktokVoiceRender1, mTiktokVoiceRender2, mTiktokVoiceRender3).shuffled().first()?.currentQuiz = Random.nextInt(1, 3)
                                delay(4000)
                            }else {//2 ghe quay lai, case nay 1/6
                                listOf(mTiktokVoiceRender2, mTiktokVoiceRender3).shuffled().first()?.currentQuiz = Random.nextInt(1, 3)
                                delay(2000)
                            }

                        }
                    }

                    var indexCongrat = 0
                    val maxLengCongrats = 139
                    mRenderPipeline?.addFilterRender(mCongratRender)
                    while (indexCongrat < maxLengCongrats) {
                        indexCongrat++
                        delay(33)
                        mCongratRender?.currentQuiz = indexCongrat

                        if (indexCongrat >= maxLengCongrats) {
                            mRenderPipeline?.removeFilterRender(mCongratRender)
                        }
                    }
                }
            }
        }
    }

    private fun startRenderWord(){
        if (!hasSetRandomWord && viewModel.typeFilter == EFFECT_SING_A_SONG) {
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                while (totalTimeRandom < maxTimeRandom) {
                    currentWord = Random.nextInt(viewModel.listQuiz.size - 1)
                    delay(timeDelayNextQuestion)
                    totalTimeRandom++
                    mWordsRender?.currentQuiz = currentWord
                    loge("startRenderWord --- $totalTimeRandom")

                    if (totalTimeRandom >= maxTimeRandom) {
                        hasSetRandomWord = true
                    }
                }
            }
        }
    }

    private fun setShowStickmanWhenOpenCamera() {
        NetworkStateMonitor.isConnectNetwork.value.let {
            if (viewModel.isRecording.get()) {
                viewModel.isNoInternet.set(false)
            } else {
                it?.let { it1 ->
                    if (!viewModel.isNoInternet.get()) {
                        viewModel.isNoInternet.set(!it1)
                    }
                }
            }
        }
    }

    private fun releaseCamera() {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            try {
                viewModel.poseLandmarkerHelper = null
                mCameraDevice?.close()
                mCameraDevice = null
            } catch (e: Exception) {
                mCameraDevice = null
            }
        }
    }

    private fun saveRecord() {
        if (mSupportRecord != null && !viewModel.isLoading.get() && viewModel.isRecording.get()) {
            viewModel.isRecording.set(false)
            stopRecording()
        }
    }

    private fun startRecording() {
        if (!viewModel.isSavedRecent.get()) {
            viewModel.setStartTimeRecording()
            if (mSupportRecord?.startRecording() == false) {
                triggerRebirth()
                mSupportRecord?.reInit()
                restartFragment()
            } else {
                binding.imvRecord.postDelayed({
                    if (mSupportRecord?.isRecording == false && viewModel.isRecording.get()) {
                        triggerRebirth()
                        viewModel.isRecording.set(false)
                    }
                }, 1500)
            }
        }
    }

    private fun stopRecording() {
        viewModel.isLoading.set(true)
        mSupportRecord?.stopRecording()
        viewModel.isRecording.set(false)
    }

    private fun restartFragment() {
        viewModel.timeRecoding.set(0f)
        if (!isDetached) {
            Handler(Looper.getMainLooper()).post {
                binding.imvBack.post {
                    findNavControllerSafely()?.popBackStack(R.id.cameraFragment, true)
                    findNavControllerSafely()?.navigate(R.id.cameraFragment)
                }
            }
        }
    }

    private fun Fragment.findNavControllerSafely(): NavController? {
        return if (isAdded) {
            findNavController()
        } else {
            null
        }
    }

    private fun triggerRebirth() {
        viewModel.viewModelScope.launch {
            delay(500)
            restartFragment()
        }
    }

    override fun onBackPressed() {

    }

    private fun restartApp() {
        val intent = MainActivity.newIntent(requireContext())
        startActivity(intent)
        exitProcess(0)
    }
}