package com.example.musicplayer.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.example.musicplayer.R
import com.example.musicplayer.ui.MainActivity

class MusicNotification(val context: Context) {
    val manager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val play = NotificationCompat.Action(
        android.R.drawable.ic_media_play,
        "play",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PLAY
        )
    )

    private val pause = NotificationCompat.Action(
        android.R.drawable.ic_media_pause,
        "pause",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PAUSE
        )
    )

    private val next = NotificationCompat.Action(
        android.R.drawable.ic_media_next,
        "next",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
    )

    private val previous = NotificationCompat.Action(
        android.R.drawable.ic_media_previous,
        "previous",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )

    companion object {
        val NOTIFICATION_ID = 1005
        val CHANNEL_ID = "com.example.musicplayer"
        val CHANNEL_NAME = "Music Channel"
        val DESCRIPTION = "Music practice description"
        val REQ_CODE = 101
    }


    @SuppressLint("ResourceAsColor")
    fun build(
        state: PlaybackStateCompat,
        token: MediaSessionCompat.Token,
        description: MediaDescriptionCompat
    ): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(token)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
                .setColor(android.R.color.holo_orange_light)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(createIntent())
                .setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            if(state.actions != PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS){
                addAction(previous)
            }
            addAction(if(state.state == PlaybackStateCompat.STATE_PLAYING) pause else play)

            if(state.actions != PlaybackStateCompat.ACTION_SKIP_TO_NEXT){
                addAction(next)
            }

        }.build()
    }

    private fun createIntent(): PendingIntent {
        return PendingIntent.getActivity(
            context,
            REQ_CODE,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = DESCRIPTION
            }

            manager.createNotificationChannel(channel)
        }
    }
}