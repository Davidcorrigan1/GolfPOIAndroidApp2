package org.wit.golfpoi.models

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Location(var name: String = "",
                    var lat: Double = 0.0,
                    var lng: Double = 0.0,
                    var zoom: Float = 0f) : Parcelable

