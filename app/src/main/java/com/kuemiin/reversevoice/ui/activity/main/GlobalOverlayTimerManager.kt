package com.kuemiin.reversevoice.ui.activity.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import com.kuemiin.reversevoice.ui.fragment.main.CountdownOverlayFragment
import com.kuemiin.reversevoice.utils.MaxUtils
import com.kuemiin.reversevoice.utils.MaxUtils.ISCHECKORG
import com.kuemiin.reversevoice.utils.MaxUtils.ISSHOWINGBREAK
import com.kuemiin.reversevoice.utils.MaxUtils.getBoolean
import com.kuemiin.reversevoice.utils.MaxUtils.getString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak")
object GlobalOverlayTimerManager : DefaultLifecycleObserver {

    private var timerJob: Job? = null
    private var overlayShowing = false
    private var currentActivityRef: WeakReference<Activity>? = null

    private val excludedScreens = listOf(
        "SplashActivity",
        "IntroActivity",
        "GuideActivity",
        "ChooseLanguageActivity",
        "MainActivity2",
        "MainActivity3",
        "MainActivity4",
        "MainActivity5",
        "AdActivity",
    )

    fun registerLifecycleCallbacks(application: Application) {
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                attachTo(activity)

            }

            override fun onActivityPaused(activity: Activity) {
                detachFrom(activity)
            }

            override fun onActivityCreated(a: Activity, s: Bundle?) {}
            override fun onActivityStarted(a: Activity) {}
            override fun onActivityStopped(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
            override fun onActivityDestroyed(a: Activity) {
                detachFrom(a)
            }
        })
    }

    fun attachTo(activity: Activity) {
        currentActivityRef = WeakReference(activity)
        if (activity is FragmentActivity) {
            if (excludedScreens.any { activity.javaClass.simpleName.contains(it) }) {
                return
            }
            startTimer(activity)
        }
    }

    fun detachFrom(activity: Activity) {
        if (currentActivityRef?.get() == activity) {
            stopTimer()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        overlayShowing = false
    }

    private fun startTimer(activity: Activity) {
        stopTimer()

        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                delay(1000L * (MaxUtils.getTimeBr(activity)))

                val act = currentActivityRef?.get()
                if (!isActivityValid(act)) {
                    overlayShowing = false
                    continue
                }

                if (overlayShowing) {
                    continue
                }

                overlayShowing = true

                val overlay = CountdownOverlayFragment(
                    onFinish = {
                        overlayShowing = false
                        startNextActivity(act)
                    }
                )

                (act as? FragmentActivity)?.let {
                    if (MaxUtils.isCheck(activity)) {
                        if (getBoolean(activity, ISSHOWINGBREAK, true)) {
                            if (!MaxUtils.maxAdsIsShowing && !MaxUtils.isShowingAd && !MaxUtils.isShowingAdSplash) {
                                overlay.safeShow(it, "countdown_overlay")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startNextActivity(activity: Activity?) {
        try {

            val intent = Intent(activity, MainActivity5::class.java)
            activity?.startActivity(intent)

        } catch (e: Exception) {
        }
    }

    private fun isActivityValid(activity: Activity?): Boolean {
        return activity != null && !activity.isFinishing && !activity.isDestroyed
    }
}

fun DialogFragment.safeShow(activity: FragmentActivity?, tag: String? = null) {
    if (activity == null || activity.isFinishing || activity.isDestroyed) {
        return
    }

    val fm: FragmentManager = activity.supportFragmentManager
    try {
        if (fm.isStateSaved) {
            fm.beginTransaction()
                .add(this, tag)
                .commitAllowingStateLoss()
        } else {
            show(fm, tag)
        }
    } catch (e: IllegalStateException) {
    }
}