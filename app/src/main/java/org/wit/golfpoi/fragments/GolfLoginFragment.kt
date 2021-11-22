package org.wit.golfpoi.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.wit.golfpoi.R
import org.wit.golfpoi.databinding.FragmentGolfLoginBinding
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfUserModel
import timber.log.Timber
import timber.log.Timber.i


class GolfLoginFragment : Fragment() {

    lateinit var app: MainApp
    private var _fragBinding: FragmentGolfLoginBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var auth: FirebaseAuth
    lateinit var loader : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentGolfLoginBinding.inflate(inflater, container, false)
        val root = fragBinding?.root

        auth = FirebaseAuth.getInstance()

        setButtonListener(fragBinding)

        return root

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GolfLoginFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    fun setButtonListener(layout: FragmentGolfLoginBinding) {

        layout.btnLogin.setOnClickListener {
            i("Check the user exists or Firebase")

            signIn(layout.editTextEmail.text.toString(), layout.editTextPassword.text.toString())

            // need to include the below someone in signIn above!


            var loggedInUser: GolfUserModel? =
                app.golfPOIData.findUser(layout.editTextEmail.text.toString())

            if (loggedInUser != null && (loggedInUser.userPassword.equals(layout.editTextPassword.text.toString()))) {
                app.golfPOIData.setCurrentUser(loggedInUser)
                var navController = it.findNavController()
                navController.navigate(R.id.action_golfLoginFragment_to_golfPoiListFragment)

                // Setting the logged on user name in the NavDrawer
                var textUserName = activity?.findViewById<TextView>(R.id.navTitleTextView)
                if (textUserName != null) {
                    textUserName.text = app.golfPOIData.getCurrentUser().firstName.toString() + " " +
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

        layout.btnRegister.setOnClickListener {
            i("Sending user to register")
            findNavController().navigate(R.id.action_golfLoginFragment_to_golfPoiRegisterFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onResume() {
        super.onResume()
    }

    private fun signIn(email: String, password: String) {
        Timber.d( "signIn:$email")
        if (!validateForm()) {
            return
        }


        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity?.mainExecutor!!) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d( "signInWithEmail:success")
                    val user = auth.currentUser

                } else {
                    // If sign in fails, display a message to the user.
                    Timber.w( "signInWithEmail:failure $task.exception")
                    Toast.makeText(activity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }

                // [START_EXCLUDE]
                if (!task.isSuccessful) {
                    Toast.makeText(activity, R.string.login_error_message,
                        Toast.LENGTH_LONG).show()
                }

                // [END_EXCLUDE]
            }
        // [END sign_in_with_email]
    }

    private fun signOut() {
        auth.signOut()
    }



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