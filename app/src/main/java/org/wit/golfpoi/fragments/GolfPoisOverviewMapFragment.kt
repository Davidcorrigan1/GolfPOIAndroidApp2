package org.wit.golfpoi.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import org.wit.golfpoi.R
import org.wit.golfpoi.adapter.GolfPOIListener
import org.wit.golfpoi.databinding.CardMapGolfpoiBinding
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
    private lateinit var selectedGolfPOI : GolfPOIModel

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

        contentBinding = CardMapGolfpoiBinding.bind(fragBinding.root)

        contentBinding.cardView.setOnClickListener{
                val action =
                    GolfPoisOverviewMapFragmentDirections.actionGolfPoisOverviewMapFragmentToGolfPoiFragment(selectedGolfPOI)
                findNavController().navigate(action)
        }

        contentBinding.mapView.onCreate(savedInstanceState)
        contentBinding.mapView.getMapAsync{
            map = it
            configureMap()
        }

        return root
    }

    private fun configureMap() {
        map.setOnMarkerClickListener(this)
        map.uiSettings.isZoomControlsEnabled = true

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
                ).tag = golfPOI.id

            }
        }
        // Move the'camera' for that it zooms to show all the Golf Courses
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(),1000,1000,0))
        populateMarkerCard (golfPOIs.last().id)

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
            val action = GolfPoisOverviewMapFragmentDirections.actionGolfPoisOverviewMapFragmentToGolfPoiListFragment()
            findNavController().navigate(action)
        }

    }

    override fun onMarkerClick(marker: Marker): Boolean {
        i("marker id: ${marker.tag}")
        populateMarkerCard (marker.tag)
        return false
    }

    private fun populateMarkerCard (id: Any) {
        var golfPOIMarker = golfPOIs.find { golfPOI -> golfPOI.id == id }
        if (golfPOIMarker != null) {
            contentBinding.golfPOITitle.text = golfPOIMarker.courseTitle
            contentBinding.golfPOIDesc.text = golfPOIMarker.courseDescription
            contentBinding.golfPOIProvince.text = golfPOIMarker.courseProvince

            contentBinding.golfPOIPar.text = "  Par: ${golfPOIMarker.coursePar}"
            // Show default image if none available
            if (golfPOIMarker.image != null) {
                if (golfPOIMarker.image.equals(Uri.EMPTY)) {
                    contentBinding.imageIcon.setImageResource(R.drawable.golflogo)
                } else {
                    Timber.i("golfPOI Image: ${golfPOIMarker.image}")
                    //Picasso.get().load(golfPOI.image).centerCrop().fit().into(binding.imageIcon)
                    Picasso.get().load(golfPOIMarker.image).resize(200,200).into(contentBinding.imageIcon)
                }
            }
            selectedGolfPOI = golfPOIMarker
        }
    }


}