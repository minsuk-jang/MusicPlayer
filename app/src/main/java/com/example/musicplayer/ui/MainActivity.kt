package com.example.musicplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.helper.MusicHelper
import com.example.musicplayer.ui.adapter.MusicAdapter
import com.example.musicplayer.ui.base.BaseActivity
import com.orhanobut.logger.Logger
import org.koin.android.ext.android.get

class MainActivity : BaseActivity<ActivityMainBinding>(), MusicHelper.OnSubscriptionListener, MusicAdapter.OnItemClickListener {
    override fun layoutIds(): Int = R.layout.activity_main
    private lateinit var musicAdapter: MusicAdapter
    private val permissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.all { it.value }) {
                initUI()
            }
        }

    private val musicHelper: MusicHelper = get()

    private val callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

        }
    }

    override fun onItemClick(item: MediaBrowserCompat.MediaItem) {
        musicHelper.controller?.transportControls?.playFromMediaId(item.mediaId,null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initUI()
        } else
            permissionResult.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
    }

    private fun initUI() {
        binding.recyclerViewMusic.run {
            layoutManager = LinearLayoutManager(this@MainActivity)

            musicAdapter = MusicAdapter().apply {
                listener = this@MainActivity
            }
            adapter = musicAdapter
        }

        musicHelper.run {
            listener = this@MainActivity
            connect()
        }
    }

    override fun onChildrenLoaded(
        parentId: String,
        children: MutableList<MediaBrowserCompat.MediaItem>
    ) {
        musicAdapter.submitList(children)
    }

    override fun onStart() {
        super.onStart()
        musicHelper.registerCallback(callback)
    }

    override fun onPause() {
        super.onPause()
        musicHelper.unregisterCallback(callback)
    }
}