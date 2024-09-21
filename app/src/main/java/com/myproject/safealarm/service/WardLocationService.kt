package com.myproject.safealarm.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class WardLocationService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 20
        private const val MAX_REQUEST_COUNT = 12
    }

    private lateinit var locationManager: LocationManager
    private var lastLat: Double = 0.0
    private var lastLng: Double = 0.0

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun registerLocationReceiver(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasFinePer = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarsePer = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (hasFinePer == PackageManager.PERMISSION_GRANTED && hasCoarsePer == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, gpsListener)
        }
    }

    private val gpsListener = LocationListener { location ->
        val correctedLocation = getCorrectedLocation(location)
        lastLat = correctedLocation.latitude
        lastLng = correctedLocation.longitude
    }

    private fun getCorrectedLocation(location: Location): LatLng {
        return if (lastLat == 0.0 && lastLng == 0.0) {
            LatLng(location.latitude, location.longitude)
        } else {
            val arr: FloatArray = floatArrayOf()
            Location.distanceBetween(lastLat, lastLng, location.latitude, location.longitude, arr)

            val distance: Float = abs(arr[0])
            val maxDistance = location.speed * 5 + 2

            if (distance >= maxDistance) {
                val distanceRatio = maxDistance / distance
                val correctedLat = lastLat + (location.latitude - lastLat) * distanceRatio
                val correctedLng = lastLng + (location.longitude - lastLng) * distanceRatio
                LatLng(correctedLat, correctedLng)
            } else {
                LatLng(location.latitude, location.longitude)
            }
        }
    }
}