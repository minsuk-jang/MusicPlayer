package com.example.musicplayer.ui.view

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ViewBottomControllerBinding
import com.example.musicplayer.helper.MusicHelper
import com.orhanobut.logger.Logger

/**
 * @JvmOverloads를 넣으면 반드시 생성자에 default 값을 넣는다
 */
class BottomControllerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs,defStyle) {
    private var isPlaying = false
    val callback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            bindUI(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            isPlaying = when (state?.state) {
                PlaybackStateCompat.STATE_STOPPED -> false
                PlaybackStateCompat.STATE_PAUSED -> false
                PlaybackStateCompat.STATE_PLAYING -> true
                else -> false
            }
            bindController()
        }
    }

    private var controller: MediaControllerCompat? = null

    private val binding: ViewBottomControllerBinding by lazy {
        DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_bottom_controller,
            null,
            false
        )
    }

    init {
        initUI()
        addView(binding.root)
    }

    private fun initUI() {
        binding.imageViewPlayPause.setOnClickListener {
            when (isPlaying) {
                true -> controller?.transportControls?.pause()
                else -> controller?.let {
                    it.transportControls?.playFromMediaId(it.metadata.description.mediaId, null)
                }
            }
        }
    }

    private fun bindController() {
        binding.imageViewPlayPause.setImageResource(
            when (isPlaying) {
                true -> android.R.drawable.ic_media_pause
                else -> android.R.drawable.ic_media_play
            }
        )
    }

    fun setController(controller : MediaControllerCompat) {
        this.controller = controller
    }

    private fun bindUI(data : MediaMetadataCompat?){
        binding.let {
            it.url = data?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
            it.artist = data?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            it.title = data?.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
            it.executePendingBindings()
        }
    }
}