package org.wit.golfpoi.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import org.wit.golfpoi.firebase.FirebaseAuthManager
import org.wit.golfpoi.models.GolfUserModel2
import timber.log.Timber.i

class GolfPoiRegisterViewModel (app: Application) : AndroidViewModel(app) {

    var firebaseAuthManager : FirebaseAuthManager = FirebaseAuthManager(app)
    var liveFirebaseUser : MutableLiveData<FirebaseUser> = firebaseAuthManager.liveFirebaseUser

    fun register(email: String?, password: String?, user: GolfUserModel2) {
        i("FirebaseDB Register2 : Contacting Firebase to register : $email")
        firebaseAuthManager.register(email, password, user)

    }
}