package com.example.musicplayer.helper

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.musicplayer.MusicLibrary
import com.example.musicplayer.service.MusicService
import com.orhanobut.logger.Logger

class MusicHelper(private val context: Context) {
    private var browser: MediaBrowserCompat? = null
    var controller: MediaControllerCompat? = null
    private val callbacks = mutableListOf<MediaControllerCompat.Callback>()
    private val browserConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Logger.e("connect")
            browser?.let {
                controller = MediaControllerCompat(context, it.sessionToken).apply {
                    registerCallback(controllerCallback)
                }
                it.subscribe(MusicLibrary.ROOT_ID, subscriptionCallback)
            }
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }

    interface OnSubscriptionListener {
        fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>)
    }

    var listener: OnSubscriptionListener? = null

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            Logger.e("subscrption")
            listener?.onChildrenLoaded(parentId, children)
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            callbacks.forEach {
                it.onMetadataChanged(metadata)
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            callbacks.forEach {
                it.onPlaybackStateChanged(state)
            }
        }
    }

    init {
        browser = MediaBrowserCompat(
            context,
            ComponentName(context, MusicService::class.java),
            browserConnectionCallback,
            null
        )
    }

    fun connect() {
        browser?.connect()
    }

    fun disconnect() {
        browser?.disconnect()
    }

    fun registerCallback(callback: MediaControllerCompat.Callback) {
        callbacks.add(callback)

        controller?.let {
            callback.onPlaybackStateChanged(it.playbackState)
            callback.onMetadataChanged(it.metadata)
        }
    }


    fun unregisterCallback(callback: MediaControllerCompat.Callback) {
        callbacks.remove(callback)
    }
}