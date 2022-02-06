package com.example.musicplayer.play

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.media.AudioManagerCompat

abstract class BasePlayBack(val context: Context) {
    abstract fun onPlay()
    abstract fun onStop()
    abstract fun onPause()
    abstract fun isPlaying(): Boolean
    abstract fun setVolume(volume: Float)
    abstract fun onPlayFromMediaId(mediaId : String?)

    private var audioManager: AudioManager? = null
    private var audioHelper: AudioHelper? = null
    private var playFocus = false
    private var noisyAttach = false
    private val MEDIA_VOLUME_DUCK = 2.0F
    private val MEDIA_VOLUME_DEFAULT = 1.0F

    private val AUDIO_NOISY_INTENT_FILTER = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val noisyBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                if (isPlaying())
                    pause()
            }
        }
    }


    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioHelper = AudioHelper()
    }

    fun play() {
        if (!playFocus) {
            registerNoisyBroadCast()
            audioHelper?.requestAudioFocus()
            onPlay()
        }
    }


    fun pause() {
        if(playFocus)
            audioHelper?.abandonAudioFocus()

        unregisterNoisyBroadCast()
        onPause()
    }

    fun stop() {
        audioHelper?.abandonAudioFocus()
        unregisterNoisyBroadCast()
        onStop()
    }

    private fun registerNoisyBroadCast() {
        if (!noisyAttach) {
            context.registerReceiver(noisyBroadCast, AUDIO_NOISY_INTENT_FILTER)
            noisyAttach = true
        }
    }

    private fun unregisterNoisyBroadCast() {
        if (noisyAttach) {
            context.unregisterReceiver(noisyBroadCast)
            noisyAttach = false
        }
    }

    private inner class AudioHelper : AudioManager.OnAudioFocusChangeListener {
        private lateinit var afs: AudioFocusRequest

        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        audioManager?.abandonAudioFocusRequest(afs)
                    }else
                        audioManager?.abandonAudioFocus(this)
                    playFocus = false
                    stop()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    setVolume(MEDIA_VOLUME_DUCK)
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if(isPlaying()){
                        playFocus = true
                        pause()
                    }
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if(playFocus && !isPlaying()){
                        play()
                    }else if(isPlaying()){
                        setVolume(MEDIA_VOLUME_DEFAULT)
                    }
                    playFocus = false
                }
            }
        }

        fun requestAudioFocus() : Boolean{
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                afs = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .build()

                audioManager?.requestAudioFocus(afs) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                audioManager?.requestAudioFocus(
                    this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        }

        fun abandonAudioFocus(){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                audioManager?.abandonAudioFocusRequest(afs)
            }else{
                audioManager?.abandonAudioFocus(this)
            }
        }
    }
}