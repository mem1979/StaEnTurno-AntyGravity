package com.sta.staenturno.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { cont ->
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    cont.resume(Pair(location.latitude, location.longitude))
                } else {
                    // If lastLocation is null, we might want to request a fresh update, 
                    // but for simplicity and speed in this scope, we'll try to get current location with high accuracy
                    fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).addOnSuccessListener { currentLocation ->
                        if (currentLocation != null) {
                            cont.resume(Pair(currentLocation.latitude, currentLocation.longitude))
                        } else {
                            cont.resume(null)
                        }
                    }.addOnFailureListener {
                        cont.resume(null)
                    }
                }
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
    }
}
