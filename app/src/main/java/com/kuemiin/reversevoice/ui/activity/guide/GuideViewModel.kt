package com.kuemiin.reversevoice.ui.activity.guide

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseViewModel
import com.kuemiin.reversevoice.data.store.DataStoreHelper
import com.kuemiin.reversevoice.data.store.DataStoreHelper.Companion.KEY_HAS_SHOW_GUIDE
import com.kuemiin.reversevoice.data.store.DataStoreHelper.Companion.KEY_HAS_SHOW_INTRO
import com.kuemiin.reversevoice.model.IntroSplash
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class GuideViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val store: DataStoreHelper
) : BaseViewModel() {

    // region Const and Fields
    private val introEventChannel = Channel<IntroEvent>()
    val introEvent = introEventChannel.receiveAsFlow()

    var indexIntro: ObservableInt = ObservableInt(0)

    var listGuide = arrayListOf(
        IntroSplash(R.string.continue_txt, R.drawable.bg_permission_notify, 3, true, isPerNotification = true),
        IntroSplash(R.string.let_start, R.drawable.bg_let_join, 4, false),
    )

    var listFragment = arrayListOf<Fragment>()
    // endregion

    fun setShowGuide() = viewModelScope.launch {
        store.setValueBoolean(true, KEY_HAS_SHOW_GUIDE)
    }

    fun onClickContinue() = viewModelScope.launch {
        if (indexIntro.get() == listGuide.size - 1) {
            setShowGuide()
            introEventChannel.send(IntroEvent.OnClickStartToHome)
        } else {
            indexIntro.set(indexIntro.get() + 1)
            introEventChannel.send(IntroEvent.OnClickContinue)
        }
    }

    fun onClickBackIntro() = viewModelScope.launch {
        if (indexIntro.get() > 0 && indexIntro.get() < listGuide.size - 1) {
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