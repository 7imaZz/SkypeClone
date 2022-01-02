package com.tawajood.skypeclone

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MyApp: Application(){

    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate()
    }
}