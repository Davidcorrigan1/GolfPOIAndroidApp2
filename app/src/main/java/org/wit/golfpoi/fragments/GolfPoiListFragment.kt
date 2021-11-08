package org.wit.golfpoi.fragments

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.wit.golfpoi.R
import org.wit.golfpoi.adapter.GolfPOIAdapter
import org.wit.golfpoi.adapter.GolfPOIListener
import org.wit.golfpoi.databinding.FragmentGolfPoiListBinding
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfPOIModel
import timber.log.Timber.i


class GolfPoiListFragment : Fragment(), GolfPOIListener{
    lateinit var app: MainApp
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>
    private var _fragBinding: FragmentGolfPoiListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private var searchView: SearchView? = null

    // When the Fragment is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp

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

        setRecyclerViewItemTouchListener(fragBinding)
        registerRefreshCallback(fragBinding)

        return root
    }

    override fun onResume() {
        super.onResume()
        i("fragment resuming")
        i("${app.golfPOIData.findAllPOIs()}")
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
    // This handles the search bar functionality and filtering the course list
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_golfpoilist, menu)
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
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Implements a menu event handler except for search
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.golfPoiSearch) {
            return false
        } else if (item.itemId == R.id.golfPoiUserFilter) {
            loadGolfPOIs(app.golfPOIData.getCurrentUser().id)
            return false
        } else {
            return NavigationUI.onNavDestinationSelected(item,
                   requireView().findNavController()) || super.onOptionsItemSelected(item)
        }
    }

    // Method to handle deleting an item with a swipe
    private fun setRecyclerViewItemTouchListener(layout:  FragmentGolfPoiListBinding) {

        // Create the callback and tell it what events to listen for
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                // Return false in onMove. You don’t want to perform any special behavior here
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                // Call onSwiped when you swipe an item in the direction specified in the ItemTouchHelper.
                // Here, you request the viewHolder parameter passed for the position of the item view,
                // and then you remove that item from your list of photos.
                // Finally, you inform the RecyclerView adapter that an item has been removed at a specific position
                val position = viewHolder.adapterPosition
                i("Deleting Item At position $position")
                app.golfPOIData.removePOI(position)
                //photosList.removeAt(position)
                layout.recyclerView.adapter!!.notifyItemRemoved(position)
            }
        }

        //4
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(layout.recyclerView)
    }

    // Register the Callback Function to refresh the recycler
    private fun registerRefreshCallback(layout: FragmentGolfPoiListBinding) {
        refreshIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { loadGolfPOIs() }
    }

    // Load Golf courses function
    private fun loadGolfPOIs() {
        showGolfPOIs(app.golfPOIData.findAllPOIs())
    }

    // Load Golf course which were created by the current user
    private fun loadGolfPOIs(id: Long) {
        var userFilteredCourses = app.golfPOIData.findByCreatedByUserId(id)
        showGolfPOIs(userFilteredCourses)
    }

    // Load Golf courses which match the query string entered
    private fun loadGolfPOIs(query: String) {
        if (query != "") {
            var allGolfCourse = app.golfPOIData.findAllPOIs()
            i("allCoursesLength: ${allGolfCourse.size}")
            var searchResults = allGolfCourse.filter { it.courseTitle.lowercase().contains(query.lowercase()) ||
                                                       it.courseDescription.lowercase().contains(query.lowercase()) ||
                                                       it.courseProvince.lowercase().contains(query.lowercase())}
            i("searchResultsLength: ${searchResults.size}")
            showGolfPOIs(searchResults)
        } else {
            loadGolfPOIs()
        }
    }

    // Bind data to adapter recycler view.
    fun showGolfPOIs (golfPOIs: List<GolfPOIModel>) {
        fragBinding.recyclerView.adapter = GolfPOIAdapter(golfPOIs, this)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
    }

}