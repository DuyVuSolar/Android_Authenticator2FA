package com.kuemiin.reversevoice.utils.player

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.SeekBar
import androidx.lifecycle.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.slider.Slider
import com.kuemiin.reversevoice.BaseApplication
import com.kuemiin.reversevoice.model.AppAudio
import com.kuemiin.reversevoice.utils.FileUtils
import com.kuemiin.reversevoice.utils.loge

class AppPlayer : LifecycleObserver {

    private var isReversedRanger = false
    var playerView: PlayerView? = null
    var duration = 0L
    var currentProgress = 0L
    private var media: SimpleExoPlayer? = null
    var liveState = MutableLiveData(State.NOT_READY)
    var isPlayReverse = MutableLiveData(false)
    var pathPlaying = MutableLiveData("")
    var listener: IAppPlayerListener? = null
    var isIniting = false
    var itemAudioPlaying : AppAudio? = null
    var mSpeed = 1f
    var mPitch = 1f

    private var isResumeBelongToLifecycle = false

    private var minCut = 0L
    var maxCut = 0L
    private var repeatMode = SimpleExoPlayer.REPEAT_MODE_OFF

    var audioHelper: AudioHelper? = null

    private var delayTime = 20L

    private var currentEventListener: Player.Listener? = null

    private var thread: Thread? = null
    private var runnable = Runnable {
        media?.let {
            try {
                while (thread != null && !thread!!.isInterrupted) {
                    if (liveState.value == State.PLAYING) {
                        handler.sendEmptyMessage(0)
                        Thread.sleep(delayTime)
                    }
                }
            } catch (e: Exception) {
                loge("Error: $e")
            }
        }
    }

