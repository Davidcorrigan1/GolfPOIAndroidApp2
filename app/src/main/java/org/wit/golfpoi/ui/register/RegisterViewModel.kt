package org.wit.golfpoi.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.wit.golfpoi.firebase.FirebaseAuthManager
import org.wit.golfpoi.models.GolfUserModel
import timber.log.Timber.i

class RegisterViewModel (app: Application) : AndroidViewModel(app) {

    var firebaseAuthManager : FirebaseAuthManager = FirebaseAuthManager(app)

    fun register(email: String?, password: String?, user: GolfUserModel) {
        i("Contacting Firebase to register : $email")
        firebaseAuthManager.register(email, password, user)
    }
}