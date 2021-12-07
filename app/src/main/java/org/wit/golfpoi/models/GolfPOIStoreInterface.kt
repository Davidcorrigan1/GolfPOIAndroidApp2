package org.wit.golfpoi.models


interface GolfPOIStoreInterface {

    fun findAllPOIs(): List<GolfPOIModel2>
    fun createPOI(golfPOI: GolfPOIModel2)
    fun updatePOI(golfPOI: GolfPOIModel2)
    fun removePOI(position: Int)
    fun findPOI(position: Int): GolfPOIModel2
    fun findPOI(id: Long): GolfPOIModel2?
    fun findByCreatedByUserId(id: Long): List<GolfPOIModel2>
    fun findUsersFavouriteCourses(id: Long): List<GolfPOIModel2>

    fun createUser(user: GolfUserModel2)
    fun findUser(email: String): GolfUserModel2?
    fun updateUser(user: GolfUserModel2)

    fun setCurrentUser(user: GolfUserModel2)
    fun getCurrentUser() : GolfUserModel2

}