package org.wit.golfpoi.ui.addPOI

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.wit.golfpoi.firebase.FirebaseDBManager
import org.wit.golfpoi.models.GolfPOIModel2


class GolfPoiFragmentViewModel  (application: Application) : AndroidViewModel(application) {

    var firebaseDBManager: FirebaseDBManager = FirebaseDBManager(application)

    fun createPOI(golfPOI: GolfPOIModel2) {
        firebaseDBManager.createPOI(golfPOI)
    }

    fun updatePOI (golfPOI: GolfPOIModel2) {
        firebaseDBManager.updatePOI(golfPOI)
    }

}