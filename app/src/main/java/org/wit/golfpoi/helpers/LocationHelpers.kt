package org.wit.golfpoi.helpers

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

fun checkLocationPermissions(activity: Activity) : Boolean {

    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        return true
    }
    else {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
        return false
    }
}