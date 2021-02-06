package com.myproject.safealarm

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    val PREFS_FILENAME = "prefs"
    val ID = "id"
    val ID_ON = "idOn"          //id가 만들어져 있는지
    val REGIST_KEY = "regKey"
    val ROLE_KEY = "role"
    val ROOM_NUM = "room"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var id: String
        get() = prefs.getString(ID, "")!!
        set(value) = prefs.edit().putString(ID, value).apply()

    var idOn: Boolean
        get() = prefs.getBoolean(ID_ON, false)
        set(value) = prefs.edit().putBoolean(ID_ON, value).apply()

    var regKey: Boolean
        get() = prefs.getBoolean(REGIST_KEY, false)
        set(value) = prefs.edit().putBoolean(REGIST_KEY, value).apply()

    var role: String
        get() = prefs.getString(ROLE_KEY, "")!!
        set(value) = prefs.edit().putString(ROLE_KEY, value).apply()

    var room: String
        get() = prefs.getString(ROOM_NUM, "")!!
        set(value) = prefs.edit().putString(ROOM_NUM, value).apply()
}