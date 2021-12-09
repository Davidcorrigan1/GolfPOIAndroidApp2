package org.wit.golfpoi.ui.addPOI

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import org.wit.golfpoi.R
import org.wit.golfpoi.databinding.FragmentGolfPoiBinding
import org.wit.golfpoi.helpers.checkLocationPermissions
import org.wit.golfpoi.helpers.createDefaultLocationRequest
import org.wit.golfpoi.helpers.showImagePicker
import org.wit.golfpoi.models.GolfPOIModel2
import org.wit.golfpoi.models.Location
import org.wit.golfpoi.ui.auth.LoginViewModel
import org.wit.golfpoi.ui.listPOI.GolfPoiListViewModel
import org.wit.golfpoi.ui.register.RegisterViewModel
import timber.log.Timber.i



class GolfPoiFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {
    var golfPOI: GolfPOIModel2 = GolfPOIModel2()
    private lateinit var golfPoiFragmentViewModel: GolfPoiFragmentViewModel
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var _fragBinding: FragmentGolfPoiBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val loginViewModel : LoginViewModel by activityViewModels()
    private val golfPoiListViewModel : GolfPoiListViewModel by activityViewModels()
    var defaultLocation = Location("Current", 52.245696, -7.139102, 13f)
    var setProvinces : String = ""
    lateinit var map: GoogleMap
    lateinit var locationService: FusedLocationProviderClient
    val locationRequest = createDefaultLocationRequest()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Acquire reference to the location provider client
        locationService = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Inflate the layout for this fragment
        _fragBinding = FragmentGolfPoiBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        golfPoiFragmentViewModel = ViewModelProvider(activity as AppCompatActivity).get(GolfPoiFragmentViewModel::class.java)


        //val golfPOI = arguments as GolfPOIModel
        val golfPOIBundle = arguments
        if (golfPOIBundle?.get("golfPOI") != null) {
            golfPOI = golfPOIBundle.getParcelable("golfPOI")!!
        }

        // creating objects needed for the spinner drop down
        // Dropdown of Provinces taken from the strings resource file
        val provinces = resources.getStringArray(R.array.provinces)
        val spinner : Spinner = fragBinding.provinceSpinner

