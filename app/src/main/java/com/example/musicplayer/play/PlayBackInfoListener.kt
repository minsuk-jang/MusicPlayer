package com.example.musicplayer.play

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat

interface PlayBackInfoListener {
    fun onMetadataChanged(metadata: MediaMetadataCompat?)
    fun onPlaybackStateChanged(state: PlaybackStateCompat?)
    fun onPlayComplete()
}