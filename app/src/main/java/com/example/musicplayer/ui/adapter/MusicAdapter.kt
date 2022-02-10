package com.example.musicplayer.ui.adapter

import android.content.Intent
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.musicplayer.MusicLibrary
import com.example.musicplayer.R
import com.example.musicplayer.databinding.CellMusicBinding
import com.example.musicplayer.ui.MusicDetailActivity
import com.example.musicplayer.ui.base.BaseViewHolder

class MusicAdapter : ListAdapter<MediaBrowserCompat.MediaItem, MusicAdapter.MusicViewHolder>(
    diff
) {

    interface OnItemClickListener{
        fun onItemClick(item : MediaBrowserCompat.MediaItem)
    }

    var listener : OnItemClickListener? = null

    companion object {
        val diff = object : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>() {
            override fun areItemsTheSame(
                oldItem: MediaBrowserCompat.MediaItem,
                newItem: MediaBrowserCompat.MediaItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: MediaBrowserCompat.MediaItem,
                newItem: MediaBrowserCompat.MediaItem
            ): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.cell_music, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class MusicViewHolder(view: View) : BaseViewHolder<CellMusicBinding>(view) {
        init{
           binding.root.setOnClickListener {
               listener?.onItemClick(getItem(adapterPosition))
           }
        }

        fun bind(position: Int) {
            val item = MusicLibrary[getItem(position).mediaId]

            item?.let {
                val title = it.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
                val artist = it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
                val duration = it.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                val album = it.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)

                binding.let {
                    it.title = title
                    it.artist = artist
                    it.duration = duration
                    it.url = album

                    it.executePendingBindings()
                }
            }
        }
    }
}