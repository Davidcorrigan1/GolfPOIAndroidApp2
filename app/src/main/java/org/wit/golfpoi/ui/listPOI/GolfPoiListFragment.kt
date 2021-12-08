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
import org.wit.golfpoi.models.GolfPOIModel2
import org.wit.golfpoi.models.GolfUserModel2
import org.wit.golfpoi.ui.auth.LoginViewModel
import timber.log.Timber.i
import java.util.Observer


class GolfPoiListFragment : Fragment(), GolfPOIListener{

    private val loginViewModel : LoginViewModel by activityViewModels()
    private val golfPoiListViewModel : GolfPoiListViewModel by activityViewModels()
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>
    private var _fragBinding: FragmentGolfPoiListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private var searchView: SearchView? = null
    private lateinit var currentUser: GolfUserModel2

    // When the Fragment is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        i("Firebase - onCreate Entered")

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


        // Check if the currentUserCollectionData is null and if it is then triger it's
        // creation by the findUserbyEmail method from loginViewModel.
        if (loginViewModel.currentUserCollectionData.value != null) {
            currentUser = loginViewModel.currentUserCollectionData.value as GolfUserModel2
        } else {
            loginViewModel.findUserbyEmail(loginViewModel.liveFirebaseUser.value?.email.toString())
        }

        // Observe the List of all golfPOIs and load the screen when available
        golfPoiListViewModel.golfPOIs.observe(viewLifecycleOwner, { golfPOIs ->
            golfPOIs?.let {
                var localCurrentUser: GolfUserModel2
                loginViewModel.currentUserCollectionData.observe(viewLifecycleOwner, { currentUserCollectionData ->
                    localCurrentUser = loginViewModel.currentUserCollectionData.value!!
                    i("Firebase loadGolfPOIs call 1: $golfPOIs")
                    loadGolfPOIs(ArrayList(golfPOIs), localCurrentUser)
                })

            }
        })

        // Observe the currentUserCollectionData and set the currentUser in the Fragment
        loginViewModel.currentUserCollectionData.observe(viewLifecycleOwner, { currentUserCollectionData ->
            currentUser = currentUserCollectionData.copy()

        })


        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // remove from the recyclerview
                val adapter = fragBinding.recyclerView.adapter as GolfPOIAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                // Delete from the data source
                //app.golfPOIData.removePOI(position)
                fragBinding.recyclerView.adapter?.notifyItemRemoved(position)
            }
        }

        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                // Send course data to update screen
          //      val action = GolfPoiListFragmentDirections.actionGolfPoiListFragmentToGolfPoiFragment(
          //          app.golfPOIData.findPOI(position)
          //      )
          //      findNavController().navigate(action)
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
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        i("Firebase - onResume Entered")
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
    override fun onGolfPOIClick(golfPOI: GolfPOIModel2) {
        //val action = GolfPoiListFragmentDirections.actionGolfPoiListFragmentToGolfPoiFragment(golfPOI)
        //findNavController().navigate(action)
    }

    // Implement the listener for the favourites button
    override fun onGolfPOIFavButtonClick(golfPOI: GolfPOIModel2) {
        var updatedUser = loginViewModel.currentUserCollectionData.value!!
        if (updatedUser.favorites.contains(golfPOI.uid)) {
            updatedUser.favorites.remove(golfPOI.uid)
        } else {
            updatedUser.favorites.add(golfPOI.uid)
        }
        golfPoiListViewModel.updateUser(updatedUser)
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
                i("Firebase loadGolfPOIs call 3 Switch on")
                loadGolfPOIs(favourites = false)
            } else {
                i("Firebase loadGolfPOIs call 2 Switch off: ${golfPoiListViewModel.golfPOIs.value}")
                loadGolfPOIs(
                    ArrayList(golfPoiListViewModel.golfPOIs.value),
                    loginViewModel.currentUserCollectionData.value!!
                )
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Implements a menu event handler except for search
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if (item.itemId == R.id.golfPoiSearch) {
                return false
            } else if (item.itemId == R.id.golfPoiUserFilter) {
                loadGolfPOIs(favourites = true)
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
            {
                i("Firebase loadGolfPOIs call 4: ${golfPoiListViewModel.golfPOIs.value}")
                loadGolfPOIs(ArrayList(golfPoiListViewModel.golfPOIs.value),
                loginViewModel.currentUserCollectionData.value!!
            ) }
    }

    // Load all Golf courses function
    private fun loadGolfPOIs(golfPOIs: ArrayList<GolfPOIModel2>, currentUser: GolfUserModel2) {
        i("Firebase loadGolfPOIs from List: $golfPOIs")
        showGolfPOIs(golfPOIs, currentUser)
    }

    // Load Golf course which were created by the current user
    private fun loadGolfPOIs(favourites: Boolean) {
        i("Firebase loadGolfPOIs from farourites: $favourites")
        if (favourites) {
            var favouriteCourses = (golfPoiListViewModel.currentUsersFavoritePOIs.value)
            showGolfPOIs(ArrayList(favouriteCourses), currentUser)
        } else {
            var userFilteredCourses = ArrayList(golfPoiListViewModel.currentUsersPOIs.value)
            showGolfPOIs(userFilteredCourses, currentUser)
        }
    }

    // Load Golf courses which match the query string entered
    private fun loadGolfPOIs(query: String) {
        i("Firebase loadGolfPOIs from query")
        if (query != "") {
            var allGolfCourse = golfPoiListViewModel.golfPOIs.value
            var searchResults = ArrayList(allGolfCourse!!.filter {
                 it.courseTitle.lowercase().contains(query.lowercase()) ||
                 it.courseDescription.lowercase().contains(query.lowercase()) ||
                 it.courseProvince.lowercase().contains(query.lowercase()) ||
                 it.coursePar.toString().contains(query.lowercase())})
            showGolfPOIs(searchResults, currentUser)
        } else {
            i("Firebase loadGolfPOIs from query - empty: ${golfPoiListViewModel.golfPOIs.value}")
            loadGolfPOIs(ArrayList(golfPoiListViewModel.golfPOIs.value),
                loginViewModel.currentUserCollectionData.value!!
            )
        }
    }

    // Bind data to adapter recycler view.
    fun showGolfPOIs (golfPOIs: ArrayList<GolfPOIModel2>, currentUser: GolfUserModel2) {
        i("Firebase showGolfPOIs : $golfPOIs")
        fragBinding.recyclerView.adapter = GolfPOIAdapter(golfPOIs, currentUser,this)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
    }

    // defining listener callback to check user authorisation
    val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            i("Firebase authStateLister Called")
            i("Firebase User: ${firebaseUser.email}")
            golfPoiListViewModel.finUsersCourse(firebaseUser.uid)
            golfPoiListViewModel.findFavouriteCourses(firebaseUser.uid)

            /*golfPoiListViewModel.golfPOIs.observe(viewLifecycleOwner, { golfPOIs ->
                golfPOIs?.let {
                    var localCurrentUser = GolfUserModel2()
                    localCurrentUser = loginViewModel.currentUserCollectionData.value!!
                    loadGolfPOIs(ArrayList(golfPOIs), localCurrentUser)
                }
            })*/

        } else {
            view?.post { findNavController().navigate(R.id.action_golfPoiListFragment_to_golfLoginFragment)}
        }
    }

}