package org.wit.golfpoi.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.wit.golfpoi.firebase.FirebaseAuthManager
import org.wit.golfpoi.firebase.FirebaseDBManager
import org.wit.golfpoi.models.GolfUserModel
import timber.log.Timber.i

class LoginViewModel (app: Application) : AndroidViewModel(app) {

    var firebaseAuthManager : FirebaseAuthManager = FirebaseAuthManager(app)
    var firebaseDBManager: FirebaseDBManager = FirebaseDBManager(app)
    var liveFirebaseUser : MutableLiveData<FirebaseUser> = firebaseAuthManager.liveFirebaseUser
    var currentUserCollectionData = MutableLiveData<GolfUserModel>()

    fun login(email: String?, password: String?) {
        firebaseAuthManager.login(email, password)
    }

    fun logOut() {
        firebaseAuthManager.logOut()
        currentUserCollectionData = MutableLiveData<GolfUserModel>()
        i("Firebase : CurrentUserCollection after logout: ${currentUserCollectionData.value}")
    }

    fun addFirebaseStateListener(listener: FirebaseAuth.AuthStateListener){
        firebaseAuthManager.addFirebaseStateListener(listener)
    }

    fun removeFirebaseStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuthManager.removeFirebaseStateListener(listener)
    }

    fun authWithGoogle(acct: GoogleSignInAccount, googleUser: GolfUserModel) {
        firebaseAuthManager.firebaseAuthWithGoogle(acct, googleUser)
    }

    fun refreshCurrentUserLiveData(email: String){
        firebaseDBManager.findUser(email, currentUserCollectionData)

    }
}