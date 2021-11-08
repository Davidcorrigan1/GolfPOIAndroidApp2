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
import org.wit.golfpoi.databinding.FragmentGolfLoginBinding
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfUserModel
import timber.log.Timber.i


class GolfLoginFragment : Fragment() {

    lateinit var app: MainApp
    private var _fragBinding: FragmentGolfLoginBinding? = null
    private val fragBinding get() = _fragBinding!!

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

    fun setButtonListener(layout: FragmentGolfLoginBinding) {

        layout.btnLogin.setOnClickListener {
            i("Check the user exists and check password")
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


}