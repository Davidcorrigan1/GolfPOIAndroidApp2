package org.wit.golfpoi.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import org.wit.golfpoi.R
import org.wit.golfpoi.databinding.FragmentGolfPoiRegisterBinding
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfUserModel
import timber.log.Timber
import java.time.LocalDate

class GolfPoiRegisterFragment : Fragment() {

    lateinit var app: MainApp
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
            Timber.i("Check the user already exists for email address")
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
                    user.userPassword = layout.editTextPassword.text.toString()
                    user.lastLoginDate = LocalDate.now()
                    user.loginCount = 1
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
                    findNavController().navigate(R.id.action_golfPoiRegisterFragment_to_golfPoiListFragment)

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
            Timber.i("Sending user to Login")
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


}