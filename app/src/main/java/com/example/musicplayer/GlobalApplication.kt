package com.example.musicplayer

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.example.musicplayer.di.helperModules
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()


        Logger.addLogAdapter(object : AndroidLogAdapter(){
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return Build.DEBUG
            }
        })

        startKoin {
            androidContext(applicationContext)
            modules(helperModules)
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}