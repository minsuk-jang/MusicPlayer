package com.example.musicplayer.service

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.musicplayer.MusicLibrary
import com.example.musicplayer.MusicLibrary.loadLocalMusics
import com.example.musicplayer.MusicLibrary.loadMetaData
import com.example.musicplayer.notification.MusicNotification
import com.example.musicplayer.play.PlayBack
import com.example.musicplayer.play.PlayBackInfoListener
import com.example.musicplayer.ui.util.supervisorJob
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*

class MusicService : MediaBrowserServiceCompat() {
    private var session: MediaSessionCompat? = null
    private var playBack: PlayBack? = null
    private var musicNotification: MusicNotification? = null
    private var serviceStart = false


    private val listener = object : PlayBackInfoListener {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            session?.setMetadata(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            session?.setPlaybackState(state)
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    val item = musicNotification?.build(
                        state = state,
                        token = sessionToken!!,
                        description = playBack?.item?.description!!,
                    )


                    if (!serviceStart) {
                        ContextCompat.startForegroundService(
                            this@MusicService,
                            Intent(this@MusicService, MusicService::class.java)
                        )

                        serviceStart = true
                    }

                    startForeground(MusicNotification.NOTIFICATION_ID, item)
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    stopForeground(true)
                    stopSelf()
                    serviceStart = false
                }
                PlaybackStateCompat.STATE_PAUSED  -> {
                    stopForeground(false)

                    val item = musicNotification?.build(
                        state = state,
                        token = sessionToken!!,
                        description = playBack?.item?.description!!
                    )

                    musicNotification?.manager?.notify(MusicNotification.NOTIFICATION_ID, item)
                }
            }
        }

        override fun onPlayComplete() {
            Logger.e("Complete")
        }
    }

    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
            loadMetaData(mediaId)?.let {
                session?.setMetadata(it)

                if (session?.isActive == false)
                    session?.isActive = true
            }
        }

        override fun onPlay() {
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            onPrepareFromMediaId(mediaId, extras)
            playBack?.onPlayFromMediaId(mediaId)
        }

        override fun onPause() {
            playBack?.pause()
        }

        override fun onStop() {
            playBack?.stop()
        }

        override fun onSkipToNext() {

        }

        override fun onSkipToPrevious() {

        }
    }

    override fun onCreate() {
        super.onCreate()
        Logger.e("onCreate")
        session = MediaSessionCompat(
            applicationContext, "com.example.MusicPlayer",
            ComponentName(applicationContext, this.javaClass), null
        ).apply {
            setCallback(callback)
            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            setSessionToken(sessionToken)
        }

        playBack = PlayBack(this, listener)
        musicNotification = MusicNotification(this)
    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        Logger.e("onGetRoot")
        return BrowserRoot(MusicLibrary.ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == MusicLibrary.ROOT_ID) {

            CoroutineScope(supervisorJob).launch {
                val item = loadLocalMusics()

                //화면에 구성할 리스트
                result.sendResult(item.map {
                    MediaBrowserCompat.MediaItem(
                        it.description,
                        FLAG_PLAYABLE
                    )
                }.toMutableList())


                session?.setQueue(item.map {
                    MediaSessionCompat.QueueItem(
                        it.description,
                        it.description.mediaId?.toLong()!!
                    )
                }.toList())

            }.start()

            result.detach()
            return
        }

        result.sendResult(null)
    }
}