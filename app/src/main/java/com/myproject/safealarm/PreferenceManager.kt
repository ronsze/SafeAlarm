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
    val SAVE_LAT = "s_lat"
    val SAVE_LNG = "s_lng"
    val CENTER_LAT = "center_lat"
    val CENTER_LNG = "center_lng"
    val RANGE_KM = "range_km"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var range_km: Float
        get() = prefs.getFloat(RANGE_KM, 1.0f)
        set(value) = prefs.edit().putFloat(RANGE_KM, value).apply()

    var center_lat: String
        get() = prefs.getString(CENTER_LAT, "37.58090")!!
        set(value) = prefs.edit().putString(CENTER_LAT, value).apply()

    var center_lng: String
        get() = prefs.getString(CENTER_LNG, "127.07432")!!
        set(value) = prefs.edit().putString(CENTER_LNG, value).apply()

    var s_lat: String
        get() = prefs.getString(SAVE_LAT, "37.58090")!!
        set(value) = prefs.edit().putString(SAVE_LAT, value).apply()

    var s_lng: String
        get() = prefs.getString(SAVE_LNG, "127.07432")!!
        set(value) = prefs.edit().putString(SAVE_LNG, value).apply()

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