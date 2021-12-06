package org.wit.golfpoi.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface GolfPOIStoreInterface {

    fun findAllPOIs(golfPOIs: MutableLiveData<List<GolfPOIModel>>)
    fun createPOI(firebaseUser: MutableLiveData<FirebaseUser>, golfPOI: GolfPOIModel)
    fun updatePOI(golfPOI: GolfPOIModel)
    fun removePOI(position: Int)
    fun findPOI(id: Long, golfPOI: MutableLiveData<GolfPOIModel>)
    fun findByCreatedByUserId(id: Long, golfPOIs: MutableLiveData<List<GolfPOIModel>>)
    fun findUsersFavouriteCourses(id: Long, golfPOIs: MutableLiveData<List<GolfPOIModel>>)

    fun createUser(uid: String, user: GolfUserModel2)
    fun findUser(email: String, user: MutableLiveData<GolfUserModel2>)
    fun updateUser(user: GolfUserModel2)

    fun setCurrentUser(user: GolfUserModel2)
    fun getCurrentUser() : GolfUserModel2

}