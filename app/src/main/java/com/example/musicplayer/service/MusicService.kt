package com.example.musicplayer.service

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.musicplayer.MusicLibrary
import com.example.musicplayer.MusicLibrary.loadLocalMusics
import com.example.musicplayer.MusicLibrary.loadMetaData
import com.example.musicplayer.notification.MusicNotification
import com.example.musicplayer.play.PlayBack
import com.example.musicplayer.play.PlayBackInfoListener
import com.example.musicplayer.ui.util.exception
import com.example.musicplayer.ui.util.supervisorJob
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*

/**
 * 1. 서비스의 세션을 이용해서 media item을 가져온다.
 * 2. 현재 실행중인 media item을 playback을 보낸다.
 */
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

        /**
         * - 순차 or 셔플 모드 확인.
         * - 사용자가 클릭한 음악이 항상 first가 되도록 기능 구현.
         * -- 만약, 클릭한 음악이 중간일 경우, 현재 클릭된 음악을 기준으로 parsing한 뒤, 앞에 있는 리스트들을 뒤로 붙여서 진행하도록 진행
         */
        override fun onSkipToNext() {
            session?.controller?.queue?.asSequence()?.let{

            }
        }

        override fun onSkipToPrevious() {
        }

    }

    /**
     * 세션은 생성하면 자동적으로 시스템에 등록되지만 setActive를 true로 해야 동작이 된다.
     */

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

            CoroutineScope( Dispatchers.IO + exception).launch {
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

    override fun onDestroy() {
        super.onDestroy()
        session?.release()
    }
}