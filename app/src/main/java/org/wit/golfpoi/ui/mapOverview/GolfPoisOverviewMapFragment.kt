package org.wit.golfpoi.ui.mapOverview

import android.app.SearchManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import org.wit.golfpoi.R
import org.wit.golfpoi.databinding.CardMapGolfpoiBinding
import org.wit.golfpoi.databinding.FragmentGolfPoiListBinding
import org.wit.golfpoi.databinding.FragmentGolfPoisOverviewMapBinding
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfPOIModel
import timber.log.Timber
import timber.log.Timber.i


class GolfPoisOverviewMapFragment : Fragment(), GoogleMap.OnMarkerClickListener {
    private lateinit var map: GoogleMap
    private lateinit var golfPOIs: ArrayList<GolfPOIModel>
    lateinit var app: MainApp
    private var _fragBinding: FragmentGolfPoisOverviewMapBinding? = null
    private lateinit var contentBinding: CardMapGolfpoiBinding
    private val fragBinding get() = _fragBinding!!
    private lateinit var selectedGolfPOI: GolfPOIModel
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp
        golfPOIs = app.golfPOIData.findAllPOIs() as ArrayList<GolfPOIModel>

        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentGolfPoisOverviewMapBinding.inflate(inflater, container, false)
        val root = fragBinding?.root

        contentBinding = CardMapGolfpoiBinding.bind(fragBinding.root)

        contentBinding.cardView.setOnClickListener {
            val action =
                GolfPoisOverviewMapFragmentDirections.actionGolfPoisOverviewMapFragmentToGolfPoiFragment(
                    selectedGolfPOI
                )
            findNavController().navigate(action)
        }

