package com.beemdevelopment.aegis.ui_old.fragment.preview_gallery

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableFloat
import androidx.databinding.ObservableInt
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ui.PlayerView
import com.beemdevelopment.aegis.base.BaseViewModel
import com.beemdevelopment.aegis.data.repository.MainRepository
import com.beemdevelopment.aegis.data.store.DataStoreHelper
import com.beemdevelopment.aegis.model.MyGalleryModel
import com.beemdevelopment.aegis.utils.player.AppPlayer
import com.beemdevelopment.aegis.utils.player.AudioHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class PreviewGalleryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val repo : DataStoreHelper,
    val repository : MainRepository,
) : BaseViewModel(), AppPlayer.IAppPlayerListener {

    private val previewEventChannel = Channel<Event>()
    val homeEvent = previewEventChannel.receiveAsFlow()
    var isLoading : ObservableBoolean = ObservableBoolean(true)
    var isDeleting : ObservableBoolean = ObservableBoolean(false)
    var isLoadCompleted : ObservableBoolean = ObservableBoolean(false)
    var isShowThumb : ObservableBoolean = ObservableBoolean(true)
    var isTouchingSeekbar : ObservableBoolean = ObservableBoolean(true)

    var progressPercent : ObservableFloat = ObservableFloat(0f)
    var currentTime : ObservableInt = ObservableInt(0)
    var totalTime : ObservableInt = ObservableInt(0)

    var myDesign : MyGalleryModel? = null
    private var isPlayAfter : Boolean = true
    private var currentTimeAfter : Long = 0

    private var mAudioHelper = AudioHelper {
        if (it) {
            pausePlayer()
        }
    }

    val appPlayer by lazy {
        AppPlayer().apply {
            listener = this@PreviewGalleryViewModel
            audioHelper = mAudioHelper
        }
    }

    //region PLAYER
    fun initVideo(playerView: PlayerView) = viewModelScope.launch{
        delay(200)
        appPlayer.playerView = playerView
        playerView.useController = false
        appPlayer.init(myDesign!!.currentVideo, isPlayAfter)
    }

    private fun playPlayer(){
        if(appPlayer.currentProgress == appPlayer.duration){
            seekToPlayer(0)
        }
        appPlayer.play()
    }

    fun pausePlayer(){
        appPlayer.pause()
    }
    fun releaseAppPlayer(){
        appPlayer.release()
    }
    fun seekToPlayer(seekTo : Long){
        appPlayer.seek(seekTo, isPlayAfter)
    }

    override fun onLoadStart() {
        isLoading.set(true)
    }


    override fun onLoading(isLoad: Boolean) {
        isLoading.set(isLoad)

    }
    override fun onLoadComplete() {
        viewModelScope.launch {
            previewEventChannel.send(Event.OnLoadVideoCompleted)
            delay(50)
            isLoadCompleted.set(true)
            delay(30)
            isShowThumb.set(false)
        }
        seekToPlayer(currentTimeAfter)
        if(isPlayAfter){
            playPlayer()
        }else{
            pausePlayer()
        }
        isLoading.set(false)
        totalTime.set(appPlayer.duration.toInt())
    }

    override fun onProgressChange(progress: Long) {
        currentTime.set(progress.toInt())
        progressPercent.set(progress.toFloat())
//        loge("onProgressChange PreviewModel ---  $progress")
    }

    override fun onPlayerError() {
        super.onPlayerError()
        isLoading.set(false)
//        toast("This file is error!")
    }

    override fun onVideoEnd() {
        super.onVideoEnd()
        viewModelScope.launch {
            previewEventChannel.send(Event.VideoEnd)
        }
    }

    //endregion

    fun onClickBack() = viewModelScope.launch {
//        if(isShowPreviewFullVideo.get()){
//            onClickCollapse()
//            return@launch
//        }
        previewEventChannel.send(Event.OnClickBack)
    }

    fun NavigatenavigateUp() = viewModelScope.launch {
        previewEventChannel.send(Event.NavigatenavigateUp)
    }

    fun onClickDelete() = viewModelScope.launch {
        previewEventChannel.send(Event.OnClickDelete)
    }

    fun onClickShare() = viewModelScope.launch {
        previewEventChannel.send(Event.OnClickShare)
    }


    fun onClickPlayPause() = viewModelScope.launch {
        if (appPlayer.isPlaying()) {
            pausePlayer()
        }else{
            playPlayer()
        }
        previewEventChannel.send(Event.OnClickPlayPause)
    }


    fun deleteGallery(callback :() -> Unit) = viewModelScope.launch {
        isDeleting.set(true)
        myDesign?.let {
            repository.deleteCreationsByIds(arrayListOf(it.id)).await()
            delay(1500)
            isDeleting.set(false)
            callback.invoke()
        }
    }

    sealed class Event {
        object OnClickBack : Event()
        object NavigatenavigateUp : Event()
        object OnClickDelete : Event()
        object OnClickShare : Event()
        object OnClickPlayPause : Event()
        object OnLoadVideoCompleted : Event()
        object VideoEnd : Event()
    }

}