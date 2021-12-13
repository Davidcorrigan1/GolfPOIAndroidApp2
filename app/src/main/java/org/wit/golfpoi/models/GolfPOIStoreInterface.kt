package org.wit.golfpoi.models

import androidx.lifecycle.MutableLiveData


interface GolfPOIStoreInterface {

    fun findAllPOIs(golfPOIs: MutableLiveData<List<GolfPOIModel2>>)
    fun setOnChangeListenerPOIs(golfPOIs: MutableLiveData<List<GolfPOIModel2>>)
    fun createPOI(golfPOI: GolfPOIModel2)
    fun updatePOI(golfPOI: GolfPOIModel2)
    fun removePOI(golfPOI: GolfPOIModel2)
    fun findPOI(position: Int): GolfPOIModel2
    fun findPOI(uid: String, golfPOI: MutableLiveData<GolfPOIModel2>)
    fun findPOIByCreatedByUserId(uid: String, golfPOIs: MutableLiveData<List<GolfPOIModel2>>)
    fun findUsersFavouriteCourses(uid: String, golfPOIs: MutableLiveData<List<GolfPOIModel2>>)

    fun createUser(user: GolfUserModel2)
    fun findUser(email: String, user: MutableLiveData<GolfUserModel2>)
    fun updateUser(user: GolfUserModel2, golfPOIs: MutableLiveData<List<GolfPOIModel2>>)

}