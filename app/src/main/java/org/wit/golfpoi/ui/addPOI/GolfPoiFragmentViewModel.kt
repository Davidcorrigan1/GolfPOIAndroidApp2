package org.wit.golfpoi.ui.addPOI

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.GoogleMap
import org.wit.golfpoi.firebase.FirebaseDBManager
import org.wit.golfpoi.models.GolfPOIModel2
import org.wit.golfpoi.models.GolfUserModel2

class GolfPoiFragmentViewModel  (application: Application) : AndroidViewModel(application) {

    fun createPOI(golfPOI: GolfPOIModel2) {
        FirebaseDBManager.createPOI(golfPOI)
    }

    fun updateUser(user: GolfUserModel2) {
        FirebaseDBManager.updateUser(user)
    }

    fun updatePOI (golfPOI: GolfPOIModel2) {
        FirebaseDBManager.updatePOI(golfPOI)
    }

}