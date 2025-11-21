package com.beemdevelopment.aegis.ui_old.fragment.proceessing

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableFloat
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ui.PlayerView
import com.beemdevelopment.aegis.data.repository.MainRepository
import com.beemdevelopment.aegis.base.BaseViewModel
import com.beemdevelopment.aegis.model.EncoderModel
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
import java.io.File
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class ProcessingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MainRepository
) : BaseViewModel(), AppPlayer.IAppPlayerListener {

    private val previewDIYEventChannel = Channel<Event>()
    val homeEvent = previewDIYEventChannel.receiveAsFlow()
    var isLoading : ObservableBoolean = ObservableBoolean(true)
    var isDownloading : ObservableBoolean = ObservableBoolean(false)

    var progressDownload: ObservableFloat = ObservableFloat(0f)
    var isLoadSuccess: ObservableBoolean = ObservableBoolean(false)
    var isPlaying: ObservableBoolean = ObservableBoolean(false)

    var isSaved = false
    var encoderModel : EncoderModel = EncoderModel()
    var delay : Long = 0
    var typeFilter : String = ""
    var pathVideo : String = ""
    var fileEncoder : String = ""

    private var mAudioHelper = AudioHelper {
        if (it) {
            pausePlayer()
        }
    }

    val appPlayer by lazy {
        AppPlayer().apply {
            listener = this@ProcessingViewModel
            audioHelper = mAudioHelper
        }
    }

    //region PLAYER
    fun initVideo(playerView: PlayerView) = viewModelScope.launch{
        appPlayer.playerView = playerView
        playerView.useController = false
        appPlayer.init(fileEncoder)

    }

    private fun playPlayer(){
        appPlayer.play()
        viewModelScope.launch{
            previewDIYEventChannel.send(Event.OnClickPlayVideo)
        }
//        hideOverlayDelay()
    }

    fun pausePlayer(){
        appPlayer.pause()
        viewModelScope.launch{
            previewDIYEventChannel.send(Event.OnClickPlayVideo)
        }
    }

    fun releaseAppPlayer(){
        appPlayer.release()
    }

    override fun onLoadStart() {
        isLoading.set(true)
    }

    override fun onLoadComplete() {
        isLoading.set(false)
        appPlayer.play()
        viewModelScope.launch {
            previewDIYEventChannel.send(Event.LoadCompleted)
        }
    }

    override fun onProgressChange(progress: Long) {
//        progressPercent.set(progress.toFloat())
//        loge("onProgressChange PreviewModel ---  $progress")
    }

    override fun onPlayerError() {
        super.onPlayerError()
        isLoading.set(false)
    }

    override fun onVideoEnd() {
        super.onVideoEnd()
        viewModelScope.launch {
            previewDIYEventChannel.send(Event.VideoEnd)
        }
    }
    //endregion


    fun onClickBack() = viewModelScope.launch {
        previewDIYEventChannel.send(Event.OnClickBack)
    }

    fun NavigateGoToHome() = viewModelScope.launch {
        previewDIYEventChannel.send(Event.NavigateGoToHome)
    }
    fun GoToHome() = viewModelScope.launch {
        previewDIYEventChannel.send(Event.GoToHome)
    }
    fun NavigateCamera() = viewModelScope.launch {
        previewDIYEventChannel.send(Event.NavigateCamera)
    }

    fun onClickShare() = viewModelScope.launch {
        previewDIYEventChannel.send(Event.OnClickShare)
    }

    fun onClickSave() = viewModelScope.launch {
        previewDIYEventChannel.send(Event.OnClickSave)
    }


    fun onClickPlayVideo() = viewModelScope.launch {
        if (appPlayer.isPlaying()) {
            pausePlayer()
        }else{
            playPlayer()
        }
    }

    fun deleteVideo(){
        if(!isSaved) File(pathVideo).delete()
    }

    fun addCollectionToDB(callback : () -> Unit) = viewModelScope.launch{
        if(isSaved) return@launch
        isSaved = true
        val myDesign = MyGalleryModel()
        myDesign.currentVideo = fileEncoder
        myDesign.typeFilter = typeFilter
        myDesign.isApplied = true

//        val allCurrent = repository.getAllVideosMyCreation().first()
//        allCurrent.forEach { it.isApplied = false }
//        repository.updateListCreation(allCurrent)
        repository.insertVideoMyCreation(myDesign)
        isLoading.set(false)
        delay(500L)
        callback.invoke()
    }

    sealed class Event {
        object OnClickShare : Event()
        object OnClickSave : Event()
        object OnClickBack : Event()
        object GoToHome : Event()
        object NavigateGoToHome : Event()
        object NavigateCamera : Event()
        object OnClickPlayVideo : Event()
//        object OnClickHome : Event()
        object LoadCompleted : Event()
        object LoadError : Event()
        object VideoEnd : Event()
    }

}