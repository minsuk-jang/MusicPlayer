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
import com.example.musicplayer.viewModel.MainActivityViewModel
import com.orhanobut.logger.Logger
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 로컬 내부에 음악이 없을 경우, 플로우 고려
 */
class MainActivity : BaseActivity<ActivityMainBinding>(), MusicHelper.OnSubscriptionListener,
    MusicAdapter.OnItemClickListener {

    override fun layoutIds(): Int = R.layout.activity_main
    private lateinit var musicAdapter: MusicAdapter
    private val musicHelper: MusicHelper = get()
    private val vm: MainActivityViewModel by viewModel()

    override fun onItemClick(item: MediaBrowserCompat.MediaItem) {
        musicHelper.controller?.transportControls?.playFromMediaId(item.mediaId, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        binding.vm = vm

        initUI()
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
        }
    }


    override fun onControllerDone(controllerCompat: MediaControllerCompat) {
        binding.bottomController.setController(controllerCompat)
    }

    override fun onChildrenLoaded(
        parentId: String,
        children: MutableList<MediaBrowserCompat.MediaItem>
    ) {
        musicAdapter.submitList(children)
    }

    override fun onStart() {
        super.onStart()
        musicHelper.connect()

        musicHelper.run {
            registerCallback(binding.bottomController.callback)
        }
    }

    override fun onPause() {
        super.onPause()
        musicHelper.run {
            unregisterCallback(binding.bottomController.callback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicHelper.disconnect()
    }
}