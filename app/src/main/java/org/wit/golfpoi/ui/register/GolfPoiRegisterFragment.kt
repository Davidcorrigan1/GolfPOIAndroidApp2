package org.wit.golfpoi.ui.register

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.wit.golfpoi.R
import org.wit.golfpoi.databinding.FragmentGolfPoiRegisterBinding
import org.wit.golfpoi.models.GolfUserModel2
import org.wit.golfpoi.ui.auth.GolfLoginFragment
import org.wit.golfpoi.ui.auth.LoginViewModel
import timber.log.Timber

class GolfPoiRegisterFragment : Fragment() {

    private lateinit var registerViewModel: RegisterViewModel
    private val loginViewModel : LoginViewModel by activityViewModels()

    private var _fragBinding: FragmentGolfPoiRegisterBinding? = null
    private val fragBinding get() = _fragBinding!!
    var newUser = GolfUserModel2()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentGolfPoiRegisterBinding.inflate(inflater, container, false)
        val root = fragBinding?.root

        registerViewModel = ViewModelProvider(activity as AppCompatActivity).get(RegisterViewModel::class.java)

        Timber.i("Firebase - onCreateView Entered")

        // defining listener callback to check if there is a logged on firebase user
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                Timber.i("Firebase authStateLister Called")
                view?.post { findNavController().navigate(R.id.action_golfPoiRegisterFragment_to_golfPoiListFragment)}
            }
        }

        // Check if there was an error with the registration on Firebase Auth
        registerViewModel.firebaseAuthManager.errorStatus.observe(this,
            {
                status ->checkStatus(status)
        })

        loginViewModel.addFirebaseStateListener(authStateListener)
        setRegisterButtonListener(fragBinding)
        setLoginButtonListener(fragBinding)

        return root

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GolfLoginFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    // Define called back for the Register button on click listener
    fun setRegisterButtonListener(layout: FragmentGolfPoiRegisterBinding) {

        layout.btnRegister.setOnClickListener {

            if (validateForm()) {
                newUser.userEmail = layout.editTextEmail.text.toString()
                newUser.firstName = layout.editTextFirstName.text.toString()
                newUser.lastName = layout.editTextLastName.text.toString()
                newUser.loginCount = 1


                registerViewModel.register(
                    layout.editTextEmail.text.toString(),
                    layout.editTextPassword.text.toString(),
                    newUser
                )

                // Setting the logged on user email in the NavDrawer
                var textUserEmail = activity?.findViewById<TextView>(R.id.navHeaderTextView)
                if (textUserEmail != null) {
                    textUserEmail.text = loginViewModel.liveFirebaseUser.value?.email.toString()
                }

                // Setting the logged on user name in the NavDrawer
                var textUserName = activity?.findViewById<TextView>(R.id.navTitleTextView)
                if (textUserName != null) {
                    textUserName.text = fragBinding.editTextFirstName.text.toString() + " " +
                            fragBinding.editTextLastName.text.toString()
                }
            }
        }
    }

    // Define called back for the Login button on click listener
    fun setLoginButtonListener(layout: FragmentGolfPoiRegisterBinding) {
        layout.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_golfPoiRegisterFragment_to_golfLoginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onResume() {
        super.onResume()
    }

    // Chech the status of the Registration on Firebase
    private fun checkStatus(error:Boolean) {
        if (error)
            Snackbar.make(requireView(), R.string.login_error_message, Snackbar.LENGTH_LONG).show()
    }

    // Validate the registration form is completed
    private fun validateForm(): Boolean {
        var valid = true

        val firstName =  fragBinding.editTextFirstName.text.toString()
        if (TextUtils.isEmpty(firstName)) {
            fragBinding.editTextFirstName.error = "Required."
            valid = false
        } else {
            fragBinding.editTextFirstName.error = null
        }

        val lastName =  fragBinding.editTextLastName.text.toString()
        if (TextUtils.isEmpty(lastName)) {
            fragBinding.editTextLastName.error = "Required."
            valid = false
        } else {
            fragBinding.editTextLastName.error = null
        }

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