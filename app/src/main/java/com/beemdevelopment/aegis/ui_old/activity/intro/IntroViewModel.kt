package com.beemdevelopment.aegis.ui_old.activity.intro

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.beemdevelopment.aegis.BaseApplication
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseViewModel
import com.beemdevelopment.aegis.data.store.DataStoreHelper
import com.beemdevelopment.aegis.data.store.DataStoreHelper.Companion.KEY_HAS_SHOW_INTRO
import com.beemdevelopment.aegis.model.IntroSplash
import com.beemdevelopment.aegis.utils.MaxUtils
import com.beemdevelopment.aegis.utils.MaxUtils.getBoolean
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class IntroViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val store: DataStoreHelper
) : BaseViewModel() {

    // region Const and Fields
    private val introEventChannel = Channel<IntroEvent>()
    val introEvent = introEventChannel.receiveAsFlow()

    var showGuide = ObservableBoolean(false)
    var indexIntro: ObservableInt = ObservableInt(0)

    var listIntroAds = arrayListOf(
        IntroSplash(R.drawable.bg_title_intro1, R.drawable.bg_intro1, 0, true),
        IntroSplash(R.drawable.bg_title_intro2, R.drawable.bg_intro2, 1, false),
        IntroSplash(R.drawable.bg_title_intro3, R.drawable.bg_intro3, 2, false),
        IntroSplash(R.drawable.bg_transparent, R.drawable.bg_transparent, 3, false, true),
        IntroSplash(R.drawable.bg_title_intro4, R.drawable.bg_intro4, 4, true),
        IntroSplash(R.drawable.bg_title_intro5, R.drawable.bg_intro5, 5, false),
    )

    var listIntro = arrayListOf(
        IntroSplash(R.drawable.bg_title_intro1, R.drawable.bg_intro1, 0, true),
        IntroSplash(R.drawable.bg_title_intro2, R.drawable.bg_intro2, 1, false),
        IntroSplash(R.drawable.bg_title_intro3, R.drawable.bg_intro3, 2, false),
        IntroSplash(R.drawable.bg_title_intro4, R.drawable.bg_intro4, 4, true),
        IntroSplash(R.drawable.bg_title_intro5, R.drawable.bg_intro5, 5, false),
    )

    var listIntroNoAds = arrayListOf(
        IntroSplash(R.drawable.bg_title_intro1, R.drawable.bg_intro1, 0, false),
        IntroSplash(R.drawable.bg_title_intro2, R.drawable.bg_intro2, 1, false),
        IntroSplash(R.drawable.bg_title_intro3, R.drawable.bg_intro3, 2, false),
        IntroSplash(R.drawable.bg_title_intro4, R.drawable.bg_intro4, 4, false),
        IntroSplash(R.drawable.bg_title_intro5, R.drawable.bg_intro5, 5, false),
    )

    fun getListIntroUse() : ArrayList<IntroSplash>{
        val res = if (getBoolean(
                BaseApplication.instance,
                MaxUtils.ISSHOWINGNATIVE_INTRO,
                true)) {
            if (MaxUtils.isCheck(BaseApplication.instance)) {
                if (getBoolean(
                        BaseApplication.instance,
                        MaxUtils.ISSHOWINGNATIVE_INTROFULL,
                        true
                    )
                ) listIntroAds else listIntro
            } else {
                listIntro
            }
        } else {
            listIntroNoAds
        }
//        res.addAll(listPermission)
        return res
    }

    var listFragment = arrayListOf<Fragment>()
    // endregion

    init {
        checkShowGuide()
    }

    fun checkShowGuide() = viewModelScope.launch {
        showGuide.set(store.hasShowGuide.first())
    }


    fun setShowIntro() = viewModelScope.launch {
        store.setValueBoolean(true, KEY_HAS_SHOW_INTRO)
    }

    fun onClickContinue() = viewModelScope.launch {
        if (indexIntro.get() == getListIntroUse().size - 1) {
            setShowIntro()
            introEventChannel.send(IntroEvent.OnClickStartToHome)
        } else {
            indexIntro.set(indexIntro.get() + 1)
            introEventChannel.send(IntroEvent.OnClickContinue)
        }
    }

    fun onClickBackIntro() = viewModelScope.launch {
        if (indexIntro.get() > 0 && indexIntro.get() < getListIntroUse().size - 1) {
            indexIntro.set(indexIntro.get() - 1)
            introEventChannel.send(IntroEvent.OnClickBackIntro)
        }
    }


    //region onclick

    // endregion

    sealed class IntroEvent {
        object OnClickContinue : IntroEvent()
        object OnClickBackIntro : IntroEvent()
        object OnClickStartToHome : IntroEvent()
    }
}