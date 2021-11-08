package org.wit.golfpoi.models

import timber.log.Timber.i

// Managing golfPOI object id.
var lastId = 1L
internal fun getId(): Long {
    return lastId++
}

class GolfPOIMemStore : GolfPOIStore {

    private val golfPOIs = ArrayList<GolfPOIModel>()
    private val users = ArrayList<GolfUserModel>()
    private var currentUser : GolfUserModel = GolfUserModel()

    override fun findAllPOIs(): List<GolfPOIModel> {
        return golfPOIs
    }

    // Add the passed in golfPOI object to the Arraylist in MemStore
    override fun createPOI(golfPOI: GolfPOIModel) {
        golfPOI.id = getId()
        golfPOI.createdById = currentUser.id
        golfPOIs.add(golfPOI)
        logAll()
    }

    override fun findPOI(id: Long): GolfPOIModel? {
        var foundGolfPOI : GolfPOIModel? = golfPOIs.find { p -> p.id == id }
        return foundGolfPOI
    }

    // Find all Golf Course entries created by the id passed in
    override fun findByCreatedByUserId(id: Long): List<GolfPOIModel> {
        return golfPOIs.filter { p -> p.createdById == id}
    }

    // Update the golfPOI object passed in as reference
    override fun updatePOI(golfPOI: GolfPOIModel) {
        i("In updatePOI: ${golfPOI}")
        var foundGolfPOI : GolfPOIModel? = golfPOIs.find { p -> p.id == golfPOI.id }
        if (foundGolfPOI != null) {
            i("in updatePOI after finding POI: ${foundGolfPOI}")
            foundGolfPOI.courseTitle = golfPOI.courseTitle
            foundGolfPOI.courseDescription = golfPOI.courseDescription
            foundGolfPOI.courseProvince = golfPOI.courseProvince
            foundGolfPOI.coursePar = golfPOI.coursePar
            foundGolfPOI.image = golfPOI.image
            foundGolfPOI.lat = golfPOI.lat
            foundGolfPOI.lng = golfPOI.lng
            foundGolfPOI.zoom = golfPOI.zoom
            logAll()
        }
    }

    // Remove the item at position passed in
    override fun removePOI(position: Int) {

        if (golfPOIs[position] != null) {
            golfPOIs.removeAt(position)
        }
    }

    // Generate a user id and add user passed in to the array
    override fun createUser(user: GolfUserModel) {
        user.id = getId()
        users.add(user)
        logAllUsers()
    }

    // Use the email address to find a user if it exists, if found check the
    // password matches the supplied. If match return the user object else null.
    override fun findUser(email: String): GolfUserModel? {
        i("user entered Email: $email")
        var userFound: GolfUserModel? = users.find{ it.userEmail == email }
        i("userFound: ${userFound}")
        if (userFound != null) {
            return userFound
        } else {
            userFound = null
        }
        return userFound
    }

    // Set the passed in user to be the current User
    override fun setCurrentUser(user: GolfUserModel) {
        currentUser = user
    }

    override fun getCurrentUser(): GolfUserModel {
        return currentUser
    }

    // Log the existing Golf Courses
    fun logAll() {
        golfPOIs.forEach{(i("${it}"))}
    }

    fun logAllUsers() {
        users.forEach{i("${it}")}
    }
}