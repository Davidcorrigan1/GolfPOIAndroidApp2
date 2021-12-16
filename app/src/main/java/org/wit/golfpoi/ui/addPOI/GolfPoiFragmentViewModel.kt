package org.wit.golfpoi.ui.addPOI

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.wit.golfpoi.firebase.FirebaseDBManager
import org.wit.golfpoi.models.GolfPOIModel


class GolfPoiFragmentViewModel  (application: Application) : AndroidViewModel(application) {

    var firebaseDBManager: FirebaseDBManager = FirebaseDBManager(application)

    fun createPOI(golfPOI: GolfPOIModel) {
        firebaseDBManager.createPOI(golfPOI)
    }

    fun updatePOI (golfPOI: GolfPOIModel) {
        firebaseDBManager.updatePOI(golfPOI)
    }

}