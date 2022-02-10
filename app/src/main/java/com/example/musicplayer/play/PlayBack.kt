package com.example.musicplayer.play

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.graphics.createBitmap
import com.example.musicplayer.MusicLibrary

class PlayBack(context: Context, val listener: PlayBackInfoListener) : BasePlayBack(context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentState: Int = 0
    private var complete = false
    private var position = -1
    private var fileChange = false
    var item: MediaMetadataCompat? = null

    private fun initializePlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setOnCompletionListener {
                    listener.onPlayComplete()
                    setState(PlaybackStateCompat.STATE_PAUSED)
                }
            }
        }
    }

    private fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onPlay() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            setState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    override fun onPlayFromMediaId(mediaId: String?) {
        item = MusicLibrary.loadMetaData(mediaId)?.apply {
            fileChange = item == null || this.description.mediaId != item?.description?.mediaId
            if (complete) {
                complete = false
                fileChange = true
            }

            if (fileChange) {
                release()
            } else {
                if (!isPlaying()) {
                    play()
                } else
                    pause()
                return
            }
        }

        initializePlayer()

        try {
            mediaPlayer?.let {
                it.setDataSource(context,item?.description?.mediaUri!!)
                it.prepare()
            }
        } catch (e: Exception) {
            throw RuntimeException("Exception")
        }

        play()
    }

    override fun onStop() {
        setState(PlaybackStateCompat.STATE_STOPPED)
        release()
    }

    override fun onPause() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            setState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    private fun setState(state: Int) {
        currentState = state

        if (currentState == PlaybackStateCompat.STATE_STOPPED) {
            complete = true
        }

        var reportPosition = 0

        if (position >= 0) {
            reportPosition = position

            if (currentState == PlaybackStateCompat.STATE_PLAYING) position = -1
        } else
            reportPosition = mediaPlayer?.let {
                it.currentPosition
            } ?: 0


        val builder = PlaybackStateCompat.Builder().apply {
            setActions(getActions())
            setState(
                currentState,
                reportPosition.toLong(),
                1.0F,
                SystemClock.elapsedRealtime()
            )
        }

        listener.onPlaybackStateChanged(builder.build())
    }

    private fun getActions(): Long {
        val actions =
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

        actions or when (currentState) {
            PlaybackStateCompat.STATE_PLAYING -> PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_SEEK_TO
            PlaybackStateCompat.STATE_STOPPED -> PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY
            PlaybackStateCompat.STATE_PAUSED -> PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP
            else -> PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PLAY_PAUSE
        }
        return actions
    }


    override fun isPlaying(): Boolean = mediaPlayer != null && mediaPlayer?.isPlaying == true

    override fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
}