package com.beemdevelopment.aegis.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.edit
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.ui_old.activity.main.MainActivity2
import com.beemdevelopment.aegis.ui_old.activity.main.MainActivity3
import com.beemdevelopment.aegis.ui_old.activity.main.MainActivity4
import com.beemdevelopment.aegis.utils.extension.loge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber


object MaxUtils {

    //region interstitial
    private var nextScreenListener: NextScreenListener? = null

    var isGoneBanner = true

    var count = 0

    interface NextScreenListener {
        fun nextScreen()
    }

    private val _showLoading = MutableStateFlow(false)
    val showLoading = _showLoading.asStateFlow()
    var maxAdsIsShowing = false

    fun loadAdmob(activity: Activity, nextScreenListener: NextScreenListener) {
        MaxUtils.nextScreenListener = nextScreenListener

        val adRequest = AdRequest.Builder().build()
        _showLoading.update { true }

        InterstitialAd.load(
            activity,
            INTER_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    nextScreenListener.nextScreen()
                    _showLoading.update { false }
                    activity.logFirebaseEvent("ad_inter_load_fail")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    activity.logFirebaseEvent("ad_inter_load_success")

                    interstitialAd.setOnPaidEventListener { impressionData ->
                        logRevCC(activity, "inter", impressionData.valueMicros.toDouble())
                        logRevAdjust(impressionData.valueMicros.toDouble())
                    }
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                maxAdsIsShowing = false
                                lastimeShowInterstitialAd = System.currentTimeMillis()
                                nextScreenListener.nextScreen()
                                loge("onAdDismissedFullScreenContent")
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                maxAdsIsShowing = false
                                _showLoading.update { false }
                                nextScreenListener.nextScreen()
                                loge("onAdFailedToShowFullScreenContent")
                            }

                            override fun onAdShowedFullScreenContent() {
                                maxAdsIsShowing = true
                                _showLoading.update { false }
                                activity.logFirebaseEvent("ad_inter_show")
                            }

                            override fun onAdClicked() {
                                super.onAdClicked()
                                activity.logFirebaseEvent("ad_inter_click")
                            }
                        }

