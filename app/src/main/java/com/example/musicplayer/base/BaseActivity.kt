package com.example.musicplayer.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity(){
    @LayoutRes
    abstract fun layoutIds() : Int
    private var _binding : T? = null
    protected val binding : T get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<T>(this,layoutIds()).apply {
            _binding = this
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
