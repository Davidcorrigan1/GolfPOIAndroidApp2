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
import org.wit.golfpoi.databinding.FragmentGolfPoisOverviewMapBinding
import org.wit.golfpoi.models.GolfPOIModel2
import timber.log.Timber.i
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import androidx.fragment.app.activityViewModels
import org.wit.golfpoi.models.GolfUserModel2
import org.wit.golfpoi.ui.auth.LoginViewModel
import org.wit.golfpoi.ui.listPOI.GolfPoiListViewModel


class GolfPoisOverviewMapFragment : Fragment(), GoogleMap.OnMarkerClickListener {
    private lateinit var map: GoogleMap
    private lateinit var golfPOIs: ArrayList<GolfPOIModel2>
    private lateinit var userFavouritesPOIs: ArrayList<GolfPOIModel2>
    private lateinit var userCreatedPOIs: ArrayList<GolfPOIModel2>
    private lateinit var currentUserCollection: GolfUserModel2
    private var _fragBinding: FragmentGolfPoisOverviewMapBinding? = null
    private lateinit var contentBinding: CardMapGolfpoiBinding
    private val fragBinding get() = _fragBinding!!
    private lateinit var selectedGolfPOI: GolfPOIModel2
    private var searchView: SearchView? = null
    private var showFavourites = false
    private var showUserCreated = false
    private var currentMarkerFavorite = false
    private var nextMarkerFavorite = false
    private val loginViewModel : LoginViewModel by activityViewModels()
    private val golfPoiListViewModel : GolfPoiListViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentGolfPoisOverviewMapBinding.inflate(inflater, container, false)
        val root = fragBinding?.root

        // Obverse the current full list of courses
        golfPoiListViewModel.golfPOIs.observe(viewLifecycleOwner, { pois: List<GolfPOIModel2> ->
            pois?.let {
                golfPOIs = pois as ArrayList<GolfPOIModel2>
            }
        })

        // Observe the current users favourites
        golfPoiListViewModel.currentUsersFavoritePOIs.observe(viewLifecycleOwner, {currentUsersFavoritePOIs ->
            userFavouritesPOIs = currentUsersFavoritePOIs as ArrayList<GolfPOIModel2>
        })

        // Observe the current user collection data
        loginViewModel.currentUserCollectionData.observe(viewLifecycleOwner, {currentUserCollectionData ->
            currentUserCollection = currentUserCollectionData as GolfUserModel2
        })

