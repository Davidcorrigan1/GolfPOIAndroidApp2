package org.wit.golfpoi.models

import android.content.Context
import android.net.Uri
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.wit.golfpoi.helpers.*
import timber.log.Timber
import java.lang.reflect.Type
import java.time.LocalDate
import java.util.*

// This is the combined
const val JSON_FILE_DATA = "golfPOIData.json"

val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .registerTypeAdapter(LocalDate::class.java, DateParser())
    .create()
val listTypeDATA: Type = object : TypeToken<GolfPOIDataModel>() {}.type

// Generate randon id for data key
fun generateRandomId(): Long {
    return Random().nextLong()
}

class GolfPOIJSONStore(private val context: Context) : GolfPOIStore {

    private var golfPOIData : GolfPOIDataModel = GolfPOIDataModel()
    private var currentUser : GolfUserModel = GolfUserModel()

    // Deserialize the POI and User files into the Data objects
    init {
        if (exists(context, JSON_FILE_DATA)) {
            deserialize(JSON_FILE_DATA, listTypeDATA)
        }
    }

    // Return all the existing Golf Courses
    override fun findAllPOIs(): MutableList<GolfPOIModel> {
        return golfPOIData.golfPOIs
    }

    // Create a new Golf Course in the List and update the JSON file
    override fun createPOI(golfPOI: GolfPOIModel) {
        golfPOI.id = generateRandomId()
        golfPOI.createdById = currentUser.id
        golfPOIData.golfPOIs.add(golfPOI)
        serialize(JSON_FILE_DATA, listTypeDATA)
    }


    // Update the golfPOI object passed in as reference and update JSON file
    override fun updatePOI(golfPOI: GolfPOIModel) {
        Timber.i("In updatePOI: $golfPOI")
        val foundGolfPOI : GolfPOIModel? = golfPOIData.golfPOIs.find { p -> p.id == golfPOI.id }
        if (foundGolfPOI != null) {
            Timber.i("in updatePOI after finding POI: $foundGolfPOI")
            foundGolfPOI.courseTitle = golfPOI.courseTitle
            foundGolfPOI.courseDescription = golfPOI.courseDescription
            foundGolfPOI.courseProvince = golfPOI.courseProvince
            foundGolfPOI.coursePar = golfPOI.coursePar
            foundGolfPOI.image = golfPOI.image
            foundGolfPOI.lat = golfPOI.lat
            foundGolfPOI.lng = golfPOI.lng
            foundGolfPOI.zoom = golfPOI.zoom
            serialize(JSON_FILE_DATA, listTypeDATA)
            logAll()
        }
    }

    // Remove the item at position passed in, and update the JSON file
    override fun removePOI(position: Int) {
        if (golfPOIData.golfPOIs[position] != null) {
            golfPOIData.golfPOIs.removeAt(position)
            serialize(JSON_FILE_DATA, listTypeDATA)
        }
    }

    // Find a Golf Course POI based on id
    override fun findPOI(id: Long): GolfPOIModel? {
        return golfPOIData.golfPOIs.find { p -> p.id == id }
    }

    // Find all Golf Course entries created by the id passed in
    override fun findByCreatedByUserId(id: Long): List<GolfPOIModel> {
        return golfPOIData.golfPOIs.filter { p -> p.createdById == id}
    }

    // Generate a user id and add user passed in to the array and update JSON File
    override fun createUser(user: GolfUserModel) {
        user.id = generateRandomId()
        golfPOIData.users.add(user)
        serialize(JSON_FILE_DATA, listTypeDATA)
        logAllUsers()
    }

    // Use the email address to find a user if it exists, if found check the
    // password matches the supplied. If match return the user object else null.
    override fun findUser(email: String): GolfUserModel? {
        Timber.i("user entered Email: $email")
        var userFound: GolfUserModel? = golfPOIData.users.find{ it.userEmail.lowercase() == email.lowercase() }
        Timber.i("userFound: $userFound")
        if (userFound != null) {
            return userFound
        } else {
            userFound = null
        }
        return userFound
    }

    // Set the passed in user to be the current User
    override fun setCurrentUser(user: GolfUserModel) {
        currentUser = user
    }

    override fun getCurrentUser(): GolfUserModel {
        return currentUser
    }


    private fun serialize(fileName: String, listType: Type) {
        val jsonString: String
        jsonString = gsonBuilder.toJson(golfPOIData, listType)
        write(context, fileName, jsonString)
    }

    private fun deserialize(fileName: String, listType: Type) {
        val jsonString = read(context, fileName)

        golfPOIData = gsonBuilder.fromJson(jsonString, listType)

    }

    private fun logAll() {
        golfPOIData.golfPOIs.forEach { Timber.i("$it") }
    }

    private fun logAllUsers() {
        golfPOIData.users.forEach{ Timber.i("$it") }
    }
}

class UriParser : JsonDeserializer<Uri>,JsonSerializer<Uri> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Uri {
        return Uri.parse(json?.asString)
    }

    override fun serialize(
        src: Uri?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }
}

class DateParser : JsonDeserializer<LocalDate>,JsonSerializer<LocalDate> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDate {
        return LocalDate.parse(json?.asString)
    }

    override fun serialize(
        src: LocalDate?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }
}