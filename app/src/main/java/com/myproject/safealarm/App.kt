package com.myproject.safealarm

import android.app.Application

class App: Application() {
    companion object{
        lateinit var prefs : PreferenceManager
    }
    override fun onCreate(){
        prefs = PreferenceManager(applicationContext)
        super.onCreate()
    }
}