        //Observe the current user created POIs
        golfPoiListViewModel.currentUsersPOIs.observe(viewLifecycleOwner, {currentUsersPOIs ->
            userCreatedPOIs = currentUsersPOIs as ArrayList<GolfPOIModel2>
        })


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
        showUserCreated = false
        showFavourites = false
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
                showFavourites = true
                showUserCreated = false
                val displayCourses = loadGolfPOIs(favourites = true)
                if (displayCourses.size.equals(0)) {
                    showFavourites = false
                    Toast.makeText(context, "No Favourites to display, showing all courses", Toast.LENGTH_LONG).show()
                    configureMap(loadGolfPOIs())
                } else {
                    configureMap(displayCourses)
                }
            } else {
                showFavourites = false
                showUserCreated = false
                configureMap(loadGolfPOIs())
            }
        }
    }

    // Callback for the favourites button in the Card View on Overview Map
    private fun onClickFavButtionCallback(contentBinding: CardMapGolfpoiBinding) {
        contentBinding.favoriteBtn.setOnClickListener {
            if (currentUserCollection != null) {
                if (currentUserCollection.favorites.contains(selectedGolfPOI.uid)) {
                    currentUserCollection.favorites.remove(selectedGolfPOI.uid)
                } else {
                    currentUserCollection.favorites.add(selectedGolfPOI.uid)
                }
                golfPoiListViewModel.updateUser(currentUserCollection)
            }
            nextMarkerFavorite = !currentMarkerFavorite

            populateMarkerCard(selectedGolfPOI.uid, favChange = true)

        }
    }

    // Callback for click of a Marker
    override fun onMarkerClick(marker: Marker): Boolean {
        i("marker id: ${marker.tag}")
        populateMarkerCard (marker.tag!!, favChange = false)
        return false
    }

    // Populate the Card for marker clicked
    private fun populateMarkerCard (uid: Any, favChange: Boolean) {
        val golfPOIMarker = golfPOIs.find { golfPOI -> golfPOI.uid == uid }
        if (golfPOIMarker != null) {
            contentBinding.golfPOITitle.text = golfPOIMarker.courseTitle
            contentBinding.golfPOIDesc.text = golfPOIMarker.courseDescription
            contentBinding.golfPOIProvince.text = golfPOIMarker.courseProvince

            contentBinding.golfPOIPar.text = "  Par: ${golfPOIMarker.coursePar}"

            if (favChange) {
                if (nextMarkerFavorite) {
                    currentMarkerFavorite = true
                    contentBinding.favoriteBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
                } else {
                    currentMarkerFavorite = false
                    contentBinding.favoriteBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }
            } else {
                // Set the favourites icon if the course is a favourite
                if (userFavouritesPOIs.find { p -> p.uid == golfPOIMarker.uid } != null) {
                    currentMarkerFavorite = true
                    contentBinding.favoriteBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
                } else {
                    currentMarkerFavorite = false
                    contentBinding.favoriteBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }
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
                showUserCreated = true
                showFavourites = false
                val displayCourses = loadGolfPOIs(favourites = false)
                if (displayCourses.size.equals(0)) {
                    showUserCreated = false
                    Toast.makeText(context, "No User courses to display, showing all courses", Toast.LENGTH_SHORT).show()
                    configureMap(loadGolfPOIs())
                } else {
                    configureMap(displayCourses)
                }
            } else {
                showUserCreated = false
                showFavourites = false
                configureMap(loadGolfPOIs())
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Load Golf courses function
    private fun loadGolfPOIs(): List<GolfPOIModel2> {
        return golfPOIs
    }


    // Load Golf courses which match the query string entered
    private fun loadGolfPOIs(query: String) : List<GolfPOIModel2> {
        if (query != "") {
            var allGolfCourse = golfPOIs
            i("allCoursesLength: ${allGolfCourse.size}")
            var searchResults = ArrayList(allGolfCourse.filter {
                    it.courseTitle.lowercase().contains(query.lowercase()) ||
                    it.courseDescription.lowercase().contains(query.lowercase()) ||
                    it.courseProvince.lowercase().contains(query.lowercase()) ||
                    it.coursePar.toString().contains(query.lowercase())
            })
            return searchResults
        } else {
            return golfPOIs
        }
    }

    // Load Golf course which were created by the current user
    private fun loadGolfPOIs(favourites: Boolean) : List<GolfPOIModel2> {
        if (favourites) {
            return userFavouritesPOIs
        } else {
            return userCreatedPOIs
        }
    }

    // This configures the overview map based on the List of courses passed in to display
    private fun configureMap(mapGolfPOIs: List<GolfPOIModel2>) {
        map.clear()
        map.setOnMarkerClickListener(this)
        map.uiSettings.isZoomControlsEnabled = true

        // create a BoundsBuilder object to help frame the map
        // For each golfCourse in the Arraylist from last Activity
        // if the location is populated create Latlng object
        // Include each map location in the boundsBuilder
        // Create a marker for each golf course with title and desc included.
        val boundsBuilder = LatLngBounds.builder()
        if (mapGolfPOIs.isNotEmpty()) {
            for (mapGolfPOI in mapGolfPOIs) {
                if (mapGolfPOI.lat != 0.0 && mapGolfPOI.lng != 0.0) {
                    val latlng = LatLng(mapGolfPOI.lat, mapGolfPOI.lng)
                    boundsBuilder.include(latlng)
                    if (showFavourites) {
                        map.addMarker(
                            MarkerOptions().position(latlng).title(mapGolfPOI.courseTitle).icon(
                                context?.let { bitmapDescriptorFromVector(it,R.drawable.ic_baseline_favorite_red_48) })
                        )?.tag = mapGolfPOI.uid
                    } else if (showUserCreated) {
                        map.addMarker(
                            MarkerOptions().position(latlng).title(mapGolfPOI.courseTitle).icon(
                                context?.let { bitmapDescriptorFromVector(it,R.drawable.ic_baseline_person_pin_circle_48) })
                        )?.tag = mapGolfPOI.uid
                    } else
                        map.addMarker(
                            MarkerOptions().position(latlng).title(mapGolfPOI.courseTitle)
                        )?.tag = mapGolfPOI.uid
                }
            }
            // Move the'camera' for that it zooms to show all the Golf Courses
            if (mapGolfPOIs.size > 1) {
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
                val latlng = LatLng(mapGolfPOIs[0].lat, mapGolfPOIs[0].lng)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12f))
            }
            populateMarkerCard(mapGolfPOIs.last().uid, favChange = false)
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

    // creating bitmap descriptor see: https://stackoverflow.com/questions/42365658/custom-marker-in-google-maps-in-android-with-vector-asset-icon
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}