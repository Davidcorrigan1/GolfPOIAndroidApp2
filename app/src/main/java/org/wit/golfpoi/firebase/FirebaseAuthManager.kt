package org.wit.golfpoi.firebase

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.wit.golfpoi.models.GolfUserModel2
import timber.log.Timber
import timber.log.Timber.i

class FirebaseAuthManager(application: Application) {

    private var application: Application? = null

    var firebaseAuth: FirebaseAuth? = null
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()
    var loggedOut = MutableLiveData<Boolean>()
    var errorStatus = MutableLiveData<Boolean>()

    init {
        i("Firebase Initiating FirebaseAuthManager")
        this.application = application
        firebaseAuth = FirebaseAuth.getInstance()
        //i("Firebase User: ${firebaseAuth!!.currentUser!!.email}")
        if (firebaseAuth!!.currentUser != null) {
            i("Firebase FirebaseAuthManager: ${firebaseAuth!!.currentUser}")
            i("Firebase FirebaseAuthManager Init Set loggedput false")
            liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
            loggedOut.postValue(false)
            errorStatus.postValue(false)
        }
    }

    fun login(email: String?, password: String?) {
        firebaseAuth!!.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(application!!.mainExecutor, { task ->
                if (task.isSuccessful) {
                    Timber.i( "Firebase Login Successful: ${task.result}")
                    liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
                    loggedOut.postValue(false)
                    errorStatus.postValue(false)
                } else {
                    Timber.i( "Firebase Login Failure: $task.exception!!.message")
                    errorStatus.postValue(true)
                }
            })
    }

    fun register(email: String?, password: String?, user: GolfUserModel2) {
        firebaseAuth!!.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(application!!.mainExecutor, { task ->
                if (task.isSuccessful) {
                    Timber.i( "Firebase Registration Successful: ${task.result}")
                    liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
                    loggedOut.postValue(false)
                    errorStatus.postValue(false)

                    // update the user object with the firebase user uid
                    // add the user to Firestone collection
                    user.uid = firebaseAuth!!.currentUser?.uid!!
                    FirebaseDBManager.createUser(user)
                } else {
                    Timber.i( "Firebase Registration Failure: $task.exception!!.message")
                    errorStatus.postValue(true)
                }
            })
    }

    fun logOut() {
        firebaseAuth!!.signOut()
        loggedOut.postValue(true)
        errorStatus.postValue(false)
        i("Firebase : FirebaseAuthManager loggedout value - ${loggedOut.value}")
    }

    fun addFirebaseStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth!!.addAuthStateListener(listener)

    }

    fun removeFirebaseStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth!!.removeAuthStateListener(listener)
    }
}