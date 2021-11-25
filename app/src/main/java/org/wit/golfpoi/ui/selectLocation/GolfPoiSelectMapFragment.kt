package org.wit.golfpoi.ui.selectLocation


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.golfpoi.databinding.FragmentGolfPoiSelectMapBinding
import org.wit.golfpoi.models.GolfPOIModel
import org.wit.golfpoi.models.Location
import timber.log.Timber


class GolfPoiSelectMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {
    private lateinit var map: GoogleMap
    private var _fragBinding: FragmentGolfPoiSelectMapBinding? = null
    private val fragBinding get() = _fragBinding!!
    var defaultLocation = Location("Current", 52.245696, -7.139102, 15f)
    var golfPOI = GolfPOIModel()
    var location = Location()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _fragBinding = FragmentGolfPoiSelectMapBinding.inflate(inflater, container, false)
        val root = fragBinding?.root

        //val golfPOI = arguments as GolfPOIModel
        val golfPOIBundle = arguments
        golfPOI = golfPOIBundle?.getParcelable("golfPOI")!!
        location.lng = golfPOI.lng
        location.lat = golfPOI.lat
        if (golfPOI.zoom == 0f) {
            golfPOI.zoom = 15f
        }
        location.zoom = golfPOI.zoom
        location.name = golfPOI.courseTitle
        Timber.i("The bundle2: ${golfPOI}")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = FragmentManager.findFragment(fragBinding.root) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GolfPoiSelectMapFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val loc = LatLng(location.lat, location.lng)
        val options = MarkerOptions()
            .title(location.name)
            .snippet("GPS : $loc")
            .draggable(true)
            .position(loc)

        map.addMarker(options)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, location.zoom))
        map.setOnMarkerDragListener(this)
        map.setOnMarkerClickListener(this)
    }

    override fun onMarkerDragStart(marker: Marker) {
        val loc = LatLng(location.lat, location.lng)
        marker.snippet = "GPS: : $loc"
    }

    override fun onMarkerDrag(marker: Marker) {


    }

    override fun onMarkerDragEnd(marker: Marker) {
        location.lat = marker.position.latitude
        location.lng = marker.position.longitude
        location.zoom = map.cameraPosition.zoom
        golfPOI.lat = location.lat
        golfPOI.lng = location.lng
        golfPOI.zoom = location.zoom
        val loc = LatLng(location.lat, location.lng)
        marker.snippet = "GPS: : $loc"
        Timber.i("Moved marker: ${location.lat} ")
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val loc = LatLng(location.lat, location.lng)
        marker.snippet = "GPS: : $loc"
        return false
    }

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    override fun onStop() {
        super.onStop()
        backPressedCallback.remove()
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val action = GolfPoiSelectMapFragmentDirections.actionGolfPoiSelectMapFragmentToGolfPoiFragment(golfPOI)
            findNavController().navigate(action)
        }

    }


}