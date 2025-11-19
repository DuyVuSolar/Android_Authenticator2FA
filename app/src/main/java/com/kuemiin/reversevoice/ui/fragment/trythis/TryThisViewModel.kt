package com.kuemiin.reversevoice.ui.fragment.trythis

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.viewModelScope
import cn.ezandroid.lib.ezfilter.extra.sticker.Bitmap2025QuizFilter
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseViewModel
import com.kuemiin.reversevoice.data.store.DataStoreHelper
import com.kuemiin.reversevoice.utils.player.AppPlayer
import com.kuemiin.reversevoice.utils.player.AudioHelper
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
class TryThisViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val repo : DataStoreHelper,
) : BaseViewModel(), AppPlayer.IAppPlayerListener {

    private val tryThisEventChannel = Channel<Event>()
    val homeEvent = tryThisEventChannel.receiveAsFlow()
    var typeEffect = Bitmap2025QuizFilter.EFFECT_SING_A_SONG
    var isLoading : ObservableBoolean = ObservableBoolean(true)
    var isLoadCompleted : ObservableBoolean = ObservableBoolean(false)
    var isShowThumb : ObservableBoolean = ObservableBoolean(true)
    private var currentTime : ObservableInt = ObservableInt(0)
    private var totalTime : ObservableInt = ObservableInt(0)

    private var isPlayAfter : Boolean = true
    private var currentTimeAfter : Long = 0

    private var mAudioHelper = AudioHelper {
        if (it) {
            pausePlayer()
        }
    }

    val appPlayer by lazy {
        AppPlayer().apply {
            listener = this@TryThisViewModel
            audioHelper = mAudioHelper
        }
    }

    //region PLAYER
    fun initVideo(playerView: PlayerView) = viewModelScope.launch{
        delay(200)
        appPlayer.playerView = playerView
        playerView.useController = false
        val fileRaw = if(typeEffect == Bitmap2025QuizFilter.EFFECT_SING_A_SONG) R.raw.video_demo_sing_a_song else R.raw.video_demo_the_voice
        appPlayer.initUri(RawResourceDataSource.buildRawResourceUri(fileRaw), isPlayAfter)
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
            tryThisEventChannel.send(Event.OnLoadVideoCompleted)
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
//        progressPercent.set(progress.toFloat())
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
            tryThisEventChannel.send(Event.VideoEnd)
        }
    }

    //endregion

    fun onClickBack() = viewModelScope.launch {
        pausePlayer()
        tryThisEventChannel.send(Event.OnClickBack)
    }

    fun OnNavigateUp() = viewModelScope.launch {
        tryThisEventChannel.send(Event.OnNavigateUp)
    }

    fun onClickTryThis() = viewModelScope.launch {
        tryThisEventChannel.send(Event.OnClickTryThis)
    }

    fun NavigateOnClickTryThis() = viewModelScope.launch {
        tryThisEventChannel.send(Event.NavigateOnClickTryThis)
    }

    fun onClickPlayPause() = viewModelScope.launch {
        if (appPlayer.isPlaying()) {
            pausePlayer()
        }else{
            playPlayer()
        }
        tryThisEventChannel.send(Event.OnClickPlayPause)
    }

    sealed class Event {
        object OnNavigateUp : Event()
        object OnClickBack : Event()
        object OnClickTryThis : Event()
        object NavigateOnClickTryThis : Event()
        object OnClickPlayPause : Event()
        object OnLoadVideoCompleted : Event()
        object VideoEnd : Event()
    }

}