    private var handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            media?.let {
                val progress = it.currentPosition
//                var fixProgress = false
//                var isEnd = false
//                if (minCut != 0L || maxCut != duration) {
//                    if (isReversedRanger) {
//                        if (progress in minCut..maxCut) {
//                            progress = if (maxCut >= duration)
//                                0
//                            else maxCut + 1
//                            fixProgress = true
//                        }
//                    } else {
//                        if (progress < minCut) {
//                            progress = minCut
//                            fixProgress = true
//                        } else if (progress > maxCut) {
//                            if (media?.repeatMode == SimpleExoPlayer.REPEAT_MODE_ALL) {
//                                //loge("CutMode: Reset to min cut")
//                                progress = minCut
//                                fixProgress = true
//                            } else {
//                                //loge("CutMode: Stop at max cut")
//                                progress = maxCut
//                                fixProgress = true
//                                isEnd = true
//                            }
//                        }
//                        //loge("Progress: $progress, Fixed: $fixProgress, Range[$minCut,$maxCut]")
//                    }
//                }
//                if (fixProgress) seek(progress, true)
                currentProgress = progress
                if (isPlaying())
                    listener?.onProgressChange(currentProgress)
//                if (isEnd) {
//                    stop()
//                    listener?.onVideoEnd()
//                }
            }
        }
    }

    private fun getApplication(): Application {
        return BaseApplication.getAppInstance()
    }

    fun init(source: String, isPlayWhenReady : Boolean = true) {
        liveState.value = State.NOT_READY
        isIniting = true
        listener?.onLoadStart()
        currentProgress = 0
        isPlayReverse.value = source.contains(FileUtils.REVERSE_AUDIO)
        pathPlaying.value = source
        itemAudioPlaying = null
        val sourceEncode = source.replace(" ", "%20")
        val uri = Uri.parse(sourceEncode)

        media = SimpleExoPlayer.Builder(BaseApplication.getAppInstance(), DefaultRenderersFactory(BaseApplication.getAppInstance())).build()//,trackSelector,loadControl)

//        val dataSourceFactory = DataSource.Factory { AssetDataSource(getApplication()) }
        val dataSourceFactory = DefaultDataSourceFactory(getApplication(), Util.getUserAgent(getApplication(), "AppPlayer"))
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))

        media?.volume = 1f
        media?.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                super.onAudioSessionIdChanged(audioSessionId)
                listener?.onAudioSessionId(audioSessionId)
            }
        })

        media?.let { media ->
            val param = PlaybackParameters(mSpeed, mPitch)
            media.playbackParameters = param
        }

        currentEventListener = object : Player.Listener {
            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == ExoPlayer.STATE_READY) {
                    listener?.onLoading(false)
                    if (media != null && isIniting) {
                        if (liveState.value == State.NOT_READY) {
                            audioHelper?.requestAudio()
                            media?.volume = 1f
                            isIniting = false
                            liveState.value = State.PLAYING
                            setStateCallback()
                            val duration = media?.duration ?: 0
                            this@AppPlayer.duration = duration
                            if (maxCut == 0L)
                                maxCut = duration
                            listener?.onLoadComplete()
//                            play()
                        }
                    }
                } else if (playbackState == ExoPlayer.STATE_ENDED) {
                    stop()
                    liveState.value = State.PAUSE
                    setStateCallback()
                    listener?.onVideoEnd()
                }else if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    listener?.onLoading(true)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                media = null
                loge("Player error: $error")
                listener?.onPlayerError()
            }
        }


        media?.addListener(currentEventListener!!)
        media?.repeatMode = repeatMode
        playerView?.player = media
        media?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        media?.prepare(mediaSource)
        media?.playWhenReady = isPlayWhenReady
    }

    fun initUri(source: Uri, isPlayWhenReady : Boolean = true) {
        liveState.value = State.NOT_READY
        isIniting = true
        listener?.onLoadStart()
        currentProgress = 0
        pathPlaying.value = source.toString()
        itemAudioPlaying = null
        val uri = source

        media = SimpleExoPlayer.Builder(BaseApplication.getAppInstance(), DefaultRenderersFactory(BaseApplication.getAppInstance())).build()//,trackSelector,loadControl)

//        val dataSourceFactory = DataSource.Factory { AssetDataSource(getApplication()) }
        val dataSourceFactory = DefaultDataSourceFactory(getApplication(), Util.getUserAgent(getApplication(), "AppPlayer"))
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))

        media?.volume = 1f
        media?.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                super.onAudioSessionIdChanged(audioSessionId)
                listener?.onAudioSessionId(audioSessionId)
            }
        })

        media?.let { media ->
            val param = PlaybackParameters(mSpeed, mPitch)
            media.playbackParameters = param
        }

        currentEventListener = object : Player.Listener {
            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == ExoPlayer.STATE_READY) {
                    listener?.onLoading(false)
                    if (media != null && isIniting) {
                        if (liveState.value == State.NOT_READY) {
                            audioHelper?.requestAudio()
                            media?.volume = 1f
                            isIniting = false
                            liveState.value = State.PLAYING
                            setStateCallback()
                            val duration = media?.duration ?: 0
                            this@AppPlayer.duration = duration
                            if (maxCut == 0L)
                                maxCut = duration
                            listener?.onLoadComplete()
//                            play()
                        }
                    }
                } else if (playbackState == ExoPlayer.STATE_ENDED) {
                    stop()
                    liveState.value = State.PAUSE
                    setStateCallback()
                    listener?.onVideoEnd()
                }else if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    listener?.onLoading(true)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                media = null
                loge("Player error: $error")
                listener?.onPlayerError()
            }
        }


        media?.addListener(currentEventListener!!)
        media?.repeatMode = repeatMode
        playerView?.player = media
        media?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        media?.prepare(mediaSource)
        media?.playWhenReady = isPlayWhenReady
    }

    fun initItemTabRecording(isReverse : Boolean, item: AppAudio, isPlayWhenReady : Boolean = true) {
        val source = if(isReverse) item.getPathReverse() else item.path
        liveState.value = State.NOT_READY
        isIniting = true
        if(itemAudioPlaying != null ) listener?.onChangeStatePlay()
        listener?.onLoadStart()
        currentProgress = 0
        isPlayReverse.value = source.contains(FileUtils.REVERSE_AUDIO)
        itemAudioPlaying = item
        pathPlaying.value = source
        val sourceEncode = source.replace(" ", "%20")
        val uri = Uri.parse(sourceEncode)

        media = SimpleExoPlayer.Builder(BaseApplication.getAppInstance(), DefaultRenderersFactory(BaseApplication.getAppInstance())).build()//,trackSelector,loadControl)

//        val dataSourceFactory = DataSource.Factory { AssetDataSource(getApplication()) }
        val dataSourceFactory = DefaultDataSourceFactory(getApplication(), Util.getUserAgent(getApplication(), "AppPlayer"))
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))

        media?.volume = 1f
        media?.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                super.onAudioSessionIdChanged(audioSessionId)
                listener?.onAudioSessionId(audioSessionId)
            }
        })

        media?.let { media ->
            val param = PlaybackParameters(mSpeed, mPitch)
            media.playbackParameters = param
        }

        currentEventListener = object : Player.Listener {
            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == ExoPlayer.STATE_READY) {
                    listener?.onLoading(false)
                    if (media != null && isIniting) {
                        if (liveState.value == State.NOT_READY) {
                            audioHelper?.requestAudio()
                            media?.volume = 1f
                            isIniting = false
                            liveState.value = State.PLAYING
                            setStateCallback()
                            val duration = media?.duration ?: 0
                            this@AppPlayer.duration = duration
                            if (maxCut == 0L)
                                maxCut = duration
                            listener?.onLoadComplete()
//                            play()
                        }
                    }
                } else if (playbackState == ExoPlayer.STATE_ENDED) {
                    stop()
                    liveState.value = State.PAUSE
                    setStateCallback()
                    listener?.onVideoEnd()
                }else if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    listener?.onLoading(true)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                media = null
                loge("Player error: $error")
                listener?.onPlayerError()
            }
        }


        media?.addListener(currentEventListener!!)
        media?.repeatMode = repeatMode
        playerView?.player = media
        media?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        media?.prepare(mediaSource)
        media?.playWhenReady = isPlayWhenReady
    }

    private fun setStateCallback(){
        if(liveState.value == State.PLAYING){
            if(isPlayReverse.value == false){
                itemAudioPlaying?.isPlayingNormal = true
                itemAudioPlaying?.isPlayingReverse = false
            }else{
                itemAudioPlaying?.isPlayingReverse = true
                itemAudioPlaying?.isPlayingNormal = false
            }
        }else{
            itemAudioPlaying?.isPlayingReverse = false
            itemAudioPlaying?.isPlayingNormal = false
        }
        if(itemAudioPlaying != null ) listener?.onChangeStatePlay()
    }
    private fun clearThread() {
        thread?.interrupt()
        thread = null
    }

    fun setVolume(volume: Float) {
        this.media?.volume = volume
    }

    fun applyNewPlayerView(playerView: PlayerView?) {
        this.playerView = null
        this.playerView = playerView
        media?.let {
            this.playerView?.player = media
        }
    }

    fun play(skipCheckNotReady: Boolean = true) {
        if (!skipCheckNotReady && liveState.value == State.NOT_READY)
            return
        clearThread()
        audioHelper?.requestAudio()
        liveState.value = State.PLAYING
        setStateCallback()
        media?.playWhenReady = true
        thread = Thread(runnable)
        thread?.start()
    }

    fun stop() {
        media?.let {
            seekToMin(false)
            liveState.value = State.STOP
            setStateCallback()
            it.playWhenReady = false
            audioHelper?.stopRequestAudio()
        }
        clearThread()
    }

    fun release() {
        maxCut = 0
        minCut = 0
        duration = 0L
        stop()
        clearThread()
        if (liveState.value == State.PLAYING || liveState.value == State.PAUSE){
            liveState.value = State.STOP
            setStateCallback()
        }
        media?.let {
            try {
                currentEventListener?.let { listener ->
                    it.removeListener(listener)
                }
                currentEventListener = null
            } catch (e: Exception) {
            }

            it.stop()
            it.release()
            media = null
        }
    }

    fun pause(useAudioHelper: Boolean = true) {
        media?.let {
            liveState.value = State.PAUSE
            setStateCallback()
            currentProgress = it.currentPosition
            media?.playWhenReady = false
            if (useAudioHelper)
                audioHelper?.stopRequestAudio()
        }
        clearThread()
    }

    fun seek(progress: Long, isPlaying: Boolean = true) {
        //loge("Seek to $progress")
        currentProgress = progress
        media?.seekTo(progress)
        listener?.onProgressChange(currentProgress)

        if (!isPlaying()) {
            clearThread()
            thread = Thread(runnable)
            thread?.start()
        }
        if (isPlaying) {
            audioHelper?.requestAudio()
        }
        media?.playWhenReady = isPlaying
        if (isPlaying) {
            liveState.value = State.PLAYING
            setStateCallback()
        } else {
            if (liveState.value == State.PLAYING) {
                setStateCallback()
            }
        }
    }

    private fun seekToMin(isPlaying: Boolean = true) {
        if (isReversedRanger) {
            seek(0, isPlaying)
        } else {
            seek(minCut, isPlaying)
        }
    }

    fun setRange(minValue: Long, maxValue: Long, reversedRanger: Boolean = false) {
        isReversedRanger = reversedRanger
        this.minCut = minValue
        if (maxValue == -1L)
            this.maxCut = duration
        else
            this.maxCut = maxValue
        //loge("Range:[$minCut,$maxCut], Duration: $duration")
    }

    fun isPlaying() = liveState.value == State.PLAYING
    fun changeRepeat(): Int {
        var mode = SimpleExoPlayer.REPEAT_MODE_OFF
        if (media != null) {
            mode = if (media?.repeatMode == SimpleExoPlayer.REPEAT_MODE_ALL) {
                SimpleExoPlayer.REPEAT_MODE_OFF
            } else {
                SimpleExoPlayer.REPEAT_MODE_ALL
            }
        }
        media?.repeatMode = mode
        return mode
    }

    fun setRepeatMode(repeatMode: Int) {
        this.repeatMode = repeatMode
        media?.repeatMode = repeatMode
    }

    fun setSpeed(speed: Float) {
        media?.let { media ->
            val param = PlaybackParameters(speed)
            media.playbackParameters = param
        }
    }
    fun setSpeedAndPitch(speed: Float, pitch : Float) {
        this.mSpeed = speed
        this.mPitch = pitch
        media?.let { media ->
            val param = PlaybackParameters(speed, pitch)
            media.playbackParameters = param
        }
    }

    private var pauseByTouch = false
    fun attachWithSeekBar(
        sb: SeekBar?,
        onStartTouch: () -> Unit = {},
        onResumeWhenPauseByTouch: () -> Unit = {},
        onProgressChange: (fromUser: Boolean) -> Unit = {}
    ) {
        sb?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar?.isPressed == true) {
                    seek(progress.toLong(), false)
                    listener?.onProgressChange(progress.toLong())
                }
                onProgressChange(fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                onStartTouch()
                if (isPlaying()) {
                    pauseByTouch = true
                    pause()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (pauseByTouch) {
                    onResumeWhenPauseByTouch()
                    pauseByTouch = false
                    seek(currentProgress, true)
                }
            }

        })
    }

    fun attachWithSlider(
        slider: Slider?,
        onStartTouch: () -> Unit = {},
        onResumeWhenPauseByTouch: () -> Unit = {},
        onProgressChange: (fromUser: Boolean) -> Unit = {}
    ) {
        slider?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                onStartTouch()
                if (isPlaying()) {
                    pauseByTouch = true
                    currentProgress = slider.value.toLong()
                    //seek(slider.value.toLong(), false)
                    pause()
                }
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (pauseByTouch) {
                    onResumeWhenPauseByTouch()
                    pauseByTouch = false
                    seek(currentProgress, true)
                }
            }

        })
        slider?.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            if (slider.isPressed) {
                seek(value.toLong(), false)
                listener?.onProgressChange(value.toLong())
            }
            onProgressChange(fromUser)
        })
    }


    private var viewLifecycleOwner: LifecycleOwner? = null
    fun bindToLifecycle(viewLifecycleOwner: LifecycleOwner) {
        viewLifecycleOwner.lifecycle.removeObserver(this)
        this.viewLifecycleOwner = viewLifecycleOwner
        viewLifecycleOwner.lifecycle.addObserver(this)
    }

    private var isPauseByLifecycle = false

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
     fun onLifeCyclePause() {
        if (isPlaying()) {
            isPauseByLifecycle = true
            pause()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
     fun onLifeCycleResume() {
        if (isPauseByLifecycle && isResumeBelongToLifecycle) {
            play()
            isPauseByLifecycle = false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
     fun onLifeCycleDestroy() {
        release()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
     fun onLifeCycleStop() {

    }


    enum class State {
        READY, PLAYING, STOP, PAUSE, NOT_READY
    }

    interface IAppPlayerListener {
        fun onLoadComplete() {}
        fun onLoadStart() {}
        fun onLoading(isLoad : Boolean) {}
        fun onVideoEnd() {}
        fun onProgressChange(progress: Long) {}
        fun onPlayerError() {}
        fun onAudioSessionId(audioSession: Int) {}
        fun onChangeStatePlay() {}
    }
}