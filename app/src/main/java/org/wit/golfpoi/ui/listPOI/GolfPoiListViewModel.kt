package org.wit.golfpoi.ui.listPOI

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.golfpoi.firebase.FirebaseAuthManager
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfPOIJSONStore
import org.wit.golfpoi.models.GolfPOIModel

class GolfPoiListViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var app: MainApp
    private val golfPOIs = MutableLiveData<List<GolfPOIModel>>()

    var firebaseAuthManager : FirebaseAuthManager = FirebaseAuthManager(application)
    var liveFirebaseUser : MutableLiveData<FirebaseUser> = firebaseAuthManager.liveFirebaseUser
    var loggedOut : MutableLiveData<Boolean> = firebaseAuthManager.loggedOut

    fun logOut() {
        firebaseAuthManager.logOut()
    }

    val observableGolfPOIs: LiveData<List<GolfPOIModel>>
        get() =golfPOIs

    init {
        load()

    }

    fun load() {
        golfPOIs.value = GolfPOIJSONStore(getApplication()).findAllPOIs()
    }

}