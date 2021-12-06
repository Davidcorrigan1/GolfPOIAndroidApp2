package org.wit.golfpoi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.firebase.auth.FirebaseUser
import org.wit.golfpoi.R
import org.wit.golfpoi.databinding.HomeBinding
import org.wit.golfpoi.databinding.NavHeaderBinding
import androidx.lifecycle.Observer
import androidx.navigation.NavDeepLinkBuilder
import org.wit.golfpoi.ui.auth.GolfLoginViewModel
import timber.log.Timber.i


class Home : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var homeBinding : HomeBinding
    private lateinit var navHeaderBinding : NavHeaderBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var golfLoginViewModel : GolfLoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        golfLoginViewModel = ViewModelProvider(this).get(GolfLoginViewModel::class.java)
        homeBinding = HomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)
        drawerLayout = homeBinding.drawerLayout

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.golfPoiListFragment, R.id.golfPoiFragment, R.id.golfPoisOverviewMapFragment), drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)

        val navView = homeBinding.navView
        navView.setupWithNavController(navController)

    }

    override fun onStart() {
        super.onStart()
        golfLoginViewModel.liveFirebaseUser.observe(this, Observer { firebaseUser ->
            if (firebaseUser != null) {
                i("Firebase Home Onstart")
                updateNavHeader(golfLoginViewModel.liveFirebaseUser.value!!)
            }
        })

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    interface ActionBarTitleSetter {
        fun setTitle(title: String?)
    }

    private fun updateNavHeader(currentUser: FirebaseUser) {
        var headerView = homeBinding.navView.getHeaderView(0)
        navHeaderBinding = NavHeaderBinding.bind(headerView)
        navHeaderBinding.navHeaderTextView.text = currentUser.email
    }

    // Triggered from the nav_drawer_menu
    fun signOut(item: MenuItem) {
        i("Firebase Nav Drawer log out")
        golfLoginViewModel.logOut()
        val pendingIntent = NavDeepLinkBuilder(this.applicationContext)
            .setGraph(R.navigation.main_navigation)
            .setDestination(R.id.golfLoginFragment)
            .createPendingIntent()
        pendingIntent.send()

    }

}