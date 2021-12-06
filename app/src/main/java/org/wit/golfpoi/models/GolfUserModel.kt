package org.wit.golfpoi.models

import java.time.LocalDate
import java.util.*

data class GolfUserModel(var id : Long = 0,
                         var userEmail : String = "",
                         var firstName : String = "",
                         var lastName : String = "",
                         var loginCount : Long = 0,
                         var favorites: MutableSet<Long> = mutableSetOf<Long>())