        // Create an ArrayAdapter object with the dropdown type
        // and populate using the list of the provinces.
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_dropdown_item_1line, provinces)
        spinner.adapter = adapter

        // Set the Range for the Course Par picker
        fragBinding.golfPOIparPicker.minValue = 70
        fragBinding.golfPOIparPicker.maxValue = 72

        // If the golfPOI has been passed in with values
        if ((golfPOI.courseTitle != "" )) {
            setScreenFromPassData(adapter, spinner)
        }

        permissionLauncherCallback()
        setSpinnerListener(spinner, provinces)
        registerImagePickerCallback(fragBinding)
        setImageButtonListener(fragBinding)
        loginViewModel.addFirebaseStateListener(authStateListener)

        fragBinding.mapViewSmall.onCreate(savedInstanceState)
        fragBinding.mapViewSmall.getMapAsync(this)

        return root
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        fragBinding.mapViewSmall.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        fragBinding.mapViewSmall.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        fragBinding.mapViewSmall.onPause()
    }

    override fun onResume() {
        super.onResume()
        fragBinding.mapViewSmall.onResume()
        doRestartLocationUpdates()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GolfPoiFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    // Override method to load the menu resource
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_golfpoi, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Implements a menu event handler;
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.golfPoiSave) {
            saveGolfCourseData(fragBinding)
            return false
        } else if (item.itemId == R.id.golfLoginFragment) {
            loginViewModel.logOut()
            return false
        } else if (item.itemId == R.id.golfPoiFavourite) {
            if (golfPOI.courseTitle.isNotEmpty()) {
                var updatedUser = loginViewModel.currentUserCollectionData.value
                updatedUser?.favorites?.add(golfPOI.uid)
                if (updatedUser != null) {
                    golfPoiListViewModel.updateUser(updatedUser)
                }
            }
            return false
        } else {
            return NavigationUI.onNavDestinationSelected(
                item,
                requireView().findNavController()) || super.onOptionsItemSelected(item)
        }
    }

    // Set the listener buttons for choosing image and creating/updating the POI
    private fun setImageButtonListener (layout: FragmentGolfPoiBinding) {
        // Listener for the Add Image button
        layout.btnChooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }
    }

    // defining listener callback to check user authorisation
    val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            view?.post { findNavController().navigate(R.id.action_golfPoiFragment_to_golfLoginFragment)}
        }
    }

    // Configuring the map when it ready to display
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        configureMap(map)
        setOnMapClickListener(map)
    }

    private fun setSpinnerListener (spinner: Spinner, provinces: Array<String>) {
        // Listener for the spinner dropdown for provinces
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                setProvinces = provinces[p2]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                i("nothing selected")
            }
        }
    }

    // Register a callback along with the contract that defines its input (and output) types
    private fun registerImagePickerCallback(layout: FragmentGolfPoiBinding) {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            golfPOI.image = result.data!!.data!!
                            Picasso.get()
                                .load(golfPOI.image)
                                .into(layout.golfPOIImage)
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    // Callback for permission requester
    private fun permissionLauncherCallback() {
        i("permission check called")
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) {
                    doSetCurrentLocation()
                } else {
                    locationUpdate(defaultLocation.lat, defaultLocation.lng)
                }
            }
    }

    private fun setOnMapClickListener(map: GoogleMap) {
        map.setOnMapClickListener(GoogleMap.OnMapClickListener {

            if (golfPOI.lat == 0.0 && golfPOI.lng == 0.0) {
                golfPOI.lat = defaultLocation.lat
                golfPOI.lng = defaultLocation.lng
                golfPOI.zoom = defaultLocation.zoom
            }
            // make sure updates to the screen are captured
            golfPOI.courseTitle = fragBinding.golfPOITitle.text.toString()
            golfPOI.courseDescription = fragBinding.golfPOIDesc.text.toString()
            golfPOI.courseProvince = setProvinces
            golfPOI.coursePar = fragBinding.golfPOIparPicker.value

            val action =
                GolfPoiFragmentDirections.actionGolfPoiFragmentToGolfPoiSelectMapFragment(golfPOI)
            findNavController().navigate(action)
        })
    }


    override fun onMarkerDragStart(marker: Marker) {
        val loc = LatLng(golfPOI.lat, golfPOI.lng)
        marker.snippet = "GPS: : $loc"
    }

    override fun onMarkerDrag(marker: Marker) {


    }

    override fun onMarkerDragEnd(marker: Marker) {
        golfPOI.lat = marker.position.latitude
        golfPOI.lng = marker.position.longitude
        golfPOI.zoom = map.cameraPosition.zoom
        val loc = LatLng(golfPOI.lat, golfPOI.lng)
        marker.snippet = "GPS: : $loc"
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val loc = LatLng(golfPOI.lat, golfPOI.lng)
        marker.snippet = "Title: : ${golfPOI.courseTitle}"
        return false
    }

    fun configureMap(googleMap: GoogleMap) {
        map = googleMap

        if (golfPOI.lat == 0.00 && golfPOI.lng == 0.00) {
            if (checkLocationPermissions(requireActivity())) {
                i("getting current location")
                doSetCurrentLocation()
            } else {
                i("using default location")
                golfPOI.lat = defaultLocation.lat
                golfPOI.lng = defaultLocation.lng
                locationUpdate(golfPOI.lat, golfPOI.lng)
            }
        } else {
            i("getting golfPOI location")
            locationUpdate(golfPOI.lat, golfPOI.lng)
        }
    }

    @SuppressLint("MissingPermission")
    fun doSetCurrentLocation() {
        //Current Location -setting location from doSetLocation")
        locationService.lastLocation.addOnSuccessListener {
            //Current Location lat lng in doSetCurrentListener")
            if(it == null) {
                //Current Location - last known location not found")
                locationUpdate(defaultLocation.lat, defaultLocation.lng)
            } else {
                locationUpdate(it.latitude, it.longitude)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun doRestartLocationUpdates() {
        var locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult != null && locationResult.locations != null) {
                    val l = locationResult.locations.last()
                    locationUpdate(l.latitude, l.longitude)
                }
            }
        }
        if (golfPOI.lat == 0.0 && golfPOI.lng == 0.0) {
            locationService.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    // Update the Marker location on the map and move focus
    fun locationUpdate(lat: Double, lng: Double) {
        golfPOI.lat = lat
        golfPOI.lng = lng
        val loc = LatLng(golfPOI.lat, golfPOI.lng)
        val options = MarkerOptions()
            .title(golfPOI.courseTitle)
            .snippet("GPS : $loc")
            .draggable(true)
            .position(loc)

        map.addMarker(options)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, defaultLocation.zoom))
        map.setOnMarkerDragListener(this)
        map.setOnMarkerClickListener(this)
    }

    // Save the screen data to the golfPOI object
    private fun saveGolfCourseData (layout: FragmentGolfPoiBinding) {
        golfPOI.courseTitle = layout.golfPOITitle.text.toString()
        golfPOI.courseDescription = layout.golfPOIDesc.text.toString()
        golfPOI.courseProvince = setProvinces
        golfPOI.coursePar = layout.golfPOIparPicker.value
        if (golfPOI.createdById == "") {
            golfPOI.createdById = loginViewModel.liveFirebaseUser.value?.uid.toString()
        }


        if (golfPOI.courseTitle.isNotEmpty() && golfPOI.courseDescription.isNotEmpty()) {
            golfPoiListViewModel.golfPOIs.observe(viewLifecycleOwner, { golfPOIs ->
                golfPOIs?.let {
                    if (golfPOIs.find { p -> p.uid == golfPOI.uid } != null) {
                        golfPoiFragmentViewModel.updatePOI(golfPOI.copy())
                    } else {
                        golfPoiFragmentViewModel.createPOI(golfPOI.copy())
                    }
                    val navController = view?.findNavController()
                    navController?.navigate(R.id.action_golfPoiFragment_to_golfPoiListFragment)
                }
            })
        } else {
            view?.let {
                Snackbar
                    .make(it, R.string.prompt_addGolfPOI, Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    // Populate the Add POI screen from the Course data
    fun setScreenFromPassData(adapter: ArrayAdapter<String>, spinner: Spinner) {
        i("golfPOI.courseTitle:  ${golfPOI.courseTitle}")
        fragBinding.golfPOITitle.setText(golfPOI.courseTitle)
        fragBinding.golfPOIDesc.setText(golfPOI.courseDescription)

        fragBinding.golfPOIparPicker.value = golfPOI.coursePar
        Picasso.get().load(golfPOI.image).into(fragBinding.golfPOIImage)

        // If coming from the list of courses and Course image already set, change button text
        if (golfPOI.image != Uri.EMPTY) {
            fragBinding.btnChooseImage.setText(R.string.change_golfPOI_image)
        }

        // check the current selected provence and default to that one!
        val spinnerPosition : Int = adapter.getPosition(golfPOI.courseProvince)
        spinner.setSelection(spinnerPosition)
    }

}