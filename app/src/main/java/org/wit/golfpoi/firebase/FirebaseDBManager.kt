package org.wit.golfpoi.firebase

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.wit.golfpoi.helpers.readImageFromPath
import org.wit.golfpoi.models.*
import timber.log.Timber.i
import java.io.ByteArrayOutputStream
import java.io.File

class FirebaseDBManager(application: Application) : GolfPOIStoreInterface {

    private val database = Firebase.firestore
    private var st: StorageReference = FirebaseStorage.getInstance().reference
    private var context = application.applicationContext

    // Find all Course POIs and update the passed in MutableLiveData List passed in.
    override fun findAllPOIs(golfPOIs: MutableLiveData<List<GolfPOIModel>>) {

        database.collection("golfPOIs")
            .get()
            .addOnSuccessListener { documents ->
                var localGolfPOIs = mutableListOf<GolfPOIModel>()
                var localGolfPOI = GolfPOIModel()
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
    override fun setOnChangeListenerPOIs(golfPOIs: MutableLiveData<List<GolfPOIModel>>) {

        database.collection("golfPOIs")
            .addSnapshotListener { documents, error ->
                var localGolfPOIs = mutableListOf<GolfPOIModel>()
                var localGolfPOI = GolfPOIModel()

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
    override fun createPOI(golfPOI: GolfPOIModel) {

        val uid = database.collection("tmp").document().id
        golfPOI.uid = uid

        val golfPOIMap = golfPOI.toMap()
        database.collection("golfPOIs")
            .document(uid)
            .set(golfPOIMap)
            .addOnSuccessListener {
                i( "Firebase Added a new POI")
                if (golfPOI.image != Uri.EMPTY) {
                    updateImage(golfPOI)
                }
            }
            .addOnFailureListener {
                i("Firebase Error adding a new POI  ")
            }
    }

    // Update a Course POI on the FireStone DB
    override fun updatePOI(golfPOI: GolfPOIModel) {
        var updatePOI = golfPOI.toMap()

        database.collection("golfPOIs")
            .document(golfPOI.uid)
            .set(updatePOI, SetOptions.merge())
            .addOnSuccessListener {
                if (golfPOI.image != Uri.EMPTY) {
                    updateImage(golfPOI)
                }
            }

    }

    // Removing
    override fun removePOI(golfPOI: GolfPOIModel) {
        database.collection("golfPOIs")
            .document(golfPOI.uid)
            .delete()
            .addOnSuccessListener { i("Firebase successful delete") }
            .addOnSuccessListener { i("Firebase failed to delete") }
    }

    override fun findPOI(position: Int): GolfPOIModel {
        TODO("Not yet implemented")
    }

    override fun findPOI(uid: String, golfPOI: MutableLiveData<GolfPOIModel>) {
        var localGolfPOI = GolfPOIModel()
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

    // Find all the courses which were created on the system by a user. Retrived into a mutable Livedata list
    override fun findPOIByCreatedByUserId(uid: String, golfPOIs: MutableLiveData<List<GolfPOIModel>>) {

        database.collection("golfPOIs")
            .whereEqualTo("createdById", uid)
            .get()
            .addOnSuccessListener {
                documents -> run {
                var localGolfPOIs = mutableListOf<GolfPOIModel>()
                var localGolfPOI = GolfPOIModel()
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
    override fun findUsersFavouriteCourses(uid: String, golfPOIs: MutableLiveData<List<GolfPOIModel>>) {
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

                        var localGolfPOIs = mutableListOf<GolfPOIModel>()
                        var localGolfPOI = GolfPOIModel()
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

    // This method will create a new user on the Firestone DB in the 'users' collection
    override fun createUser(user: GolfUserModel) {
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

    // This will search the 'users' collection by userEmail returning a user into livedata object
    override fun findUser(email: String, user: MutableLiveData<GolfUserModel>){
        database.collection("users")
            .whereEqualTo("userEmail", email)
            .get()
            .addOnSuccessListener {

                documents ->
                    var localUser = GolfUserModel()
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

    // Update a user details in FireStone 'users' collection
    override fun updateUser(user: GolfUserModel, golfPOIs: MutableLiveData<List<GolfPOIModel>>) {
        i("Firebase user being update : $user")
        var updateUser = user.toMap()
        database.collection("users")
            .document(user.uid)
            .set(updateUser, SetOptions.merge())
            .addOnSuccessListener {
                findUsersFavouriteCourses(user.uid, golfPOIs)
            }
    }

    // Update image in the Firebase Storage and update the golfPOI image field with location
    fun updateImage(golfPOI: GolfPOIModel) {
        if (golfPOI.image != Uri.EMPTY) {
            var imageString = golfPOI.image.toString()
            val fileName = File(imageString)
            val imageName = fileName.getName()

            var imageRef = st.child(golfPOI.uid + '/' + imageName)
            val baos = ByteArrayOutputStream()
            val bitmap = readImageFromPath(context, imageString)

            bitmap?.let {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val uploadTask = imageRef.putBytes(data)
                uploadTask.addOnFailureListener {
                    println(it.message)
                }.addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        golfPOI.image = it
                        var updatePOI = golfPOI.toMap()
                        database.collection("golfPOIs").document(golfPOI.uid).set(updatePOI, SetOptions.merge())
                    }
                }
            }
        }
    }

    // Stores the document which is the result of the query to firebase into an Object
    private fun storeResultInObject(localGolfPOI: GolfPOIModel, document: QueryDocumentSnapshot) {
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

    private fun storeResultInObjectSingle(localGolfPOI: GolfPOIModel, document: DocumentSnapshot) {
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

    // This adds a new to the Firestone 'users' collection
    private fun addUserToFirestone(user: GolfUserModel) {
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