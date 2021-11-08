package org.wit.golfpoi.fragments

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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.golfpoi.databinding.FragmentGolfPoisOverviewMapBinding
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfPOIModel


class GolfPoisOverviewMapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var golfPOIs: ArrayList<GolfPOIModel>
    lateinit var app: MainApp
    private var _fragBinding: FragmentGolfPoisOverviewMapBinding? = null
    private val fragBinding get() = _fragBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp
        golfPOIs = app.golfPOIData.findAllPOIs() as ArrayList<GolfPOIModel>

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentGolfPoisOverviewMapBinding.inflate(inflater, container, false)
        val root = fragBinding?.root

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = FragmentManager.findFragment(fragBinding.root) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GolfPoisOverviewMapFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    /**
     * This is where we can add markers for all our courses
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // create a BoundsBuilder object to help frame the map
        // For each golfCourse in the Arraylist from last Activity
        // if the location is populated create Latlng object
        // Include each map location in the boundsBuilder
        // Create a marker for each golf course with title and desc included.
        val boundsBuilder = LatLngBounds.builder()
        for (golfPOI in golfPOIs) {
            if (golfPOI.lat != 0.0 && golfPOI.lng != 0.0) {
                val latlng = LatLng(golfPOI.lat, golfPOI.lng)
                boundsBuilder.include(latlng)
                map.addMarker(
                    MarkerOptions().position(latlng).title(golfPOI.courseTitle)
                        .snippet(golfPOI.courseDescription)
                )

            }
        }
        // Move the'camera' for that it zooms to show all the Golf Courses
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(),1000,1000,0))
    }

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    override fun onStop() {
        super.onStop()
        backPressedCallback.remove()
    }

    // Back button callback
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val action = GolfPoisOverviewMapFragmentDirections.actionGolfPoisOverviewMapFragmentToGolfPoiListFragment()
            findNavController().navigate(action)
        }

    }
}