package com.kuemiin.reversevoice.ui.activity.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseActivity
import com.kuemiin.reversevoice.databinding.ActivitySplashBinding
import com.kuemiin.reversevoice.ui.activity.main.MainActivity
import com.kuemiin.reversevoice.ui.activity.intro.IntroActivity
import com.kuemiin.reversevoice.utils.MaxUtils
import com.kuemiin.reversevoice.utils.extension.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.kuemiin.reversevoice.utils.GoogleMobileAdsConsentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.Arrays
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, SplashActivity::class.java).apply {
            }
    }

    private val viewModel by viewModels<SplashViewModel>()

    override fun getLayoutId(): Int = R.layout.activity_splash

    private val isMobileAdsInitializeCalled = AtomicBoolean(false)

    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@SplashActivity) {}
            MobileAds.setAppMuted(true)
        }
    }

    override fun initViews() {
        if (!isTaskRoot) {
            finish()
            return
        }

        googleMobileAdsConsentManager =
            GoogleMobileAdsConsentManager.getInstance(this)
        if (!MaxUtils.getBoolean(this, "gatherStatus", false)) {
            googleMobileAdsConsentManager.gatherConsent(this) { consentError ->
                if (consentError != null) {
                }
                MaxUtils.setBoolean(this, "gatherStatus", true)

                if (googleMobileAdsConsentManager.canRequestAds) {
                    initializeMobileAdsSdk()
                    initView()
                }

            }
        } else {

            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }

            initView()
        }
    }

    private fun initView() {
        MaxUtils.checkFromStart = true

        binding.viewmodel = viewModel
        binding.maxSingleton = MaxUtils

        MaxUtils.loadNativeSplash(
            this@SplashActivity, binding.bannerView, binding.shimmerContainerBanner,
            binding.flAdplaceholderBanner, object : MaxUtils.NextScreenListener {
                override fun nextScreen() {
                    viewModel.startDelayedNavigation()
                    viewModel.initialSeekbar()
                }
            }
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.splashEvent.collect { event ->
                    when (event) {
                        is SplashViewModel.SplashEvent.NavigateToHome -> {
                            if (MaxUtils.getBoolean(
                                    this@SplashActivity,
                                    MaxUtils.ISSHOWINGADSSPLASH,
                                    true
                                )
                            ) {
                                if (MaxUtils.getBoolean(
                                        this@SplashActivity,
                                        MaxUtils.ISSHOWINGAOAFIRST,
                                        true
                                    )
                                ) {
                                    MaxUtils.showAdIfAvailableSplash(
                                        this@SplashActivity,
                                        object : MaxUtils.OnShowAdCompleteListener {
                                            override fun onShowAdComplete(isSuccess: Boolean) {
                                                checkToNavigateHome()
                                            }
                                        })
                                } else {
                                    MaxUtils.showAdmobSplash(
                                        this@SplashActivity,
                                        object : MaxUtils.OnShowAdCompleteListener {
                                            override fun onShowAdComplete(isSuccess: Boolean) {
                                                checkToNavigateHome()
                                            }
                                        })
                                }
                            } else {
                                checkToNavigateHome()
                            }
                        }

                        SplashViewModel.SplashEvent.FailedAOA -> {
                            checkToNavigateHome()
                        }

                        SplashViewModel.SplashEvent.TimeOutAOA -> {
                            checkToNavigateHome()
                        }

                        else -> {
                            // Handle other events
                        }
                    }.exhaustive
                }
            }
        }
    }

    private fun checkToNavigateHome() {
//        if (!viewModel.showIntro.get()) {
        startActivity(IntroActivity.newIntent(this@SplashActivity))
//        } else {
//            startActivity(MainActivity.newIntent(this@SplashActivity))
//        }
        MaxUtils.checkFromStart = false
        finish()
    }


    override fun initEvents() {
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }

    override fun onDestroy() {
        super.onDestroy()
        MaxUtils.checkFromStart = false
    }
}