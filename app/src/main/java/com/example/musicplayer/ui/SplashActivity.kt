package com.example.musicplayer.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivitySplashBinding
import com.example.musicplayer.ui.base.BaseActivity

/**
 * samsung music
 * 다시 보지 않기 설정했을 경우, 설정 페이지로 이동 시켜서 유저가 직접 권한을 부여하도록 설정하는 플로우
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun layoutIds(): Int = R.layout.activity_splash
    private val permissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.all { it.value }) {
                goToMain()
            } else {
                finish()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            goToMain()
        } else
            permissionResult.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
    }

    private fun goToMain() {
        Handler(mainLooper).postDelayed(
            {
                Intent(this, MainActivity::class.java).run {
                    startActivity(this)
                }
            }, 1500L
        )
    }

}