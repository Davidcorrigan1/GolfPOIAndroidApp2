package org.wit.golfpoi.models

interface GolfPOIStore {

    fun findAllPOIs(): List<GolfPOIModel>
    fun createPOI(golfPOI: GolfPOIModel)
    fun updatePOI(golfPOI: GolfPOIModel)
    fun removePOI(position: Int)
    fun findPOI(id: Long): GolfPOIModel?
    fun findByCreatedByUserId(id: Long): List<GolfPOIModel>

    fun createUser(user: GolfUserModel)
    fun findUser(email: String): GolfUserModel?
    fun updateUser(user: GolfUserModel)

    fun setCurrentUser(user: GolfUserModel)
    fun getCurrentUser() : GolfUserModel

}