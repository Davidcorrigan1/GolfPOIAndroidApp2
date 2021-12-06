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
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfUserModel
import org.wit.golfpoi.models.GolfUserModel2
import org.wit.golfpoi.ui.auth.GolfLoginFragment
import org.wit.golfpoi.ui.auth.GolfLoginViewModel
import timber.log.Timber
import timber.log.Timber.i


class GolfPoiRegisterFragment : Fragment() {

    lateinit var app: MainApp
    private lateinit var golfPoiRegisterViewModel: GolfPoiRegisterViewModel
    private val golfLoginViewModel : GolfLoginViewModel by activityViewModels()

    private var _fragBinding: FragmentGolfPoiRegisterBinding? = null
    private val fragBinding get() = _fragBinding!!
    var user = GolfUserModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentGolfPoiRegisterBinding.inflate(inflater, container, false)
        val root = fragBinding?.root

        golfPoiRegisterViewModel = ViewModelProvider(activity as AppCompatActivity).get(GolfPoiRegisterViewModel::class.java)

        Timber.i("Firebase - onCreateView Entered")

        // defining listener callback
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                Timber.i("Firebase authStateLister Called")
                view?.post { findNavController().navigate(R.id.action_golfPoiRegisterFragment_to_golfPoiListFragment)}
            }
        }
        golfLoginViewModel.addFirebaseStateListener(authStateListener)
        setButtonListener(fragBinding)

        return root

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GolfLoginFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    fun setButtonListener(layout: FragmentGolfPoiRegisterBinding) {

        layout.btnRegister.setOnClickListener {

            if (validateForm()) {
                val newUser = GolfUserModel2()
                newUser.userEmail = layout.editTextEmail.text.toString()
                newUser.firstName = layout.editTextFirstName.text.toString()
                newUser.lastName = layout.editTextLastName.text.toString()
                newUser.loginCount = 1
                i("FirebaseDB Register1: $newUser")
                golfPoiRegisterViewModel.register(
                    layout.editTextEmail.text.toString(),
                    layout.editTextPassword.text.toString(),
                    newUser
                )
            }

            //"Check the user already exists for email address on DB"
            val existingUser: GolfUserModel? =
                app.golfPOIData.findUser(layout.editTextEmail.text.toString())
            if (existingUser == null) {
                if (layout.editTextEmail.text.toString() != "" &&
                    layout.editTextFirstName.text.toString() != "" &&
                    layout.editTextLastName.text.toString() != "" &&
                    layout.editTextPassword.text.toString() != ""
                ) {
                    user.userEmail = layout.editTextEmail.text.toString()
                    user.firstName = layout.editTextFirstName.text.toString()
                    user.lastName = layout.editTextLastName.text.toString()
                    app.golfPOIData.createUser(user)
                    app.golfPOIData.setCurrentUser(user)
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
                    //findNavController().navigate(R.id.action_golfPoiRegisterFragment_to_golfPoiListFragment)

                } else {
                    Snackbar
                        .make(it, R.string.register_input_message, Snackbar.LENGTH_LONG)
                        .show()
                }

            } else {
                Snackbar
                    .make(it, R.string.register_error_message, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

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

    // Chech the status of the authentication
    private fun checkStatus(error:Boolean) {
        if (error)
            Snackbar.make(requireView(), R.string.login_error_message, Snackbar.LENGTH_LONG).show()
    }

    // Validate the registration form is completed
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