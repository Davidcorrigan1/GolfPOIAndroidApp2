package org.wit.golfpoi.models

import androidx.lifecycle.MutableLiveData


interface GolfPOIStoreInterface {

    fun findAllPOIs(golfPOIs: MutableLiveData<List<GolfPOIModel>>)
    fun setOnChangeListenerPOIs(golfPOIs: MutableLiveData<List<GolfPOIModel>>)
    fun createPOI(golfPOI: GolfPOIModel)
    fun updatePOI(golfPOI: GolfPOIModel)
    fun removePOI(golfPOI: GolfPOIModel)
    fun findPOI(position: Int): GolfPOIModel
    fun findPOI(uid: String, golfPOI: MutableLiveData<GolfPOIModel>)
    fun findPOIByCreatedByUserId(uid: String, golfPOIs: MutableLiveData<List<GolfPOIModel>>)
    fun findUsersFavouriteCourses(uid: String, golfPOIs: MutableLiveData<List<GolfPOIModel>>)

    fun createUser(user: GolfUserModel)
    fun findUser(email: String, user: MutableLiveData<GolfUserModel>)
    fun updateUser(user: GolfUserModel, golfPOIs: MutableLiveData<List<GolfPOIModel>>)

}