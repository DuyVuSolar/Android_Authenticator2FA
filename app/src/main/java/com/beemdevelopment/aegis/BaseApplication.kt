package com.beemdevelopment.aegis

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.webkit.WebView
import androidx.camera.core.CameraSelector
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.beemdevelopment.aegis.utils.LocaleHelper.dLocale
import com.beemdevelopment.aegis.utils.MaxUtils
import com.beemdevelopment.aegis.utils.MaxUtils.TYPEX
import com.beemdevelopment.aegis.utils.MaxUtils.logFirebaseEvent
import com.beemdevelopment.aegis.utils.extension.initBaseApplication
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.Locale

@HiltAndroidApp
class BaseApplication : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
//        PreferenceManager.setDefaultValues(this, R.xml.preferences_koler, false)
    }

    private fun getProcessName(context: Context?): String? {
        if (context == null) return null
        val manager: ActivityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == Process.myPid()) {
                return processInfo.processName
            }
        }
        return null
    }

    private fun configRemote() {
        val remoteConfig = Firebase.remoteConfig.apply {
            setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            })
            setDefaultsAsync(R.xml.remote_config_defaults)
        }

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Set number
                    MaxUtils.setCappingAds(this, remoteConfig.getLong("capping_ads").toInt())
                    MaxUtils.setCount(this, remoteConfig.getLong("countcc").toInt())
                    MaxUtils.setTimeReloadCollap(this, remoteConfig.getLong("timeReloadCollap").toInt())

                    MaxUtils.setString(this, TYPEX, remoteConfig.getString("typex"))

                    val booleanParams = listOf(
                        "isShowingAOA" to MaxUtils.ISSHOWINGAOA,
                        "isShowingAOAFirst" to MaxUtils.ISSHOWINGAOAFIRST,
                        "isShowingInter" to MaxUtils.ISSHOWINGINTER,
                        "isShowingInterGuide" to MaxUtils.ISSHOWINGINTERGUIDE,
                        "isShowingNativeFull" to MaxUtils.ISSHOWINGNATIVE_FULL,
                        "isShowingNativeFullSplash" to MaxUtils.ISSHOWINGNATIVE_FULLSPLASH,
                        "isShowingNativeIntroNext" to MaxUtils.ISSHOWINGNATIVE_FULLINTRONEXT,
                        "isShowingNativeIntro" to MaxUtils.ISSHOWINGNATIVE_INTRO,
                        "isShowingNativeIntroFull" to MaxUtils.ISSHOWINGNATIVE_INTROFULL,
                        "isShowingNativeBottom" to MaxUtils.ISSHOWINGNATIVE_BOTTOM,
                        "isShowingAdsSplash" to MaxUtils.ISSHOWINGADSSPLASH,
                        "isCheckOrg" to MaxUtils.ISCHECKORG,
                        "isShowingNativeSplash" to MaxUtils.ISSHOWINGNATIVE_SPLASH,
                        "isShowingNativeCollap" to MaxUtils.ISSHOWINGNATIVE_COLLAP,
                        "isShowingNativeNotify" to MaxUtils.ISSHOWINGNATIVE_NOTIFY,
                        "isShowingNativeGuide" to MaxUtils.ISSHOWINGNATIVE_GUIDE,
                        "isShowingNativePermission" to MaxUtils.ISSHOWINGNATIVE_PERMISSION,
                        "isShowBreak" to MaxUtils.ISSHOWINGBREAK
                    )

                    // Apply all boolean config values
                    booleanParams.forEach { (remoteKey, localKey) ->
                        MaxUtils.setBoolean(this, localKey, remoteConfig.getBoolean(remoteKey))
                    }

                    MaxUtils.setTimeBr(this, remoteConfig.getLong("timeBr").toInt())
                }
            }
    }

    override fun onCreate() {
        super<Application>.onCreate()

        initBaseApplication()
        instance = this
        dLocale = Locale(getLanguage())
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName(this)
            val packageName = this.packageName
            if (packageName != processName) {
                WebView.setDataDirectorySuffix(processName!!)
            }
        }

        configRemote()

        val appToken = "7597olriscn4"
        val environment = AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(this, appToken, environment)
        config.enableSendingInBackground()
        config.enableCostDataInAttribution()
        config.setOnAttributionChangedListener { attribution ->
            if (attribution.network != null) {
                MaxUtils.setString(this, "attribution", attribution.network.toString().trim())
//                MaxUtils.setString(this, "attribution", "kdfhgkshdfg")
                this.logFirebaseEvent(attribution.network.toString().replace(" ", "_").trim())
            }
        }
        Adjust.initSdk(config)

        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

//        GlobalOverlayTimerManager.registerLifecycleCallbacks(this)
    }


    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
//        currentActivity?.let {//TODO duyvd
//            if (!MaxUtils.maxAdsIsShowing) {
//                if (MaxUtils.getBoolean(
//                        it, MaxUtils.ISSHOWINGAOA,
//                        true
//                    )
//                ) {
//                    MaxUtils.loadAd(
//                        it,
//                    )
//                }
//            }
//        }
    }

    private var currentActivity: Activity? = null

    /** ActivityLifecycleCallback methods. */

    override fun onActivityStarted(activity: Activity) {
        if (!MaxUtils.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}


    //region Language
    private val SharedPreferences_Name_Language = "reverse_audio_language"
    private val SHARE_LANGUAGE_KEY = "SHARE_LANGUAGE_KEY"
    private val SHARE_FLAG_KEY = "SHARE_FLAG_KEY"
    private val defaultLanguage = "en"
    private val defaultFlag = "us"
    fun setLanguage(activity: Context, value: String) {
        val sharePreference =
            activity.getSharedPreferences(SharedPreferences_Name_Language, Context.MODE_PRIVATE)
        sharePreference.edit { putString(SHARE_LANGUAGE_KEY, value) }
    }

    fun getLanguage(): String {
        return getSharedPreferences(
            SharedPreferences_Name_Language,
            Context.MODE_PRIVATE
        ).getString(SHARE_LANGUAGE_KEY, defaultLanguage).toString()
    }

    fun setFlag(activity: Context, value: String) {
        val sharePreference =
            activity.getSharedPreferences(SharedPreferences_Name_Language, Context.MODE_PRIVATE)
        sharePreference.edit { putString(SHARE_FLAG_KEY, value) }
    }

    fun getFlag(): String {
        return getSharedPreferences(
            SharedPreferences_Name_Language,
            Context.MODE_PRIVATE
        ).getString(SHARE_FLAG_KEY, defaultFlag).toString()
    }

    //endregion

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        lateinit var instance: com.beemdevelopment.aegis.BaseApplication

        @JvmStatic
        fun getAppInstance(): com.beemdevelopment.aegis.BaseApplication = instance

        var cameraFacing = CameraSelector.LENS_FACING_FRONT

    }

}