package org.wit.golfpoi.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class GolfUserModel2(var uid : String = "",
                         var userEmail : String = "",
                         var firstName : String = "",
                         var lastName : String = "",
                         var loginCount : Long = 0,
                         var favorites: ArrayList<String> = ArrayList<String>()) : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "userEmail" to userEmail,
            "firstName" to firstName,
            "lastName" to lastName,
            "loginCount" to loginCount,
            "favorites" to favorites
        )}
}
