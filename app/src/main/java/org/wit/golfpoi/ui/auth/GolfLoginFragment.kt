package org.wit.golfpoi.ui.auth

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.wit.golfpoi.R
import org.wit.golfpoi.databinding.FragmentGolfLoginBinding
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfUserModel
import timber.log.Timber
import timber.log.Timber.i


class GolfLoginFragment : Fragment() {
    private val loginViewModel : LoginViewModel by activityViewModels()
    lateinit var app: MainApp
    private var _fragBinding: FragmentGolfLoginBinding? = null
    private val fragBinding get() = _fragBinding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp
        i("Firebase - onCreate Entered")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentGolfLoginBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        i("Firebase - onCreateView Entered")

        // defining listener callback to check user authorisation
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                i("Firebase authStateLister Called")
                i("Firebase User: ${firebaseUser.email}")
                app.golfPOIData.findUser(firebaseUser?.email.toString())
                    ?.let { app.golfPOIData.setCurrentUser(it) }
                view?.post { findNavController().navigate(R.id.action_golfLoginFragment_to_golfPoiListFragment)}
            }
        }

        // Setting up listeners
        setLoginButtonListener(fragBinding)
        setRegisterButtonListener(fragBinding)
        loginViewModel.addFirebaseStateListener(authStateListener)

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

    // This is the login Button listener which will authenicate with Firebase
    fun setLoginButtonListener(layout: FragmentGolfLoginBinding) {

        layout.btnLogin.setOnClickListener {
            i("Firebase - setOnClickListerner Entered")

            signIn(layout.editTextEmail.text.toString(), layout.editTextPassword.text.toString())

            // Authentication above
            // Below is getting the user object from the DB

            var loggedInUser: GolfUserModel? =
                app.golfPOIData.findUser(layout.editTextEmail.text.toString())

            if (loggedInUser != null) {
                app.golfPOIData.setCurrentUser(loggedInUser)
                //var navController = it.findNavController()
                //navController.navigate(R.id.action_golfLoginFragment_to_golfPoiListFragment)

                // Setting the logged on user name in the NavDrawer
                var textUserName = activity?.findViewById<TextView>(R.id.navTitleTextView)
                if (textUserName != null) {
                    textUserName.text =
                        app.golfPOIData.getCurrentUser().firstName.toString() + " " +
                                app.golfPOIData.getCurrentUser().lastName.toString()
                }

                // Setting the logged on user email in the NavDrawer
                var textUserEmail = activity?.findViewById<TextView>(R.id.navHeaderTextView)
                if (textUserEmail != null) {
                    textUserEmail.text = app.golfPOIData.getCurrentUser().userEmail.toString()
                }


            } else {
                i("Cannot find user: ${R.string.login_error_message}")
                Snackbar.make(it, R.string.login_error_message, Snackbar.LENGTH_LONG).show()
            }
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



}