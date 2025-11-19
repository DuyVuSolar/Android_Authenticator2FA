package com.kuemiin.reversevoice.ui.activity.splash

import android.annotation.SuppressLint
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.viewModelScope
import com.kuemiin.reversevoice.BaseApplication
import com.kuemiin.reversevoice.base.BaseViewModel
import com.kuemiin.reversevoice.data.store.DataStoreHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.kuemiin.reversevoice.utils.MaxUtils
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class SplashViewModel @Inject constructor(
    val store: DataStoreHelper
) : BaseViewModel() {

    // region Const and Fields

    var progressSeekbar: ObservableInt = ObservableInt(0)
    private val splashEventChannel = Channel<SplashEvent>()
    val splashEvent = splashEventChannel.receiveAsFlow()
    var hasChooseLanguage: ObservableBoolean = ObservableBoolean(false)
    var showIntro: ObservableBoolean = ObservableBoolean(false)
    private val timeOutSeekbar = 8000L
    private var currentTimeOutAOA = 0
    // endregion

    init {
        checkShowIntro()
    }

    private fun checkShowIntro() = viewModelScope.launch {
        hasChooseLanguage.set(store.hasChooseLanguage.first())
        showIntro.set(store.hasShowIntro.first())
//        hasSetCategoryDefault.set(store.hasSetCategoryDefault.first())
    }


    fun initialSeekbar() = viewModelScope.launch {
        progressSeekbar.set(0)
        checkShowIntro()
        viewModelScope.launch {
            while (progressSeekbar.get() < 100) {
                progressSeekbar.set(progressSeekbar.get() + 1)
                delay(timeOutSeekbar / 100)
            }
        }
    }

    fun startDelayedNavigation() = viewModelScope.launch {
        var isShowAOA = false
        viewModelScope.launch {
            var seconds = 0
            while (true) {
                seconds++
                delay(1000)
                if (seconds == if (MaxUtils.getBoolean(
                            BaseApplication.instance,
                            MaxUtils.ISSHOWINGADSSPLASH,
                            true
                        )
                    ) 18 else 8
                ) {
                    if (!isShowAOA) {
                        onTimeout()
                    }
                }
            }
        }
        if (MaxUtils.getBoolean(
                BaseApplication.instance,
                MaxUtils.ISSHOWINGADSSPLASH,
                true
            )
        ) {
            if (MaxUtils.getBoolean(
                    BaseApplication.instance,
                    MaxUtils.ISSHOWINGAOAFIRST,
                    true
                )
            ) {
                MaxUtils.loadAdSplash(
                    BaseApplication.instance,
                    object : MaxUtils.OnShowAdCompleteListener {
                        override fun onShowAdComplete(isSuccess: Boolean) {
                            if (isSuccess) {
                                isShowAOA = true
                                showAOA()
                            } else {
                                isShowAOA = false
                                onFailAOA()
                            }
                        }
                    })
            } else {
                MaxUtils.loadAdmobSplash(
                    BaseApplication.instance,
                    object : MaxUtils.OnShowAdCompleteListener {
                        override fun onShowAdComplete(isSuccess: Boolean) {
                            if (isSuccess) {
                                isShowAOA = true
                                showAOA()
                            } else {
                                isShowAOA = false
                                onFailAOA()
                            }
                        }
                    })
            }
        } else {
            onTimeout()
        }
    }

    fun showAOA() = viewModelScope.launch {
        currentTimeOutAOA = (progressSeekbar.get() * timeOutSeekbar / 100f).toInt()
        if (currentTimeOutAOA >= timeOutSeekbar) {
            splashEventChannel.send(SplashEvent.NavigateToHome)
        } else {
            delay(timeOutSeekbar - currentTimeOutAOA + 300)
            splashEventChannel.send(SplashEvent.NavigateToHome)
        }
    }

    fun onTimeout() = viewModelScope.launch {
        splashEventChannel.send(SplashEvent.TimeOutAOA)
    }

    fun onFailAOA() = viewModelScope.launch {
        delay(2000)
        splashEventChannel.send(SplashEvent.FailedAOA)
    }
    // endregion

    sealed class SplashEvent {
        object NavigateToHome : SplashEvent()
        object TimeOutAOA : SplashEvent()
        object FailedAOA : SplashEvent()
    }
}