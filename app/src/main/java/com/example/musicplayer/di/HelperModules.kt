package com.example.musicplayer.di

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import com.example.musicplayer.helper.MusicHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val helperModules = module {
    single {
        MusicHelper(androidContext())
    }
}