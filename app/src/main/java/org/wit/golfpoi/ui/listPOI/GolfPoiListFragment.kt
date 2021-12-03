package org.wit.golfpoi.ui.listPOI

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.wit.golfpoi.R
import org.wit.golfpoi.adapter.GolfPOIAdapter
import org.wit.golfpoi.adapter.GolfPOIListener
import org.wit.golfpoi.databinding.FragmentGolfPoiListBinding
import org.wit.golfpoi.helpers.SwipeToDeleteCallback
import org.wit.golfpoi.helpers.SwipeToEditCallback
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfPOIModel
import org.wit.golfpoi.models.GolfUserModel
import org.wit.golfpoi.ui.auth.LoginViewModel
import timber.log.Timber.i


class GolfPoiListFragment : Fragment(), GolfPOIListener{
    private lateinit var golfPoiListViewModel : GolfPoiListViewModel
    private val loginViewModel : LoginViewModel by activityViewModels()
    lateinit var app: MainApp
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>
    private var _fragBinding: FragmentGolfPoiListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private var searchView: SearchView? = null
    private lateinit var currentUser: GolfUserModel

    // When the Fragment is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp

        i("Firebase - onCreate Entered")
        currentUser = app.golfPOIData.getCurrentUser()

        // Disable the back button here so user can't backpress to login screen
        activity?.onBackPressedDispatcher?.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                i("Firebase: Doing nothing on Backpress!")
            }
        })

        setHasOptionsMenu(true)
    }

    // When the view is created
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle? ): View? {
        _fragBinding = FragmentGolfPoiListBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        activity?.title = getString(R.string.app_name)
        i("Firebase - onCreateView Entered")

        fragBinding.recyclerView.setLayoutManager(LinearLayoutManager(activity))
        loadGolfPOIs(app.golfPOIData.getCurrentUser())

        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // remove from the recyclerview
                val adapter = fragBinding.recyclerView.adapter as GolfPOIAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                // Delete from the data source
                app.golfPOIData.removePOI(position)
                fragBinding.recyclerView.adapter?.notifyItemRemoved(position)
            }
        }

        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                // Send course data to update screen
                val action = GolfPoiListFragmentDirections.actionGolfPoiListFragmentToGolfPoiFragment(
                    app.golfPOIData.findPOI(position)
                )
                findNavController().navigate(action)
            }
        }

        loginViewModel.addFirebaseStateListener(authStateListener)
        // Attach delete touch callback to the recyclerview
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)
        // Attach Edit touch callback to the recyclerview
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)
        // Set the listeners for the floating button and the refresh of the recycler
        setFabButtonListener(fragBinding)
        registerRefreshCallback(fragBinding)

        return root
    }

    override fun onStart() {
        super.onStart()
        i("Firebase - onStart Entered")
        golfPoiListViewModel = ViewModelProvider(activity as AppCompatActivity).get(GolfPoiListViewModel::class.java)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        i("Firebase - onResume Entered")
        currentUser = app.golfPOIData.getCurrentUser()
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GolfPoiListFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    // Handle the click of the Add button to trigger navigation and send the data
    override fun onGolfPOIClick(golfPOI: GolfPOIModel) {
        val action = GolfPoiListFragmentDirections.actionGolfPoiListFragmentToGolfPoiFragment(golfPOI)
        findNavController().navigate(action)
    }

    // Implement the listener for the favourites button
    override fun onGolfPOIFavButtonClick(golfPOI: GolfPOIModel) {
        var updatedUser = app.golfPOIData.getCurrentUser()
        if (updatedUser.favorites.contains(golfPOI.id)) {
            updatedUser.favorites.remove(golfPOI.id)
        } else {
            updatedUser.favorites.add(golfPOI.id)
        }
        app.golfPOIData.updateUser(updatedUser)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
    }

    // Set the listener for the floating add button listener
    private fun setFabButtonListener (layout: FragmentGolfPoiListBinding) {
        // Listener for the Add Image button
        layout.fab.setOnClickListener {
            findNavController().navigate(R.id.action_golfPoiListFragment_to_golfPoiFragment)
        }
    }

    // Override method to load the menu resource
    // This handles the search bar functionality and filtering the course list and the toggle switch
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_golfpoilist, menu)

        // Set up the Search in the Tool bar and setup the listener for entry of text.
        val searchItem: MenuItem = menu.findItem(R.id.golfPoiSearch)
        val searchManager: SearchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = searchItem.actionView as SearchView
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))

        searchView!!.setOnQueryTextListener (object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                query?.let { loadGolfPOIs(it) }
                return true
            }
        })

        // Set up a listener for the toggle switch. This wil control showing all
        // courses of just the current users entered courses
        val userSwitch: SwitchCompat = menu.findItem(R.id.user_switch).actionView as SwitchCompat
        userSwitch.setOnCheckedChangeListener { compoundButton, switchOn ->
            if (switchOn) {
                loadGolfPOIs(app.golfPOIData.getCurrentUser().id, favourites = false)
            } else {
                loadGolfPOIs(app.golfPOIData.getCurrentUser())
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Implements a menu event handler except for search
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if (item.itemId == R.id.golfPoiSearch) {
                return false
            } else if (item.itemId == R.id.golfPoiUserFilter) {
                loadGolfPOIs(app.golfPOIData.getCurrentUser().id,favourites = true)
                return false
            } else if (item.itemId == R.id.golfLoginFragment) {
                loginViewModel.logOut()
                return false
            } else {
                return NavigationUI.onNavDestinationSelected(item,
                    requireView().findNavController()) || super.onOptionsItemSelected(item)
            }
    }

    // Register the Callback Function to refresh the recycler
    private fun registerRefreshCallback(layout: FragmentGolfPoiListBinding) {
        refreshIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { loadGolfPOIs(app.golfPOIData.getCurrentUser()) }
    }

    // Load all Golf courses function
    private fun loadGolfPOIs(currentUser: GolfUserModel) {
        showGolfPOIs(ArrayList(app.golfPOIData.findAllPOIs()), currentUser)
    }

    // Load Golf course which were created by the current user
    private fun loadGolfPOIs(id: Long, favourites: Boolean) {
        if (favourites) {
            var favouriteCourses = ArrayList(app.golfPOIData.findUsersFavouriteCourses(id))
            showGolfPOIs(favouriteCourses, currentUser)
        } else {
            var userFilteredCourses = ArrayList(app.golfPOIData.findByCreatedByUserId(id))
            showGolfPOIs(userFilteredCourses, currentUser)
        }
    }

    // Load Golf courses which match the query string entered
    private fun loadGolfPOIs(query: String) {
        if (query != "") {
            var allGolfCourse = app.golfPOIData.findAllPOIs()
            var searchResults = ArrayList(allGolfCourse.filter {
                 it.courseTitle.lowercase().contains(query.lowercase()) ||
                 it.courseDescription.lowercase().contains(query.lowercase()) ||
                 it.courseProvince.lowercase().contains(query.lowercase()) ||
                 it.coursePar.toString().contains(query.lowercase())})
            showGolfPOIs(searchResults, currentUser)
        } else {
            loadGolfPOIs(app.golfPOIData.getCurrentUser())
        }
    }

    // Bind data to adapter recycler view.
    fun showGolfPOIs (golfPOIs: ArrayList<GolfPOIModel>, currentUser: GolfUserModel) {
        fragBinding.recyclerView.adapter = GolfPOIAdapter(golfPOIs, currentUser,this)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
    }

    // defining listener callback to check user authorisation
    val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            i("Firebase authStateLister Called")
            i("Firebase User: ${firebaseUser.email}")
            app.golfPOIData.findUser(firebaseUser?.email.toString())
                ?.let { app.golfPOIData.setCurrentUser(it) }
            loadGolfPOIs(app.golfPOIData.getCurrentUser())
            fragBinding.recyclerView.adapter?.notifyDataSetChanged()
        } else {
            view?.post { findNavController().navigate(R.id.action_golfPoiListFragment_to_golfLoginFragment)}
        }
    }

}