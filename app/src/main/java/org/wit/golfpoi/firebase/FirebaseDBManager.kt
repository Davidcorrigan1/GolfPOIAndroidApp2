package org.wit.golfpoi.firebase

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.wit.golfpoi.models.*
import timber.log.Timber.i

object FirebaseDBManager : GolfPOIStoreInterface {

    private val database = Firebase.firestore


    // Find all Course POIs and update the passed in MutableLiveData List passed in.
    override fun findAllPOIs(golfPOIs: MutableLiveData<List<GolfPOIModel2>>) {

        database.collection("golfPOIs")
            .get()
            .addOnSuccessListener { documents ->
                var localGolfPOIs = mutableListOf<GolfPOIModel2>()
                var localGolfPOI = GolfPOIModel2()
                for (document in documents) {
                    i("FirebaseDB POI document: ${document.data} ")
                    i("FirebaseDB POI document uid: ${document.data.getValue("uid").toString()} ")
                    if (document != null) {
                        storeResultInObject(localGolfPOI, document)
                        i("FirebaseDB POI documents 123: $localGolfPOI ")
                        localGolfPOIs.add(localGolfPOI.copy())
                    }
                }
                i("FirebaseDB FindAllPOIs: $localGolfPOIs")
                golfPOIs.value = localGolfPOIs

            }
            .addOnFailureListener { exception ->
                i("Firebase Error retrieveing POI data : $exception")
            }
    }

    // Find all Course POIs and update the passed in MutableLiveData List passed in.
    override fun setOnChangeListenerPOIs(golfPOIs: MutableLiveData<List<GolfPOIModel2>>) {

        database.collection("golfPOIs")
            .addSnapshotListener { documents, error ->
                var localGolfPOIs = mutableListOf<GolfPOIModel2>()
                var localGolfPOI = GolfPOIModel2()

                if (error == null) {
                    if (documents != null && !documents.isEmpty) {
                       for (document in documents) {
                           if (document != null) {
                               storeResultInObject(localGolfPOI, document)
                               i("FirebaseDB POI documents 123: $localGolfPOI ")
                               localGolfPOIs.add(localGolfPOI.copy())
                           }
                        }
                        golfPOIs.value = localGolfPOIs
                    }
                }
            }
    }


    // Create a New Course POI on the Firestone DB
    override fun createPOI(golfPOI: GolfPOIModel2) {

        val uid = database.collection("tmp").document().id
        golfPOI.uid = uid

        val golfPOIMap = golfPOI.toMap()
        database.collection("golfPOIs")
            .document(uid)
            .set(golfPOIMap)
            .addOnSuccessListener {
                i( "Firebase Added a new POI")
            }
            .addOnFailureListener {
                i("Firebase Error adding a new POI  ")
            }
    }

    // Update a Course POI on the FireStone DB
    override fun updatePOI(golfPOI: GolfPOIModel2) {
        var updatePOI = golfPOI.toMap()

        database.collection("golfPOIs")
            .document(golfPOI.uid)
            .set(updatePOI, SetOptions.merge())
    }

    // Removing
    override fun removePOI(golfPOI: GolfPOIModel2) {
        database.collection("golfPOIs")
            .document(golfPOI.uid)
            .delete()
            .addOnSuccessListener { i("Firebase successful delete") }
            .addOnSuccessListener { i("Firebase failed to delete") }
    }

    override fun findPOI(position: Int): GolfPOIModel2 {
        TODO("Not yet implemented")
    }

