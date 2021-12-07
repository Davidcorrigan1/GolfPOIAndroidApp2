package org.wit.golfpoi.models

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class GolfPOIModel2(var uid: String = "",
                        var courseTitle: String = "",
                        var courseDescription: String = "",
                        var courseProvince: String = "",
                        var coursePar: Int = 0,
                        var image: Uri = Uri.EMPTY,
                        var lat: Double = 0.0,
                        var lng: Double = 0.0,
                        var zoom: Float = 0f,
                        var createdById: String = "" ) : Parcelable

{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "courseTitle" to courseTitle,
            "courseDescription" to courseDescription,
            "courseProvince" to courseProvince,
            "coursePar" to coursePar,
            "image" to image,
            "lat" to lat,
            "lng" to lng,
            "zoom" to zoom,
            "createdById" to createdById
        )
    }
}

