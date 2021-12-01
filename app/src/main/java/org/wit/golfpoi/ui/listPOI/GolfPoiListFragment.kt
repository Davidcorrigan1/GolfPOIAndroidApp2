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
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfPOIModel
import org.wit.golfpoi.ui.auth.LoggedInViewModel
import org.wit.golfpoi.ui.auth.LoginViewModel
import timber.log.Timber.i


class GolfPoiListFragment : Fragment(), GolfPOIListener{
    private lateinit var golfPoiListViewModel : GolfPoiListViewModel
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val loginViewModel : LoginViewModel by activityViewModels()
    lateinit var app: MainApp
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>
    private var _fragBinding: FragmentGolfPoiListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private var searchView: SearchView? = null

    // When the Fragment is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp

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

        fragBinding.recyclerView.setLayoutManager(LinearLayoutManager(activity))
        loadGolfPOIs()

        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.adapterPosition
                i("Deleting Item At position $position")

                // remove from the recyclerview
                val adapter = fragBinding.recyclerView.adapter as GolfPOIAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                // Delete from the data source
                app.golfPOIData.removePOI(position)
                fragBinding.recyclerView.adapter?.notifyItemRemoved(position)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        loginViewModel.addFirebaseStateListener(authStateListener)
        registerRefreshCallback(fragBinding)

        return root
    }

    override fun onStart() {
        super.onStart()
        golfPoiListViewModel = ViewModelProvider(activity as AppCompatActivity).get(GolfPoiListViewModel::class.java)
    }

    override fun onPause() {
        super.onPause()
        //loginViewModel.removeFirebaseStateListener(authStateListener)
    }

    override fun onResume() {
        super.onResume()
        i("fragment resuming")
        i("${app.golfPOIData.findAllPOIs()}")
        //loginViewModel.addFirebaseStateListener(authStateListener)
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
                i("onQueryTextCHange: $newText")
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                i("onQueryTextSubmit: $query")
                query?.let { loadGolfPOIs(it) }
                return true
            }

        })

        // Set up a listener for the toggle switch. This wil control showing all
        // courses of just the current users entered courses
        val userSwitch: SwitchCompat = menu.findItem(R.id.user_switch).actionView as SwitchCompat
        userSwitch.setOnCheckedChangeListener { compoundButton, switchOn ->
            if (switchOn == true) {
                i("Switch is on")
                loadGolfPOIs(app.golfPOIData.getCurrentUser().id)
            } else {
                i("Switch is off")
                loadGolfPOIs()
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    // Implements a menu event handler except for search
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if (item.itemId == R.id.golfPoiSearch) {
                return false
            } else if (item.itemId == R.id.golfPoiUserFilter) {
                loadGolfPOIs(app.golfPOIData.getCurrentUser().id)
                return false
            } else if (item.itemId == R.id.golfLoginFragment) {
                i("Firebase GolfPoiList Log Out")
                loggedInViewModel.logOut()
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
            { loadGolfPOIs() }
    }

    // Load Golf courses function
    private fun loadGolfPOIs() {
        showGolfPOIs(ArrayList(app.golfPOIData.findAllPOIs()))
    }

    // Load Golf course which were created by the current user
    private fun loadGolfPOIs(id: Long) {
        var userFilteredCourses = ArrayList(app.golfPOIData.findByCreatedByUserId(id))
        showGolfPOIs(userFilteredCourses)
    }

    // Load Golf courses which match the query string entered
    private fun loadGolfPOIs(query: String) {
        if (query != "") {
            var allGolfCourse = app.golfPOIData.findAllPOIs()
            i("allCoursesLength: ${allGolfCourse.size}")
            var searchResults = ArrayList(allGolfCourse.filter { it.courseTitle.lowercase().contains(query.lowercase()) ||
                                                       it.courseDescription.lowercase().contains(query.lowercase()) ||
                                                       it.courseProvince.lowercase().contains(query.lowercase())})
            i("searchResultsLength: ${searchResults.size}")
            showGolfPOIs(searchResults)
        } else {
            loadGolfPOIs()
        }
    }

    // Bind data to adapter recycler view.
    fun showGolfPOIs (golfPOIs: ArrayList<GolfPOIModel>) {
        fragBinding.recyclerView.adapter = GolfPOIAdapter(golfPOIs, this)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
    }

    // defining listener callback to check user authorisation
    val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        app.golfPOIData.findUser(firebaseUser?.email.toString())
            ?.let { app.golfPOIData.setCurrentUser(it) }

        if (firebaseUser == null) {
            i("Firebase authStateLister Called from PoiList and not logged on")
            view?.post { findNavController().navigate(R.id.action_golfPoiListFragment_to_golfLoginFragment)}
        }
    }
}