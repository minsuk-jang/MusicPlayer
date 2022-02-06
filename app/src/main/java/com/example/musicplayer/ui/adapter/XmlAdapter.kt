package com.example.musicplayer.ui.adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.musicplayer.R

@BindingAdapter(value = ["bind:Thumbnail"], requireAll = false)
fun thumbnail(view : ImageView, url : String?){
    url?.let {
        view.scaleType = ImageView.ScaleType.CENTER_CROP
        try {
            Glide.with(view)
                .load(url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(view)
        }catch (e : Exception){
            Glide.with(view)
                .load("")
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(view)
        }
    }
}

@BindingAdapter(value = ["bind:convertLongToString"], requireAll = false)
fun convertLongToString(view : TextView, duration : Long?){
    duration?.let {
        view.text = it.toString()
    }
}