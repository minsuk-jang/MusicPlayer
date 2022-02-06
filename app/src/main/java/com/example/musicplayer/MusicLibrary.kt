package com.example.musicplayer

import android.content.Context
import android.database.MergeCursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaMetadataCompat

object MusicLibrary {
    val ROOT_ID = "root_id"
    val KEY = "music_key"
    private val library = mutableMapOf(
        KEY to mutableListOf<MediaMetadataCompat>()
    )


    private val selection =
        MediaStore.Audio.Media.IS_MUSIC + " != 0 and " + MediaStore.Audio.Media.MIME_TYPE + " !='audio/ogg'"

    private val projects = arrayOf(
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME
    )


    fun loadMetaData(mediaId : String?) : MediaMetadataCompat?{
        return library[KEY]?.first { it.description.mediaId == mediaId }
    }

    /**
     * 로컬에서 음악 데이터를 가져오는 메소드
     */
    fun Context.loadLocalMusics(): MutableList<MediaBrowserCompat.MediaItem> {
        val exCur = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projects, selection, null, null
        )


        val inCur = contentResolver.query(
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
            projects, selection, null, null
        )


        val ret = mutableListOf<MediaMetadataCompat>()
        MergeCursor(arrayOf(exCur, inCur)).use { cursor ->
            while (cursor.moveToNext()) {
                val metadata = MediaMetadataCompat.Builder().also {
                    val title =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    val artist =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val id =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val album =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                    it.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                    it.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    it.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                    it.putString(
                        MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                        Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id.toString()
                        ).toString()
                    )
                    it.putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM,
                        "content://media/external/audio/albumart/$album"
                    )
                }.build()

                ret.add(metadata)
            }
        }

        library[KEY] = ret

        return ret.map {
            MediaBrowserCompat.MediaItem(
                it.description,
                FLAG_PLAYABLE
            )
        }.toMutableList()

    }
}