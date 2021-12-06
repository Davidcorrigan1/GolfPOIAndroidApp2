package org.wit.golfpoi.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.wit.golfpoi.models.GolfPOIModel
import org.wit.golfpoi.models.GolfPOIStoreInterface
import org.wit.golfpoi.models.GolfUserModel
import org.wit.golfpoi.models.GolfUserModel2
import timber.log.Timber

object FirebaseDBManager : GolfPOIStoreInterface{

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun findAllPOIs(golfPOIs: MutableLiveData<List<GolfPOIModel>>) {
        TODO("Not yet implemented")
    }

    override fun createPOI(firebaseUser: MutableLiveData<FirebaseUser>, golfPOI: GolfPOIModel) {
        TODO("Not yet implemented")
    }

    override fun updatePOI(golfPOI: GolfPOIModel) {
        TODO("Not yet implemented")
    }

    override fun removePOI(position: Int) {
        TODO("Not yet implemented")
    }

    override fun findPOI(id: Long, golfPOI: MutableLiveData<GolfPOIModel>) {
        TODO("Not yet implemented")
    }

    override fun findByCreatedByUserId(id: Long, golfPOIs: MutableLiveData<List<GolfPOIModel>>) {
        TODO("Not yet implemented")
    }

    override fun findUsersFavouriteCourses(
        id: Long,
        golfPOIs: MutableLiveData<List<GolfPOIModel>>
    ) {
        TODO("Not yet implemented")
    }

    // Creates a new user on the Realtime DB for user with uid of the firebase user
    override fun createUser(uid: String, user: GolfUserModel2) {
        user.uid = uid
        user.favorites.add(uid)
        val userValues = user.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/users/$uid"] = userValues

        Timber.i("FirebaseDB Register4 : Updating DB: $uid and $user")
        database.updateChildren(childAdd)
    }

    override fun findUser(email: String, user: MutableLiveData<GolfUserModel2>) {
        TODO("Not yet implemented")
    }

    override fun updateUser(user: GolfUserModel2) {
        TODO("Not yet implemented")
    }

    override fun setCurrentUser(user: GolfUserModel2) {
        TODO("Not yet implemented")
    }

    override fun getCurrentUser(): GolfUserModel2 {
        TODO("Not yet implemented")
    }


}