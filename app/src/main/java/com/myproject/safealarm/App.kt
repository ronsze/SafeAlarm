package com.myproject.safealarm

import android.app.Application

class App: Application() {
    companion object{
        lateinit var prefs : PreferenceManager
        const val CNG_LOC = "CHANGE_LOCATION"
    }
    override fun onCreate(){
        prefs = PreferenceManager(applicationContext)
        super.onCreate()
    }
}