package org.wit.golfpoi.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.wit.golfpoi.firebase.FirebaseAuthManager
import org.wit.golfpoi.firebase.FirebaseDBManager
import org.wit.golfpoi.models.GolfUserModel2
import timber.log.Timber

class LoginViewModel (app: Application) : AndroidViewModel(app) {

    var firebaseAuthManager : FirebaseAuthManager = FirebaseAuthManager(app)
    var liveFirebaseUser : MutableLiveData<FirebaseUser> = firebaseAuthManager.liveFirebaseUser
    var currentUserCollectionData = MutableLiveData<GolfUserModel2>()

    fun login(email: String?, password: String?) {
        firebaseAuthManager.login(email, password)
    }

    fun logOut() {
        firebaseAuthManager.logOut()
    }

    fun addFirebaseStateListener(listener: FirebaseAuth.AuthStateListener){
        firebaseAuthManager.addFirebaseStateListener(listener)
    }

    fun removeFirebaseStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuthManager.removeFirebaseStateListener(listener)
    }

    fun refreshCurrentUserLiveData(email: String){
        FirebaseDBManager.findUser(email, currentUserCollectionData)

    }
}