        contentBinding.mapView.onCreate(savedInstanceState)
        contentBinding.mapView.getMapAsync {
            map = it
            configureMap(golfPOIs)
        }
        onChipCheckedCallback(contentBinding)
        onClickFavButtionCallback(contentBinding)
        return root
    }


    // This configures the overview map based on the List of courses passed in to display
    private fun configureMap(golfPOIs: List<GolfPOIModel>) {
        map.setOnMarkerClickListener(this)
        map.uiSettings.isZoomControlsEnabled = true

        // create a BoundsBuilder object to help frame the map
        // For each golfCourse in the Arraylist from last Activity
        // if the location is populated create Latlng object
        // Include each map location in the boundsBuilder
        // Create a marker for each golf course with title and desc included.
        val boundsBuilder = LatLngBounds.builder()
        if (golfPOIs.size > 0) {
            for (golfPOI in golfPOIs) {
                if (golfPOI.lat != 0.0 && golfPOI.lng != 0.0) {
                    val latlng = LatLng(golfPOI.lat, golfPOI.lng)
                    boundsBuilder.include(latlng)
                    map.addMarker(
                        MarkerOptions().position(latlng).title(golfPOI.courseTitle)
                    )?.tag = golfPOI.id

                }
            }
            // Move the'camera' for that it zooms to show all the Golf Courses
            if (golfPOIs.size > 1) {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(),
                        1300,
                        1300,
                        50
                    )
                )
            } else
            // Zoom differently when only a single course returned.
            {
                val latlng = LatLng(golfPOIs[0].lat, golfPOIs[0].lng)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12f))
            }
            populateMarkerCard(golfPOIs.last().id)
        } else {
            val latlng = LatLng(52.490, -6.272)
            boundsBuilder.include(latlng)
            map.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    boundsBuilder.build(),
                    1300,
                    1300,
                    50
                )
            )
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GolfPoisOverviewMapFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    override fun onStop() {
        super.onStop()
        backPressedCallback.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        contentBinding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        contentBinding.mapView.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        contentBinding.mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        contentBinding.mapView.onResume()
    }

    // Back button callback
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val action =
                GolfPoisOverviewMapFragmentDirections.actionGolfPoisOverviewMapFragmentToGolfPoiListFragment()
            findNavController().navigate(action)
        }
    }

    // Callback for Favourites Chip checked
    private fun onChipCheckedCallback(layout: CardMapGolfpoiBinding) {
        layout.chip.setOnCheckedChangeListener { chip, isChecked ->
            if (isChecked) {
                val displayCourses = loadGolfPOIs(app.golfPOIData.getCurrentUser().id , favourites = true)
                if (displayCourses.size.equals(0)) {
                    displayCourses == app.golfPOIData.findAllPOIs()
                    Toast.makeText(context, "No Favourites to display, showing all courses", Toast.LENGTH_LONG).show()
                    configureMap(loadGolfPOIs())
                } else {
                    configureMap(displayCourses)
                }
            } else {
                configureMap(loadGolfPOIs())
            }
        }
    }

    // Callback for the favourites button in the Card View on Overview Map
    private fun onClickFavButtionCallback(contentBinding: CardMapGolfpoiBinding) {
        contentBinding.favoriteBtn.setOnClickListener {
            var updatedUser = app.golfPOIData.getCurrentUser()
            if (updatedUser.favorites.contains(selectedGolfPOI.id)) {
                updatedUser.favorites.remove(selectedGolfPOI.id)
            } else {
                updatedUser.favorites.add(selectedGolfPOI.id)
            }
            app.golfPOIData.updateUser(updatedUser)
            populateMarkerCard(selectedGolfPOI.id)
        }
    }

    // Callback for click of a Marker
    override fun onMarkerClick(marker: Marker): Boolean {
        i("marker id: ${marker.tag}")
        populateMarkerCard (marker.tag!!)
        return false
    }

    // Populate the Card for marker clicked
    private fun populateMarkerCard (id: Any) {
        var golfPOIMarker = golfPOIs.find { golfPOI -> golfPOI.id == id }
        if (golfPOIMarker != null) {
            contentBinding.golfPOITitle.text = golfPOIMarker.courseTitle
            contentBinding.golfPOIDesc.text = golfPOIMarker.courseDescription
            contentBinding.golfPOIProvince.text = golfPOIMarker.courseProvince

            contentBinding.golfPOIPar.text = "  Par: ${golfPOIMarker.coursePar}"

            // Set the favourites icon if the course is a favourite
            if (app.golfPOIData.getCurrentUser().favorites.contains(golfPOIMarker.id)) {
                contentBinding.favoriteBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
            } else {
                contentBinding.favoriteBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }

            // Show default image if none available
            if (golfPOIMarker.image != null) {
                if (golfPOIMarker.image.equals(Uri.EMPTY)) {
                    contentBinding.imageIcon.setImageResource(R.drawable.golflogo)
                } else {
                    Picasso.get().load(golfPOIMarker.image).resize(200,200).into(contentBinding.imageIcon)
                }
            }
            selectedGolfPOI = golfPOIMarker
        }
    }

    // This handles the search bar functionality and filtering the course list and the toggle switch
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_golfpoi_map, menu)


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
                query?.let {configureMap(loadGolfPOIs(it)) }
                return true
            }
        })

        // Set up a listener for the toggle switch. This will control showing all
        // courses of just the current users entered courses
        val userSwitch: SwitchCompat = menu.findItem(R.id.user_switch).actionView as SwitchCompat
        userSwitch.setOnCheckedChangeListener { compoundButton, switchOn ->
            if (switchOn) {
                val displayCourses = loadGolfPOIs(app.golfPOIData.getCurrentUser().id)
                if (displayCourses.size.equals(0)) {
                    displayCourses == app.golfPOIData.findAllPOIs()
                    Toast.makeText(context, "No User courses to display, showing all courses", Toast.LENGTH_SHORT).show()
                    configureMap(loadGolfPOIs())
                } else {
                    configureMap(displayCourses)
                }
            } else {
                configureMap(loadGolfPOIs())
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Load Golf courses function
    private fun loadGolfPOIs(): List<GolfPOIModel> {
        return app.golfPOIData.findAllPOIs()
    }

    // Load Golf course which were created by the current user
    private fun loadGolfPOIs(id: Long): List<GolfPOIModel> {
        return app.golfPOIData.findByCreatedByUserId(id)
    }

    // Load Golf courses which match the query string entered
    private fun loadGolfPOIs(query: String) : List<GolfPOIModel> {
        if (query != "") {
            var allGolfCourse = app.golfPOIData.findAllPOIs()
            i("allCoursesLength: ${allGolfCourse.size}")
            var searchResults = ArrayList(allGolfCourse.filter {
                    it.courseTitle.lowercase().contains(query.lowercase()) ||
                    it.courseDescription.lowercase().contains(query.lowercase()) ||
                    it.courseProvince.lowercase().contains(query.lowercase()) ||
                    it.coursePar.toString().contains(query.lowercase())
            })
            return searchResults
        } else {
            return app.golfPOIData.findAllPOIs()
        }
    }

    // Load Golf course which were created by the current user
    private fun loadGolfPOIs(id: Long, favourites: Boolean) : List<GolfPOIModel> {
        if (favourites) {
            var favouriteCourses = ArrayList(app.golfPOIData.findUsersFavouriteCourses(id))
            return favouriteCourses
        } else {
            var userFilteredCourses = ArrayList(app.golfPOIData.findByCreatedByUserId(id))
            return userFilteredCourses
        }
    }


}