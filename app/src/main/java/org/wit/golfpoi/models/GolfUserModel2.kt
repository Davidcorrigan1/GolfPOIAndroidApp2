package org.wit.golfpoi.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDate

@IgnoreExtraProperties
data class GolfUserModel2(var uid : String = "",
                          var userEmail : String = "",
                          var firstName : String = "",
                          var lastName : String = "",
                          var loginCount : Long = 0,
                          var favorites: List<String> = mutableListOf<String>()
)

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
        )
    }
}