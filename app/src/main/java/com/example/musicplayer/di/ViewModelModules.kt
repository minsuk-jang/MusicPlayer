package com.example.musicplayer.di

import com.example.musicplayer.viewModel.MainActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {
    viewModel { MainActivityViewModel() }
}