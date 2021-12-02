package org.wit.golfpoi.models

import java.time.LocalDate
import java.util.*

data class GolfUserModel(var id : Long = 0,
                         var userEmail : String = "",
                         var userPassword : String = "",
                         var firstName : String = "",
                         var lastName : String = "",
                         var lastLoginDate: LocalDate = LocalDate.of(2000,1,1),
                         var loginCount : Long = 0,
                         var favorites: MutableSet<Long> = mutableSetOf<Long>())

