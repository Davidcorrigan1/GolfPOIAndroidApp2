package org.wit.golfpoi.firebase

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import org.wit.golfpoi.R
import org.wit.golfpoi.models.GolfUserModel
import timber.log.Timber
import timber.log.Timber.i

class FirebaseAuthManager(application: Application) {

    private var application: Application? = null
    var firebaseDBManager: FirebaseDBManager = FirebaseDBManager(application)
    var firebaseAuth: FirebaseAuth? = null
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()
    var loggedOut = MutableLiveData<Boolean>()
    var errorStatus = MutableLiveData<Boolean>()
    var googleSignInClient = MutableLiveData<GoogleSignInClient>()

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

        configureGoogleSignIn()
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

    fun register(email: String?, password: String?, user: GolfUserModel) {
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
                    firebaseDBManager.createUser(user)
                } else {
                    Timber.i( "Firebase Registration Failure: $task.exception!!.message")
                    errorStatus.postValue(true)
                }
            })
    }

    private fun configureGoogleSignIn() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application!!.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient.value = GoogleSignIn.getClient(application!!.applicationContext,gso)
    }

    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount, googleUser: GolfUserModel) {
        Timber.i( "GolfPOI firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(application!!.mainExecutor) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update with the signed-in user's information
                    Timber.i( "signInWithCredential:success")
                    liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
                    // update the user object with the firebase user uid
                    // add the user to Firestone collection
                    googleUser.uid = firebaseAuth!!.currentUser?.uid!!
                    googleUser.userEmail = firebaseAuth!!.currentUser?.email.toString()
                    googleUser.loginCount = 1
                    googleUser.firstName = acct.givenName
                    googleUser.lastName = acct.familyName
                    googleUser.favorites = mutableListOf()
                    i("Firebase Here is the new google user: $googleUser")
                    firebaseDBManager.createUser(googleUser)

                } else {
                    // If sign in fails, display a message to the user.
                    Timber.i( "signInWithCredential:failure $task.exception")
                    errorStatus.postValue(true)
                }
            }
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