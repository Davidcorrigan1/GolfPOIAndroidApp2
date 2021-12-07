package org.wit.golfpoi.firebase

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.wit.golfpoi.models.*
import timber.log.Timber.i

object FirebaseDBManager : GolfPOIStoreInterface {

    val database = Firebase.firestore


    override fun findAllPOIs(): List<GolfPOIModel2> {
        TODO("Not yet implemented")
    }

    override fun createPOI(golfPOI: GolfPOIModel2) {
        TODO("Not yet implemented")
    }

    override fun updatePOI(golfPOI: GolfPOIModel2) {
        TODO("Not yet implemented")
    }

    override fun removePOI(position: Int) {
        TODO("Not yet implemented")
    }

    override fun findPOI(position: Int): GolfPOIModel2 {
        TODO("Not yet implemented")
    }

    override fun findPOI(id: Long): GolfPOIModel2? {
        TODO("Not yet implemented")
    }

    override fun findByCreatedByUserId(id: Long): List<GolfPOIModel2> {
        TODO("Not yet implemented")
    }

    override fun findUsersFavouriteCourses(id: Long): List<GolfPOIModel2> {
        TODO("Not yet implemented")
    }

    override fun createUser(user: GolfUserModel2) {

        val userMap = user.toMap()

        database.collection("users")
            .add(userMap)
            .addOnSuccessListener { documentReference ->
                i( "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                i("Error adding document : $e")
            }
    }

    override fun findUser(email: String): GolfUserModel2? {
        TODO("Not yet implemented")
    }

    override fun updateUser(user: GolfUserModel2) {
        TODO("Not yet implemented")
    }

    override fun setCurrentUser(user: GolfUserModel2) {
        TODO("Not yet implemented")
    }

    override fun getCurrentUser(): GolfUserModel2 {
        TODO("Not yet implemented")
    }


}