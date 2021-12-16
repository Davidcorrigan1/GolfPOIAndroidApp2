package org.wit.golfpoi.ui.listPOI

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.wit.golfpoi.firebase.FirebaseDBManager
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfPOIModel
import org.wit.golfpoi.models.GolfUserModel

class GolfPoiListViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var app: MainApp
    var firebaseDBManager: FirebaseDBManager = FirebaseDBManager(application)
    var golfPOIs = MutableLiveData<List<GolfPOIModel>>()
    var currentUsersPOIs = MutableLiveData<List<GolfPOIModel>>()
    var currentUsersFavoritePOIs = MutableLiveData<List<GolfPOIModel>>()


    init {
        findAllPOIs()
    }


    fun findAllPOIs(){
        firebaseDBManager.findAllPOIs(golfPOIs)
    }

    fun setOnPOIChangeListener() {
        firebaseDBManager.setOnChangeListenerPOIs(golfPOIs)
    }

    fun updateUser(user: GolfUserModel) {
        firebaseDBManager.updateUser(user, currentUsersFavoritePOIs)
    }

    fun removePOI(golfPOI: GolfPOIModel){
        firebaseDBManager.removePOI(golfPOI)
    }

    fun findFavouriteCourses(uid: String) {
        firebaseDBManager.findUsersFavouriteCourses(uid, currentUsersFavoritePOIs)
    }

    fun finUsersCourse(uid: String) {
        firebaseDBManager.findPOIByCreatedByUserId(uid, currentUsersPOIs)
    }

}