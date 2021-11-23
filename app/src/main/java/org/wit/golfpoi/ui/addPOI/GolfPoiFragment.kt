package org.wit.golfpoi.ui.addPOI

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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import org.wit.golfpoi.R
import org.wit.golfpoi.databinding.FragmentGolfPoiBinding
import org.wit.golfpoi.helpers.showImagePicker
import org.wit.golfpoi.main.MainApp
import org.wit.golfpoi.models.GolfPOIModel
import org.wit.golfpoi.models.Location
import timber.log.Timber.i



class GolfPoiFragment : Fragment() {
    var golfPOI: GolfPOIModel = GolfPOIModel()
    lateinit var app: MainApp
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private var _fragBinding: FragmentGolfPoiBinding? = null
    private val fragBinding get() = _fragBinding!!
    var location = Location("Current", 52.245696, -7.139102, 15f)
    var setProvinces : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        if ((golfPOI.courseTitle != "" ) ||
            (golfPOI.courseDescription != "") ||
            (golfPOI.coursePar != 0 ) ||
            (golfPOI.lng.equals(0.0) || golfPOI.lat.equals(0.0)) ||
            (golfPOI.image != Uri.EMPTY)){
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
            i("Setting the dropdown default from model value")
        }
        setSpinnerListener(spinner, provinces)
        registerImagePickerCallback(fragBinding)
        setButtonListener(fragBinding)

        return root
    }

    override fun onResume() {
        super.onResume()

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
        } else {
            return NavigationUI.onNavDestinationSelected(
                item,
                requireView().findNavController()
            ) || super.onOptionsItemSelected(item)
        }
    }

    // Set the listener buttons for choosing image and creating/updating the POI
    private fun setButtonListener (layout: FragmentGolfPoiBinding) {
        // Listener for the Add Image button
        layout.btnChooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }


        // Set the listener for the button to select the location
        layout.btnGolfPOILocation.setOnClickListener {
            i ("Set Location Pressed")
            if (golfPOI.lat == 0.0 && golfPOI.lng == 0.0) {
                golfPOI.lat = location.lat
                golfPOI.lng = location.lng
                golfPOI.zoom = location.zoom
            }
            // make sure updates to the screen are captured
            golfPOI.courseTitle = fragBinding.golfPOITitle.text.toString()
            golfPOI.courseDescription = fragBinding.golfPOIDesc.text.toString()
            golfPOI.courseProvince = setProvinces
            golfPOI.coursePar = fragBinding.golfPOIparPicker.value
            val action = GolfPoiFragmentDirections.actionGolfPoiFragmentToGolfPoiSelectMapFragment(golfPOI)
            findNavController().navigate(action)
        }

    }

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
}