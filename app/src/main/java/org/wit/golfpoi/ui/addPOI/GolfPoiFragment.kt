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
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfPOIModel
import org.wit.golfpoi.models.Location
import org.wit.golfpoi.ui.auth.LoggedInViewModel
import org.wit.golfpoi.ui.auth.LoginViewModel
import timber.log.Timber.i



class GolfPoiFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {
    var golfPOI: GolfPOIModel = GolfPOIModel()
    lateinit var app: MainApp
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var _fragBinding: FragmentGolfPoiBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val loginViewModel : LoginViewModel by activityViewModels()
    var defaultLocation = Location("Current", 52.245696, -7.139102, 13f)
    var setProvinces : String = ""
    lateinit var map: GoogleMap
    lateinit var locationService: FusedLocationProviderClient
    val locationRequest = createDefaultLocationRequest()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp

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

        //val golfPOI = arguments as GolfPOIModel
        val golfPOIBundle = arguments
        i("golfPOIBundle: $golfPOIBundle")
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
        if ((golfPOI.courseTitle != "" ) || (golfPOI.courseDescription != "")) {
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
            i("Firebase GolfPoi Log out")
            loggedInViewModel.logOut()
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
            i("Firebase authStateLister Called from PoiAdd and not logged on")
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
                i("Selected: ${getString(R.string.selected_item)} ${provinces[p2]}" )
                setProvinces = provinces[p2]
                i("The selected provence is: $setProvinces")
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
                            i("result.data.data: ${result.data!!.data!!}")
                            i("golfPOI.image: ${golfPOI.image}")
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
                    i("Current Location setting 246")
                    doSetCurrentLocation()
                } else {
                    locationUpdate(defaultLocation.lat, defaultLocation.lng)
                }
            }
    }

    private fun setOnMapClickListener(map: GoogleMap) {
        map.setOnMapClickListener(GoogleMap.OnMapClickListener {
            i("Set Map Clicked")

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
                i("Current Location setting 296")
                doSetCurrentLocation()
            } else {
                golfPOI.lat = defaultLocation.lat
                golfPOI.lng = defaultLocation.lng
                locationUpdate(golfPOI.lat, golfPOI.lng)
            }
        } else {
            locationUpdate(golfPOI.lat, golfPOI.lng)
        }
    }

    @SuppressLint("MissingPermission")
    fun doSetCurrentLocation() {
        i("Current Location -setting location from doSetLocation")
        locationService.lastLocation.addOnSuccessListener {
            i("Current Location lat lng in doSetCurrentListener")
            if(it == null) {
                i("Current Location - last known location not found")
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

        i("Setting the model province to $setProvinces")

        if (golfPOI.courseTitle.isNotEmpty() && golfPOI.courseDescription.isNotEmpty()) {
            if (app.golfPOIData.findPOI(golfPOI.id) != null) {
                i("save Button Pressed ${golfPOI.courseTitle} and ${golfPOI.courseDescription}")
                i("Course being saved: $golfPOI")
                app.golfPOIData.updatePOI(golfPOI.copy())
            } else {
                i("add Button Pressed ${golfPOI.courseTitle} and ${golfPOI.courseDescription}")
                app.golfPOIData.createPOI(golfPOI.copy())
            }
            val navController = view?.findNavController()
            navController?.navigate(R.id.action_golfPoiFragment_to_golfPoiListFragment)

        } else {
            view?.let {
                Snackbar
                    .make(it, R.string.prompt_addGolfPOI, Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

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