package org.wit.golfpoi.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GolfPOIModel(var id: Long = 0,
                        var courseTitle: String = "",
                        var courseDescription: String = "",
                        var courseProvince: String = "",
                        var coursePar: Int = 0,
                        var image: Uri = Uri.EMPTY,
                        var lat: Double = 0.0,
                        var lng: Double = 0.0,
                        var zoom: Float = 0f,
                        var createdById: Long = 0 ) : Parcelable