    override fun findPOI(uid: String, golfPOI: MutableLiveData<GolfPOIModel2>) {
        var localGolfPOI = GolfPOIModel2()
        database.collection("golfPOIs")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    storeResultInObjectSingle(localGolfPOI, document)
                }
                golfPOI.value = localGolfPOI
            }
    }


    override fun findPOIByCreatedByUserId(uid: String, golfPOIs: MutableLiveData<List<GolfPOIModel2>>) {

        database.collection("golfPOIs")
            .whereEqualTo("createdById", uid)
            .get()
            .addOnSuccessListener {
                documents -> run {
                var localGolfPOIs = mutableListOf<GolfPOIModel2>()
                var localGolfPOI = GolfPOIModel2()
                for (document in documents) {
                    i("FirebaseDB POI by Id document: ${document.data} ")
                    i("FirebaseDB POI by Id document uid: ${document.data.getValue("uid").toString()} ")
                    if (document != null) {
                        storeResultInObject(localGolfPOI, document)

                        localGolfPOIs.add(localGolfPOI.copy())
                    }

                }
                golfPOIs.value = localGolfPOIs

                }
            }
    }

    // This will retrieve from Firestone a MutableLiveData list of favourite courses of the user with uid passed in
    override fun findUsersFavouriteCourses(uid: String, golfPOIs: MutableLiveData<List<GolfPOIModel2>>) {
        i("Firebase Finding Favorites1: $uid")

        database.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                document -> run {
                    var favorites = mutableListOf<String>()
                    i("Firebase Finding Favorites2: ${document.data}")
                    if (document.data != null) {
                        favorites = document.data?.getValue("favorites") as MutableList<String>

                        var localGolfPOIs = mutableListOf<GolfPOIModel2>()
                        var localGolfPOI = GolfPOIModel2()
                        favorites.forEach {
                            database.collection("golfPOIs")
                                .document(it)
                                .get()
                                .addOnSuccessListener { document ->
                                    run {

                                        if (document.data != null) {
                                            storeResultInObjectSingle(localGolfPOI, document)
                                            localGolfPOIs.add(localGolfPOI.copy())
                                        }
                                    }
                                }
                            golfPOIs.value = localGolfPOIs
                        }
                    }
            }
        }
    }

    override fun createUser(user: GolfUserModel2) {
        i("Firebase Checking Google user exists first: ${user.userEmail}")
        database.collection("users")
            .whereEqualTo("userEmail", user.userEmail)
            .get()
            .addOnSuccessListener {
                        documents -> run {
                    i("Firebase : Does User already exists on Firestone?")
                    if(documents.isEmpty){
                        addUserToFirestone(user)
                    } else {
                        i("Firebase : User does not exists on Firestone")
                    }
                }
            }.addOnFailureListener {
                addUserToFirestone(user)
            }
    }

    override fun findUser(email: String, user: MutableLiveData<GolfUserModel2>){
        database.collection("users")
            .whereEqualTo("userEmail", email)
            .get()
            .addOnSuccessListener {

                documents ->
                    var localUser = GolfUserModel2()
                    for (document in documents) {
                        i("FirebaseDB user document: ${document.data} ")
                        i("FirebaseDB user document uid: ${document.data.getValue("uid").toString()} ")
                        localUser.uid = document.data.getValue("uid").toString()
                        localUser.userEmail = document.data.getValue("userEmail").toString()
                        localUser.firstName = document.data.getValue("firstName").toString()
                        localUser.lastName = document.data.getValue("lastName").toString()
                        localUser.loginCount = document.data.getValue("loginCount") as Long
                        localUser.favorites = document.data.getValue("favorites") as MutableList<String>
                }
                user.value = localUser
            }
            .addOnFailureListener { exception ->
                i("Error retrieveing User data : $exception")
            }
    }

    // Update a user details
    override fun updateUser(user: GolfUserModel2, golfPOIs: MutableLiveData<List<GolfPOIModel2>>) {
        i("Firebase user being update : $user")
        var updateUser = user.toMap()
        database.collection("users")
            .document(user.uid)
            .set(updateUser, SetOptions.merge())
            .addOnSuccessListener {
                findUsersFavouriteCourses(user.uid, golfPOIs)
            }
    }

    // Stores the document which is the result of the query to firebase into an Object
    private fun storeResultInObject(localGolfPOI: GolfPOIModel2, document: QueryDocumentSnapshot) {
        localGolfPOI.uid = document.data.getValue("uid").toString()
        localGolfPOI.courseTitle = document.data.getValue("courseTitle").toString()
        localGolfPOI.courseDescription = document.data.getValue("courseDescription").toString()
        localGolfPOI.courseProvince = document.data.getValue("courseProvince").toString()
        localGolfPOI.coursePar = document.data.getValue("coursePar").toString().toInt()
        localGolfPOI.lat = document.data.getValue("lat") as Double
        localGolfPOI.lng = document.data.getValue("lng") as Double
        localGolfPOI.zoom = document.data.getValue("zoom").toString().toFloat()
        localGolfPOI.createdById = document.data.getValue("createdById").toString()
        localGolfPOI.image = Uri.parse(document.data.getValue("image").toString())

    }

    private fun storeResultInObjectSingle(localGolfPOI: GolfPOIModel2, document: DocumentSnapshot) {
        localGolfPOI.uid = document.data?.getValue("uid").toString()
        localGolfPOI.courseTitle = document.data?.getValue("courseTitle").toString()
        localGolfPOI.courseDescription = document.data?.getValue("courseDescription").toString()
        localGolfPOI.courseProvince = document.data?.getValue("courseProvince").toString()
        localGolfPOI.coursePar = document.data?.getValue("coursePar").toString().toInt()
        localGolfPOI.lat = document.data?.getValue("lat") as Double
        localGolfPOI.lng = document.data!!.getValue("lng") as Double
        localGolfPOI.zoom = document.data!!.getValue("zoom").toString().toFloat()
        localGolfPOI.createdById = document.data!!.getValue("createdById").toString()
        localGolfPOI.image = Uri.parse(document.data!!.getValue("image").toString())
    }

    private fun addUserToFirestone(user: GolfUserModel2) {
        var userMap = user.toMap()
        database.collection("users")
            .document(user.uid)
            .set(userMap)
            .addOnSuccessListener {
                i("Firebase DocumentSnapshot added with ID")
            }
            .addOnFailureListener {
                i("Firebase Error adding document ")
            }
    }


}