                    if (getBoolean(activity, ISSHOWINGNATIVE_FULL, true)) {
                        if (isCheck(activity)) {
                            activity.startActivity(
                                Intent(
                                    activity,
                                    MainActivity2::class.java
                                )
                            )
                        }
                    }
                    Handler(Looper.myLooper()!!).postDelayed({
                        // Make sure the activity is still alive before showing the ad.
                        if (!activity.isFinishing && !activity.isDestroyed) {
                            interstitialAd.show(activity)
                        }
                    }, 10)

                }
            }
        )
    }

    private var lastimeShowInterstitialAd = 0L

    fun showAdsWithCustomCount(
        activity: Activity,
        nextScreenListener: NextScreenListener
    ) {
        MaxUtils.nextScreenListener = nextScreenListener
//        if (getBoolean(activity, ISSHOWINGINTER, true)) {
//            if ((System.currentTimeMillis() - lastimeShowInterstitialAd) > (getCappingAds(
//                    activity
//                ) * 1000)
//            ) {
                loadAdmob(activity, nextScreenListener)//
//            } else {
//                nextScreenListener.nextScreen()
//            }
//        } else {
//            nextScreenListener.nextScreen()
//        }
    }
    //endregion

    //region interstitial splash
    private var interstitialAdSplash: InterstitialAd? = null
    private var isLoadingInterSplash = false
    private var isShowingInterSplash = false
    fun loadAdmobSplash(
        activity: Context, onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        if (isLoadingInterSplash || interstitialAdSplash != null) return
        val adRequest = AdRequest.Builder().build()
        isLoadingInterSplash = true
        InterstitialAd.load(
            activity,
            INTER_ID_SPLASH,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    activity.logFirebaseEvent("ad_inter_load_fail")
                    isLoadingInterSplash = false
                    onShowAdCompleteListener.onShowAdComplete(false)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    activity.logFirebaseEvent("ad_inter_load_success")
                    isLoadingInterSplash = false
                    interstitialAdSplash = interstitialAd
                    onShowAdCompleteListener.onShowAdComplete(true)
                }
            }
        )
    }

    fun showAdmobSplash(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {
        if (isShowingInterSplash) return
        if (interstitialAdSplash == null) {
            onShowAdCompleteListener.onShowAdComplete(false)
            return
        }
        interstitialAdSplash?.setOnPaidEventListener { impressionData ->
            logRevCC(activity, "inter", impressionData.valueMicros.toDouble())
            logRevAdjust(impressionData.valueMicros.toDouble())
        }
        interstitialAdSplash?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAdSplash = null
                    isShowingInterSplash = false
                    onShowAdCompleteListener.onShowAdComplete(true)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    interstitialAdSplash = null
                    isShowingInterSplash = false
                    onShowAdCompleteListener.onShowAdComplete(false)
                }

                override fun onAdShowedFullScreenContent() {
                    activity.logFirebaseEvent("ad_inter_show")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    activity.logFirebaseEvent("ad_inter_click")
                }
            }
        isShowingInterSplash = true
        if (getBoolean(activity, ISSHOWINGNATIVE_FULLSPLASH, true)) {
            if (isCheck(activity)) {
                activity.startActivity(Intent(activity, MainActivity3::class.java))
            }
        }
        Handler(Looper.myLooper()!!).postDelayed({
            if (!activity.isFinishing && !activity.isDestroyed) {
                interstitialAdSplash?.show(activity)
            }
        }, 10)
    }
    //endregion

    //region interstitial intro
    fun loadAdmobIntro(activity: Activity, nextScreenListener: NextScreenListener) {
        MaxUtils.nextScreenListener = nextScreenListener

        val adRequest = AdRequest.Builder().build()
        _showLoading.update { true }

        InterstitialAd.load(
            activity,
            INTER_ID_INTRO,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    nextScreenListener.nextScreen()
                    _showLoading.update { false }
                    activity.logFirebaseEvent("ad_inter_load_fail")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    activity.logFirebaseEvent("ad_inter_load_success")

                    interstitialAd.setOnPaidEventListener { impressionData ->
                        logRevCC(activity, "inter", impressionData.valueMicros.toDouble())
                        logRevAdjust(impressionData.valueMicros.toDouble())
                    }
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                maxAdsIsShowing = false
                                lastimeShowInterstitialAd = System.currentTimeMillis()
                                nextScreenListener.nextScreen()
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                maxAdsIsShowing = false
                                _showLoading.update { false }
                                nextScreenListener.nextScreen()
                            }

                            override fun onAdShowedFullScreenContent() {
                                maxAdsIsShowing = true
                                _showLoading.update { false }
                                activity.logFirebaseEvent("ad_inter_show")
                            }

                            override fun onAdClicked() {
                                super.onAdClicked()
                                activity.logFirebaseEvent("ad_inter_click")
                            }
                        }

                    if (getBoolean(activity, ISSHOWINGNATIVE_FULLINTRONEXT, true)) {
                        activity.startActivity(Intent(activity, MainActivity4::class.java))
                    }
                    Handler(Looper.myLooper()!!).postDelayed({
                        if (!activity.isFinishing && !activity.isDestroyed) {
                            interstitialAd.show(activity)
                        }
                    }, 10)
                }
            }
        )
    }
    //endregion

    //region native 1 2 3
    fun loadNative123(
        mActivity: Activity,
        rootView: ViewGroup?,
        containerShimmer: ShimmerFrameLayout?,
        frameLayout: FrameLayout?,
        typeScreen: Int = 0
    ) {
        if (rootView == null || containerShimmer == null || frameLayout == null) return

        rootView.visibility = View.VISIBLE
        containerShimmer.apply {
            visibility = View.VISIBLE
            startShimmer()
        }

        val nativeAdKeys = mapOf(
            8 to Pair(NATIVEID_INTRO2, AD_UNIT_ID_NATIVE_INTRO1),
            7 to Pair(NATIVEID_INTRO1, AD_UNIT_ID_NATIVE_INTRO2),
            6 to Pair(NATIVEID_INTROFULL, AD_UNIT_ID_NATIVE_INTROFULL),
            5 to Pair(NATIVEID_COLLAP, AD_UNIT_ID_NATIVE_COLLAP),
        )

        val key = getString(
            mActivity,
            nativeAdKeys[typeScreen]?.first ?: NATIVEID_INTRO1,
            nativeAdKeys[typeScreen]?.second ?: AD_UNIT_ID_NATIVE_INTRO1
        )

        val adLoader = AdLoader.Builder(mActivity, key)
            .forNativeAd { unifiedNativeAd ->
                unifiedNativeAd.setOnPaidEventListener { impressionData ->
                    logRevAdjust(
                        impressionData.valueMicros.toDouble()
                    )

                    logRevCC(mActivity, "native", impressionData.valueMicros.toDouble())
                }
                containerShimmer.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
                frameLayout.visibility = View.VISIBLE

                val layoutMap = mapOf(
                    8 to R.layout.native_admob_ad_intro,
                    7 to R.layout.native_admob_ad_intro,
                    6 to R.layout.full_screen_native_ads,
                    5 to R.layout.native_admob_ad_collap,
                )

                val layoutRes = layoutMap[typeScreen] ?: R.layout.native_admob_ad
                val nativeAdView = mActivity.layoutInflater.inflate(layoutRes, null) as NativeAdView

                populateUnifiedNativeAdView(unifiedNativeAd, nativeAdView)

                frameLayout.removeAllViews()
                frameLayout.addView(nativeAdView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    containerShimmer.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }
                    rootView.visibility = View.GONE
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    loadNative123(mActivity, rootView, containerShimmer, frameLayout, typeScreen)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build()).build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun loadNativeSplash(
        mActivity: Activity,
        rootView: ViewGroup,
        containerShimmer: ShimmerFrameLayout,
        frameLayout: FrameLayout, nextScreenListener: NextScreenListener
    ) {
        MaxUtils.nextScreenListener = nextScreenListener
        if (!getBoolean(
                mActivity,
                ISSHOWINGNATIVE_SPLASH,
                true
            ) && !isCheck(mActivity)
        ) {
            containerShimmer.apply {
                stopShimmer()
                visibility = View.GONE
            }
            rootView.visibility = View.GONE
            frameLayout.visibility = View.GONE
            nextScreenListener.nextScreen()
            return
        }

        rootView.visibility = View.VISIBLE
        containerShimmer.apply {
            visibility = View.VISIBLE
            startShimmer()
        }

        val adLoader = AdLoader.Builder(mActivity, AD_UNIT_ID_NATIVE_SPLASH)
            .forNativeAd { unifiedNativeAd ->
                unifiedNativeAd.setOnPaidEventListener { adValue ->
                    logRevCC(
                        mActivity,
                        "native",
                        adValue.valueMicros.toDouble()
                    )
                    logRevAdjust(adValue.valueMicros.toDouble())
                }
                containerShimmer.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
                frameLayout.visibility = View.VISIBLE

                val layoutRes = R.layout.native_admob_ad
                val nativeAdView = mActivity.layoutInflater.inflate(layoutRes, null) as NativeAdView

                populateUnifiedNativeAdView(
                    unifiedNativeAd,
                    nativeAdView
                )

                frameLayout.removeAllViews()
                frameLayout.addView(nativeAdView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    nextScreenListener.nextScreen()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    containerShimmer.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }
                    rootView.visibility = View.GONE
                    frameLayout.visibility = View.GONE
                    nextScreenListener.nextScreen()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    loadNativeSplash(
                        mActivity,
                        rootView,
                        containerShimmer,
                        frameLayout,
                        nextScreenListener
                    )
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build()).build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun loadNativeNotify(
        mActivity: Activity,
        rootView: ViewGroup,
        containerShimmer: ShimmerFrameLayout,
        frameLayout: FrameLayout,
        guide: Boolean = false
    ) {
        val checkKey = if (!guide) ISSHOWINGNATIVE_NOTIFY else ISSHOWINGNATIVE_GUIDE
        if (!getBoolean(
                mActivity,
                checkKey,
                true
            )
        ) {
            containerShimmer.apply {
                stopShimmer()
                visibility = View.GONE
            }
            rootView.visibility = View.GONE
            frameLayout.visibility = View.GONE
            return
        }

        rootView.visibility = View.VISIBLE
        containerShimmer.apply {
            visibility = View.VISIBLE
            startShimmer()
        }

        val key = if (!guide) AD_UNIT_ID_NATIVE_NOTIFY else AD_UNIT_ID_NATIVE_GUIDE

        val adLoader = AdLoader.Builder(mActivity, key)
            .forNativeAd { unifiedNativeAd ->
                unifiedNativeAd.setOnPaidEventListener { adValue ->
                    logRevCC(
                        mActivity,
                        "native",
                        adValue.valueMicros.toDouble()
                    )
                    logRevAdjust(adValue.valueMicros.toDouble())
                }
                containerShimmer.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
                frameLayout.visibility = View.VISIBLE

                val layoutRes = R.layout.native_admob_ad_guide
                val nativeAdView = mActivity.layoutInflater.inflate(layoutRes, null) as NativeAdView

                populateUnifiedNativeAdView(
                    unifiedNativeAd,
                    nativeAdView
                )

                frameLayout.removeAllViews()
                frameLayout.addView(nativeAdView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    containerShimmer.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }
                    rootView.visibility = View.GONE
                    frameLayout.visibility = View.GONE
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    loadNativeNotify(
                        mActivity,
                        rootView,
                        containerShimmer,
                        frameLayout,
                    )
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build()).build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun loadNativePermission(
        mActivity: Activity,
        rootView: ViewGroup,
        containerShimmer: ShimmerFrameLayout,
        frameLayout: FrameLayout
    ) {
        if (!getBoolean(
                mActivity,
                ISSHOWINGNATIVE_PERMISSION,
                true
            )
        ) {
            containerShimmer.apply {
                stopShimmer()
                visibility = View.GONE
            }
            rootView.visibility = View.GONE
            frameLayout.visibility = View.GONE
            return
        }

        rootView.visibility = View.VISIBLE
        containerShimmer.apply {
            visibility = View.VISIBLE
            startShimmer()
        }

        val adLoader = AdLoader.Builder(mActivity, AD_UNIT_ID_NATIVE_PERMISSION)
            .forNativeAd { unifiedNativeAd ->
                unifiedNativeAd.setOnPaidEventListener { adValue ->
                    logRevCC(
                        mActivity,
                        "native",
                        adValue.valueMicros.toDouble()
                    )
                    logRevAdjust(adValue.valueMicros.toDouble())
                }
                containerShimmer.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
                frameLayout.visibility = View.VISIBLE

                val layoutRes = R.layout.native_admob_ad_guide
                val nativeAdView = mActivity.layoutInflater.inflate(layoutRes, null) as NativeAdView

                populateUnifiedNativeAdView(
                    unifiedNativeAd,
                    nativeAdView
                )

                frameLayout.removeAllViews()
                frameLayout.addView(nativeAdView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    containerShimmer.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }
                    rootView.visibility = View.GONE
                    frameLayout.visibility = View.GONE
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    loadNativePermission(
                        mActivity,
                        rootView,
                        containerShimmer,
                        frameLayout,
                    )
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build()).build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun populateNativeAdViewBanner(nativeAd: NativeAd, adView: NativeAdView) {

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)

        if (nativeAd.headline == null) {
            adView.headlineView?.visibility = View.INVISIBLE
        } else {
            adView.headlineView?.visibility = View.VISIBLE
            (adView.headlineView as TextView).text = nativeAd.headline
        }

        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }

    fun populateUnifiedNativeAdView(
        nativeAd: NativeAd,
        adView: NativeAdView
    ) {
        val mediaView: MediaView = adView.findViewById(R.id.ad_media)
        adView.mediaView = mediaView
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)

        if (nativeAd.headline == null) {
            adView.headlineView?.visibility = View.INVISIBLE
        } else {
            adView.headlineView?.visibility = View.VISIBLE
            (adView.headlineView as TextView).text = nativeAd.headline
        }

        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }
    //endregion

    //region appopen
    private val _showLoadingAOA = MutableStateFlow(false)
    val showLoadingAOA = _showLoadingAOA.asStateFlow()

    interface OnShowAdCompleteListener {
        fun onShowAdComplete(isSuccess: Boolean)
    }

    private var isLoadingAd = false
    var isShowingAd = false
    var checkFromStart = true

    fun loadAd(context: Activity) {
        if (isLoadingAd || checkFromStart) {
            return
        }
        _showLoadingAOA.update { true }
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AOA_RESUME_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    isLoadingAd = false
                    _showLoadingAOASplash.update { true }
                    ad.setOnPaidEventListener { impressionData ->
                        logRevCC(context, "app_open", impressionData.valueMicros.toDouble())
                        logRevAdjust(impressionData.valueMicros.toDouble())
                    }
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            isShowingAd = false
                            _showLoadingAOA.update { false }
                            _showLoadingAOASplash.update { false }
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            isShowingAd = false
                            _showLoadingAOA.update { false }
                            _showLoadingAOASplash.update { false }
                        }

                        override fun onAdShowedFullScreenContent() {
                        }
                    }
                    isShowingAd = true
                    ad.show(context)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    _showLoadingAOA.update { false }
                }
            })
    }
    //endregion

    //region app open splash
    private val _showLoadingAOASplash = MutableStateFlow(false)
    val showLoadingAOASplash = _showLoadingAOASplash.asStateFlow()

    private var appOpenAdSplash: AppOpenAd? = null
    private var isLoadingAdSplash = false
    var isShowingAdSplash = false

    fun loadAdSplash(
        context: Context, onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        if (isLoadingAdSplash || appOpenAdSplash != null) {
            return
        }
        isLoadingAdSplash = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AOA_SPLASH_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAdSplash = ad
                    isLoadingAdSplash = false
                    onShowAdCompleteListener.onShowAdComplete(true)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAdSplash = false
                    onShowAdCompleteListener.onShowAdComplete(false)
                }
            })
    }


    fun showAdIfAvailableSplash(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        if (isShowingAdSplash) {
            return
        }

        if (appOpenAdSplash == null) {
            onShowAdCompleteListener.onShowAdComplete(false)
            return
        }
        appOpenAdSplash!!.setOnPaidEventListener { impressionData ->
            logRevCC(activity, "app_open", impressionData.valueMicros.toDouble())
            logRevAdjust(impressionData.valueMicros.toDouble())
        }
        appOpenAdSplash!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAdSplash = null
                isShowingAdSplash = false
                onShowAdCompleteListener.onShowAdComplete(true)
                _showLoadingAOASplash.update { false }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAdSplash = null
                isShowingAdSplash = false
                onShowAdCompleteListener.onShowAdComplete(false)
                _showLoadingAOASplash.update { false }
            }

            override fun onAdShowedFullScreenContent() {
            }
        }
        isShowingAdSplash = true
        _showLoadingAOASplash.update { true }
        appOpenAdSplash!!.show(activity)
    }

    //endregion

    // region event
    fun isCheck(activity: Context): Boolean = getBoolean(activity, ISCHECKORG, true) &&
            "Organic" != getString(activity, "attribution", "Organic")

    fun Context.logFirebaseEvent(rawEvent: String) {
        val cleanedEvent = rawEvent
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9_]+"), "_")
            .take(40)

        if (cleanedEvent.startsWith("firebase_") || cleanedEvent.startsWith("google_")) {
            Timber.e("Event name must not start with reserved prefixes.")
            return
        }

        val bundle = Bundle().apply {
            putString(cleanedEvent, cleanedEvent)
        }

        FirebaseAnalytics.getInstance(this).logEvent(cleanedEvent, bundle)
    }

    fun logRevCC(activity: Context, adFormat: String, adValue: Double) {
        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        val paramsAdRevenueSDK = Bundle()
        paramsAdRevenueSDK.putString(FirebaseAnalytics.Param.AD_FORMAT, adFormat)
        paramsAdRevenueSDK.putDouble(
            FirebaseAnalytics.Param.VALUE,
            (adValue / 1000000)
        )
        paramsAdRevenueSDK.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
        mFirebaseAnalytics.logEvent("ad_revenue_sdk", paramsAdRevenueSDK)
    }

    fun logRevAdjust(revenue: Double) {
        val adRevenue = AdjustAdRevenue("admob_sdk")
        adRevenue.setRevenue(revenue * 0.000001f, "USD")
        Adjust.trackAdRevenue(adRevenue)
    }

    // endregion

