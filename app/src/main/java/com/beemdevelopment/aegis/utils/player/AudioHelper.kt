package com.beemdevelopment.aegis.utils.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.beemdevelopment.aegis.BaseApplication


class AudioHelper(val onAudioLoss: (Boolean) -> Unit) {

    private val mTelephonyManager by lazy {
        BaseApplication.getAppInstance().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
    private var mAudioManager: AudioManager? = BaseApplication.getAppInstance().getSystemService(Context.AUDIO_SERVICE) as AudioManager?

    private var mediaResult : Int? = null

    private val onAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    onAudioLoss.invoke(true)
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    onAudioLoss.invoke(true)
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    onAudioLoss.invoke(false)
                }
            }
        }
    /**
     * Ngừng lắng nghe tín hiệu media
     */
    fun stopRequestAudio() {
        try {
            abandonMediaFocus()
        } catch (e: Exception) {
        }
    }

    /**
     * Yêu cầu ngừng media các app khác
     */
    fun requestAudio() {
        return try {
            if(mediaResult == null) {
                audioFocusRequest?.let { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mAudioManager?.requestAudioFocus(it)
                }}
            }
            requestAudioFocus()
        } catch (e: Exception) {

        }
    }

    private var audioFocusRequest: AudioFocusRequest? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_GAME)
                    setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    build()
                })
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(onAudioFocusChangeListener)
                build()
            }
        } else {
            null
        }

    /**
     * Tắt âm thanh từ app khác & bắt sự kiện âm thanh
     */
    private fun requestAudioFocus() {
        abandonMediaFocus()
        mTelephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        mediaResult = mAudioManager?.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

//        if (mediaResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
////            toast("Audio focus received")
//        } else {
////            toast("Audio focus NOT received")
//        }
    }

    private fun abandonMediaFocus() {
        mTelephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE)
    }
    /**
     * Xử lý khi có cuộc gọi
     */
    private val phoneListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state != TelephonyManager.CALL_STATE_IDLE) {
                onAudioLoss.invoke(true)
            } else {
//                onAudioLoss.invoke(false)
//            }
                super.onCallStateChanged(state, phoneNumber)
            }
        }
    }
}