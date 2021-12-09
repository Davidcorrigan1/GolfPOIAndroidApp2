package org.wit.golfpoi.ui.listPOI

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.wit.golfpoi.firebase.FirebaseDBManager
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfPOIModel2
import org.wit.golfpoi.models.GolfUserModel2

class GolfPoiListViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var app: MainApp

    var golfPOIs = MutableLiveData<List<GolfPOIModel2>>()
    var currentUsersPOIs = MutableLiveData<List<GolfPOIModel2>>()
    var currentUsersFavoritePOIs = MutableLiveData<List<GolfPOIModel2>>()


    init {
        findAllPOIs()
    }


    fun findAllPOIs(){
        FirebaseDBManager.findAllPOIs(golfPOIs)
    }

    fun setOnPOIChangeListener() {
        FirebaseDBManager.setOnChangeListenerPOIs(golfPOIs)
    }

    fun updateUser(user: GolfUserModel2) {
        FirebaseDBManager.updateUser(user)
    }

    fun removePOI(golfPOI: GolfPOIModel2){
        FirebaseDBManager.removePOI(golfPOI)
    }

    fun findFavouriteCourses(uid: String) {
        FirebaseDBManager.findUsersFavouriteCourses(uid, currentUsersFavoritePOIs)
    }

    fun finUsersCourse(uid: String) {
        FirebaseDBManager.findPOIByCreatedByUserId(uid, currentUsersPOIs)
    }

}