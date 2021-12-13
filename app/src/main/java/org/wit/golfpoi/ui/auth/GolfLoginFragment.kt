package org.wit.golfpoi.ui.auth

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.wit.golfpoi.R
import org.wit.golfpoi.databinding.FragmentGolfLoginBinding
import org.wit.golfpoi.models.GolfUserModel2
import timber.log.Timber
import timber.log.Timber.i


class GolfLoginFragment : Fragment() {
    private val loginViewModel : LoginViewModel by activityViewModels()
    private var _fragBinding: FragmentGolfLoginBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var startForResult : ActivityResultLauncher<Intent>
    var googleUser = GolfUserModel2()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        i("Firebase - onCreate Entered")

        // Disable the back button here so user can't backpress to login screen
        activity?.onBackPressedDispatcher?.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                i("Firebase: Doing nothing on Backpress!")
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentGolfLoginBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.googleSignInButton.setSize(SignInButton.SIZE_WIDE)
        fragBinding.googleSignInButton.setColorScheme(0)

        i("Firebase - onCreateView Entered")

        // defining listener callback to check user authorisation
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                i("Firebase authStateLister Called")
                i("Firebase User: ${firebaseUser.email}")
                view?.post { findNavController().navigate(R.id.action_golfLoginFragment_to_golfPoiListFragment)}
            }
        }

        // Check if there was an error with the registration on Firebase Auth
        loginViewModel.firebaseAuthManager.errorStatus.observe(this, {
                    status ->checkStatus(status)
        })

        // Setting up listeners
        setLoginButtonListener(fragBinding)
        setRegisterButtonListener(fragBinding)
        setGoogleSigninListener(fragBinding)
        loginViewModel.addFirebaseStateListener(authStateListener)
        setupGoogleSignInCallback()

        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        i("Firebase - onViewCreated Entered")
        loginViewModel.firebaseAuthManager.errorStatus.observe(viewLifecycleOwner, Observer
        { status -> checkStatus(status) })
    }


    override fun onResume() {
        super.onResume()
        i("Firebase - onResume Entered")
        loginViewModel.firebaseAuthManager.errorStatus.observe(viewLifecycleOwner, Observer
        { status -> checkStatus(status) })

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GolfLoginFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    // This is the google login button listener
    fun setGoogleSigninListener(layout: FragmentGolfLoginBinding) {
        layout.googleSignInButton.setOnClickListener {
            googleSignIn()
        }


    }
    // This is the login Button listener which will authenicate with Firebase
    fun setLoginButtonListener(layout: FragmentGolfLoginBinding) {

        layout.btnLogin.setOnClickListener {
            i("Firebase - setOnClickListerner Entered")

            signIn(layout.editTextEmail.text.toString(), layout.editTextPassword.text.toString())

            // Authentication above
            // Below is getting the user object from the DB
            // Make call to Firebase user collection with the Firebase user email

            // Setting the logged on user email in the NavDrawer
            var textUserEmail = activity?.findViewById<TextView>(R.id.navHeaderTextView)
            if (textUserEmail != null) {
                textUserEmail.text = loginViewModel.liveFirebaseUser.value?.email.toString()
            }

            loginViewModel.refreshCurrentUserLiveData(loginViewModel.liveFirebaseUser.value?.email.toString())

            var textUserName = activity?.findViewById<TextView>(R.id.navTitleTextView)
            loginViewModel.currentUserCollectionData.observe(viewLifecycleOwner, Observer
            { user ->
                    if (textUserName != null) {
                        textUserName.text = user.firstName.toString() + " " +
                                user.lastName.toString()

                    }
            })

        }
    }

    // Define the Register Button callback
    fun setRegisterButtonListener(layout: FragmentGolfLoginBinding) {
        layout.btnRegister.setOnClickListener {
            i("Sending user to register")
            findNavController().navigate(R.id.action_golfLoginFragment_to_golfPoiRegisterFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        i("Firebase - onDestroyView Entered")
        _fragBinding = null
    }

    // This method will validate the form data and then authenicate with Firebase via
    // the login View Model.
    private fun signIn(email: String, password: String) {
        Timber.d( "signIn:$email")
        if (!validateForm()) {
            return
        }
        loginViewModel.login(email,password)

    }

    // Checks the status variable to see if the login has been successful on Firebase
    private fun checkStatus(error:Boolean) {
        if (error)
            Snackbar.make(requireView(), R.string.login_error_message, Snackbar.LENGTH_LONG).show()
    }

    // This method will validate the login screen entries.
    private fun validateForm(): Boolean {
        var valid = true

        val email = fragBinding.editTextEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            fragBinding.editTextEmail.error = "Required."
            valid = false
        } else {
            fragBinding.editTextEmail.error = null
        }

        val password = fragBinding.editTextPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            fragBinding.editTextPassword.error = "Required."
            valid = false
        } else {
            fragBinding.editTextPassword.error = null
        }

        return valid
    }

    private fun googleSignIn() {
        val signInIntent = loginViewModel.firebaseAuthManager.googleSignInClient.value!!.signInIntent

        startForResult.launch(signInIntent)
    }

    private fun setupGoogleSignInCallback() {
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when(result.resultCode){
                    RESULT_OK -> {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            val account = task.getResult(ApiException::class.java)
                            loginViewModel.authWithGoogle(account!!, googleUser)
                        } catch (e: ApiException) {
                            // Google Sign In failed
                            Timber.i( "Google sign in failed $e")
                            Snackbar.make(requireView(), R.string.google_auth_fail, Snackbar.LENGTH_LONG).show()
                        }
                        Timber.i("DonationX Google Result $result.data")
                    }
                    RESULT_CANCELED -> {

                    } else -> { }
                }
            }
    }


}