//    const val INTER_ID = "ca-app-pub-2622117118790581/4788570087"
//    const val INTER_ID_INTRO = "ca-app-pub-2622117118790581/8441286352"
//    const val INTER_ID_SPLASH = "ca-app-pub-2622117118790581/2974558449"
//    const val AOA_RESUME_ID = "ca-app-pub-2622117118790581/8577017727"
//    const val AOA_SPLASH_ID = "ca-app-pub-2622117118790581/8131322830"
//    const val AD_UNIT_ID_NATIVE_FULL = "ca-app-pub-2622117118790581/4756780492"
//    const val AD_UNIT_ID_NATIVE_FULLSPLASH = "ca-app-pub-2622117118790581/7185072670"
//    const val AD_UNIT_ID_NATIVE_FULLINTRO = "ca-app-pub-2622117118790581/4479018288"
//    const val AD_UNIT_ID_NATIVE_FULLBREAK = "ca-app-pub-2622117118790581/3188192730"
//    const val AD_UNIT_ID_NATIVE_SPLASH = "ca-app-pub-2622117118790581/7971540730"
//    const val AD_UNIT_ID_NATIVE_NOTIFY = ""
//    const val AD_UNIT_ID_NATIVE_GUIDE = ""
//    const val AD_UNIT_ID_NATIVE_PERMISSION = ""
//    const val AD_UNIT_ID_NATIVE_INTRO1 = "ca-app-pub-2622117118790581/4040228105"
//    const val AD_UNIT_ID_NATIVE_INTRO2 = "ca-app-pub-2622117118790581/4558909339"
//    const val AD_UNIT_ID_NATIVE_INTROFULL = "ca-app-pub-2622117118790581/4581932397"
//    const val AD_UNIT_ID_NATIVE_COLLAP = "ca-app-pub-2622117118790581/6898132219"
//    const val AD_UNIT_ID_NATIVE_BOTTOM = "ca-app-pub-2622117118790581/3245827661"

    const val INTER_ID = "ca-app-pub-3940256099942544/1033173712"
    const val INTER_ID_INTRO = "ca-app-pub-3940256099942544/1033173712"
    const val INTER_ID_SPLASH = "ca-app-pub-3940256099942544/1033173712"
    const val AOA_RESUME_ID = "ca-app-pub-3940256099942544/9257395921"
    const val AOA_SPLASH_ID = "ca-app-pub-3940256099942544/9257395921"
    const val AD_UNIT_ID_NATIVE_FULL = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_FULLSPLASH = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_FULLINTRO = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_FULLBREAK = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_SPLASH = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_NOTIFY = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_GUIDE = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_PERMISSION = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_INTRO1 = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_INTRO2 = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_INTROFULL = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_COLLAP = "/21775744923/example/native"
    const val AD_UNIT_ID_NATIVE_BOTTOM = "/21775744923/example/native"

    private const val SharedPreferences_Name = "REVERSE_VOICE"
    const val ISSHOWINGADSSPLASH = "ISSHOWINGADSSPLASH"
    const val ISSHOWINGNATIVE_SPLASH = "ISSHOWINGNATIVE_SPLASH"
    const val ISSHOWINGAOAFIRST = "ISSHOWINGAOAFIRST"
    const val ISSHOWINGNATIVE_FULLSPLASH = "ISSHOWINGNATIVE_FULLSPLASH"
    const val ISSHOWINGNATIVE_FULL = "ISSHOWINGNATIVE_FULL"
    const val ISSHOWINGNATIVE_NOTIFY = "ISSHOWINGNATIVE_NOTIFY"
    const val ISSHOWINGNATIVE_GUIDE = "ISSHOWINGNATIVE_GUIDE"
    const val ISSHOWINGNATIVE_PERMISSION = "ISSHOWINGNATIVE_PERMISSION"

    const val ISSHOWINGAOA = "ISSHOWINGAOA"

    const val ISSHOWINGINTER = "ISSHOWINGINTER"
    const val ISSHOWINGINTERGUIDE = "ISSHOWINGINTERGUIDE"

    const val TYPEX = "TYPEX"

    const val ISSHOWINGNATIVE_INTRO = "ISSHOWINGNATIVE_INTRO"
    const val ISSHOWINGNATIVE_FULLINTRONEXT = "ISSHOWINGNATIVE_FULLINTRONEXT"
    const val ISSHOWINGNATIVE_INTROFULL = "ISSHOWINGNATIVE_INTROFULL"
    const val ISSHOWINGNATIVE_BOTTOM = "ISSHOWINGNATIVE_BOTTOM"
    const val ISSHOWINGNATIVE_COLLAP = "ISSHOWINGNATIVE_COLLAP"
    const val ISSHOWINGBREAK = "ISSHOWINGBREAK"

    const val ISCHECKORG = "ISCHECKORG"

    const val NATIVEID_INTRO1 = "NATIVEID_INTRO1"
    const val NATIVEID_INTRO2 = "NATIVEID_INTRO2"
    const val NATIVEID_INTROFULL = "NATIVEID_INTROFULL"
    const val NATIVEID_COLLAP = "NATIVEID_COLLAP"

    //endregion
    fun setTimeBr(activity: Context, value: Int) {
        val sharePreference =
            activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
        sharePreference.edit { putInt("timeBr", value) }
    }

    fun getTimeBr(activity: Activity): Int {
        return activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
            .getInt("timeBr", 20)
    }

    fun setCappingAds(activity: Context, value: Int) {
        val sharePreference =
            activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
        sharePreference.edit().putInt("capping_ads", value).apply()
    }

    private fun getCappingAds(activity: Activity): Int {
        return activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
            .getInt("capping_ads", 20)
    }

    fun setCount(activity: Context, value: Int) {
        val sharePreference =
            activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
        sharePreference.edit().putInt("countcc", value).apply()
    }

    fun getCount(activity: Activity): Int {
        return activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
            .getInt("countcc", 5)
    }

    fun setTimeReloadCollap(activity: Context, value: Int) {
        val sharePreference =
            activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
        sharePreference.edit().putInt("timeReloadCollap", value).apply()
    }

    fun getTimeReloadCollap(activity: Activity): Int {
        return activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
            .getInt("timeReloadCollap", 20)
    }

    fun setString(activity: Context, key: String, value: String) {
        val sharePreference =
            activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
        sharePreference.edit().putString(key, value).apply()
    }

    fun getString(activity: Context, key: String, defaultValue: String): String {
        return activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
            .getString(key, defaultValue).toString()
    }

    fun setBoolean(activity: Context, key: String, value: Boolean) {
        val sharePreference =
            activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
        sharePreference.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(activity: Context, key: String, defaultValue: Boolean): Boolean {
        return activity.getSharedPreferences(SharedPreferences_Name, Context.MODE_PRIVATE)
            .getBoolean(key, defaultValue)
    }
}