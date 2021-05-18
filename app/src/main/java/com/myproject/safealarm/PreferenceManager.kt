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
    val RANGE_KM = "range_km"
    val P_KEY = "key"
    val PASSWORD = "pass"
    val CERTIFICATION = "cert"
    val ALARM_COUNT = "a_count"
    val PREDICTION = "isPred"
    val CELL_NOW = "now"
    val CELL_NEXT = "next"
    val ALARM_TIME = "alarmTime"
    val WARD_INFO_REGIST = "onWardInfo"
    val W_NAME = "name"
    val W_HEIGHT = "height"
    val W_AGE = "age"
    val G_NUMBER = "number"
    val W_SEX = "sex"
    val EXTRA = "extra"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

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

    var key: String
        get() = prefs.getString(P_KEY, "")!!
        set(value) = prefs.edit().putString(P_KEY, value).apply()

    var pass: String
        get() = prefs.getString(PASSWORD, "0000")!!
        set(value) = prefs.edit().putString(PASSWORD, value).apply()

    var cert: Boolean
        get() = prefs.getBoolean(CERTIFICATION, false)
        set(value) = prefs.edit().putBoolean(CERTIFICATION, value).apply()

    var a_count: Int
        get() = prefs.getInt(ALARM_COUNT, 0)
        set(value) = prefs.edit().putInt(ALARM_COUNT, value).apply()

    var isPred: Boolean
        get() = prefs.getBoolean(PREDICTION, false)
        set(value) = prefs.edit().putBoolean(PREDICTION, value).apply()

    var now: Int
        get() = prefs.getInt(CELL_NOW, -1)!!
        set(value) = prefs.edit().putInt(CELL_NOW, value).apply()

    var next: Int
        get() = prefs.getInt(CELL_NEXT, -1)!!
        set(value) = prefs.edit().putInt(CELL_NEXT, value).apply()

    var alarmTime: String
        get() = prefs.getString(ALARM_TIME, "")!!
        set(value) = prefs.edit().putString(ALARM_TIME, value).apply()

    var onWardInfo: Boolean
        get() = prefs.getBoolean(WARD_INFO_REGIST, false)
        set(value) = prefs.edit().putBoolean(WARD_INFO_REGIST, value).apply()

    var name: String
        get() = prefs.getString(W_NAME, "")!!
        set(value) = prefs.edit().putString(W_NAME, value).apply()

    var height: String
        get() = prefs.getString(W_HEIGHT, "")!!
        set(value) = prefs.edit().putString(W_HEIGHT, value).apply()

    var number: String
        get() = prefs.getString(G_NUMBER, "")!!
        set(value) = prefs.edit().putString(G_NUMBER, value).apply()

    var age: String
        get() = prefs.getString(W_AGE, "")!!
        set(value) = prefs.edit().putString(W_AGE, value).apply()

    var sex: Int
        get() = prefs.getInt(W_SEX, -1)!!
        set(value) = prefs.edit().putInt(W_SEX, value).apply()

    var extra: String
        get() = prefs.getString(EXTRA, "")!!
        set(value) = prefs.edit().putString(EXTRA, value).apply()
}