package com.beemdevelopment.aegis.ui_old.fragment.main

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.beemdevelopment.aegis.base.BaseViewModel
import com.beemdevelopment.aegis.data.repository.MainRepository
import com.beemdevelopment.aegis.data.store.DataStoreHelper
import com.beemdevelopment.aegis.utils.Constant
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.beemdevelopment.aegis.data.store.DataStoreHelper.Companion.KEY_PITCH_SHIFT
import com.beemdevelopment.aegis.data.store.DataStoreHelper.Companion.KEY_PLAYBACK_SPEED
import com.beemdevelopment.aegis.model.AppAudio
import com.beemdevelopment.aegis.model.MyGalleryModel
import com.beemdevelopment.aegis.utils.FileUtils
import com.beemdevelopment.aegis.utils.doJob
import kotlinx.coroutines.Dispatchers
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class MainFragmentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val store: DataStoreHelper,
    val repository: MainRepository,
) : BaseViewModel(){

    private val mainEventChannel = Channel<MainEvent>()
    val mainEvent = mainEventChannel.receiveAsFlow()
    var listAudio = arrayListOf<AppAudio>()
    var listAll = arrayListOf<AppAudio>()//use for filter, not use for adapter, query for category

    var listCreation = arrayListOf<MyGalleryModel>()
    var isEmptyVideo: ObservableBoolean = ObservableBoolean(false)


    var typeBottomSelected = ObservableInt(Constant.MAIN_TYPE_HOME)
    var isLoading = ObservableBoolean(false)
    var isSelectMore = ObservableBoolean(false)
    var isEmptyRecording = ObservableBoolean(false)
    var isReversing = ObservableBoolean(false)
    var isSelectVideo = ObservableBoolean(false)


    var hasInitAudioAfterRecorded =
        ObservableBoolean(false)//sau khi record, sẽ có 2 file đc tạo ra -> false, hiện animation zoom, click play hoặc pause -> true, ẩn animation
    var isRecorded = ObservableBoolean(false)
    var isRecording = ObservableBoolean(false)

    var currentTempPath = ""
    var currentTempPathReverse = ""
    val liveCurrentTime by lazy { MutableLiveData("00:00") }
    private val timeFormat by lazy { DecimalFormat("00") }

    //endregion


    fun getListAudio() = viewModelScope.launch {
        doJob({
            FileUtils.getListDateGroupAudio(FileUtils.getFolderTemp().absolutePath)
        }, {
            listAll.clear()
            listAll.addAll(it.sortedByDescending { it.date })
            isEmptyRecording.set(listAll.isEmpty())
            filterList()
        }, dispathcherOut = Dispatchers.Main)
    }

    private fun filterList() = viewModelScope.launch {
        listAudio.clear()
        listAudio.addAll(listAll.filter { !it.path.contains("mp4") }.sortedByDescending { it.date })
        mainEventChannel.send(MainEvent.NotifyAdapterRecording)
    }

    // region override/ listener method
    fun setLoading(loading: Boolean) = viewModelScope.launch {
        isLoading.set(loading)
        mainEventChannel.send(MainEvent.ShowLoading)
    }

    //endregion

    fun onClickBottom(type: Int) = viewModelScope.launch {
        if (isReversing.get()) return@launch
        if (type != typeBottomSelected.get()) {
            viewModelScope.launch {
                typeBottomSelected.set(type)
                mainEventChannel.send(MainEvent.OnClickBottom)
            }
        } else {
            typeBottomSelected.set(type)
        }
    }


    fun onClickBackHome() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.BackToHome)
    }

    fun onClickReverse() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.OnClickReverse)
    }

    fun NavigateOnClickReverse() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.NavigateOnClickReverse)
    }

    fun onClickSingAlong() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.OnClickSingAlong)
    }

    fun NavigateTryThis() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.NavigateTryThis)
    }

    fun onClickTiktokVoice() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.OnClickTiktokVoice)
    }

    fun NavigateOnClickTiktokVoice() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.NavigateOnClickTiktokVoice)
    }

    fun onClickStartRecord() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.OnClickStartRecord)
    }

    fun onClickStopRecord() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.OnClickStopRecord)
    }

    fun onClickPlayRecorded() = viewModelScope.launch {
        if (typeBottomSelected.get() == Constant.MAIN_TYPE_HOME) {
            mainEventChannel.send(MainEvent.OnClickPlayRecorded)
        }

    }

    fun onClickPlayReverse() = viewModelScope.launch {

    }


    fun onChangeSpeed(speed: Float) = viewModelScope.launch {//5-20
        store.setValueFloat(speed, KEY_PLAYBACK_SPEED)
    }

    fun onChangePitchShift(pitchShift: Float) = viewModelScope.launch {//5-20
        store.setValueFloat(pitchShift, KEY_PITCH_SHIFT)
    }

    fun onClickSwLoop() = viewModelScope.launch {
    }

    fun onClickShare() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.OnClickShare)
    }

    fun onClickPolicy() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.OnClickPolicy)
    }

    //endregion

    //endregion

    sealed class MainEvent {
        //main
        object ShowLoading : MainEvent()
        object OnClickBottom : MainEvent()
        object OnClickReverse : MainEvent()
        object NavigateOnClickReverse : MainEvent()
        object OnClickSingAlong : MainEvent()
        object NavigateTryThis : MainEvent()
        object OnClickTiktokVoice : MainEvent()
        object NavigateOnClickTiktokVoice : MainEvent()
        object BackToHome : MainEvent()

        //reverse
        object OnClickStartRecord : MainEvent()
        object OnClickStopRecord : MainEvent()
        object OnClickPlayRecorded : MainEvent()
        object OnClickPlayReverse : MainEvent()

        //recording
        object NotifyAdapterVideo : MainEvent()
        object NotifyAdapterRecording : MainEvent()
        object EndAudioTabRecord : MainEvent()
        object PauseAudioTabRecord : MainEvent()
        class NavigateOnClickItemVideo(val item: MyGalleryModel) : MainEvent()
        class OnClickItemVideo(val item: MyGalleryModel) : MainEvent()
        class OnHandleDelete(val list: List<AppAudio>) : MainEvent()
        class OnHandleShare(val list: List<AppAudio>) : MainEvent()

        //setting
        object OnClickShare : MainEvent()
        object OnClickPolicy : MainEvent()
        object OnClickTermOfUse : MainEvent()
    }
}
