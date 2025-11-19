package com.kuemiin.reversevoice.ui.activity.main

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.viewModelScope
import com.kuemiin.reversevoice.base.BaseViewModel
import com.kuemiin.reversevoice.data.store.DataStoreHelper
import com.kuemiin.reversevoice.utils.Constant
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val store: DataStoreHelper,
) : BaseViewModel() {

    // region Const and Fields
    private val mainEventChannel = Channel<MainEvent>()
    val mainEvent = mainEventChannel.receiveAsFlow()
    var typeBottomSelected = ObservableInt(Constant.MAIN_TYPE_HOME)
    var hasConnectedInternet : ObservableBoolean = ObservableBoolean(true)
    // endregion

    var hasChooseLanguage: ObservableBoolean = ObservableBoolean(false)
    var hasPickEvent: ObservableBoolean = ObservableBoolean(false)
    var showIntro: ObservableBoolean = ObservableBoolean(false)

    // region override/ listener method

    fun checkShowIntro() = viewModelScope.launch {
        hasChooseLanguage.set(store.hasChooseLanguage.first())
        showIntro.set(store.hasShowIntro.first())
    }

    fun onRestartApp() = viewModelScope.launch{
        mainEventChannel.send(MainEvent.OnRestartApp)
    }
    fun onConnectAgainInternet() = viewModelScope.launch{
        mainEventChannel.send(MainEvent.OnConnectAgainInternet)
    }

    // endregion

    sealed class MainEvent {
        object OnRestartApp : MainEvent()
        object OnConnectAgainInternet : MainEvent()
        object OnClickBottom : MainEvent()
    }
}