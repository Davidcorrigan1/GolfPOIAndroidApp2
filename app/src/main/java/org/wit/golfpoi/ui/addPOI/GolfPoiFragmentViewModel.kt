package org.wit.golfpoi.ui.addPOI

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.wit.golfpoi.firebase.FirebaseDBManager
import org.wit.golfpoi.models.GolfPOIModel2


class GolfPoiFragmentViewModel  (application: Application) : AndroidViewModel(application) {

    fun createPOI(golfPOI: GolfPOIModel2) {
        FirebaseDBManager.createPOI(golfPOI)
    }

    fun updatePOI (golfPOI: GolfPOIModel2) {
        FirebaseDBManager.updatePOI(golfPOI)
    }

}