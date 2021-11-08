package org.wit.golfpoi.main

import android.app.Application
import android.net.Uri
import org.wit.golfpoi.models.*
import timber.log.Timber
import timber.log.Timber.i
import java.time.LocalDate

class MainApp : Application() {

    //val golfPOIs = ArrayList<GolfPOIModel>()
    lateinit var golfPOIData: GolfPOIStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        i("Golf POI started")

        golfPOIData = GolfPOIJSONStore(applicationContext)

        if (golfPOIData.findUser("davidcorrigan1@gmail.com") == null) {

            golfPOIData.createUser(GolfUserModel(2000, "davidcorrigan1@gmail.com","Pass1976", "David", "Corrigan", LocalDate.now(), 1 ))
            golfPOIData.createUser(GolfUserModel(2000, "bencorrigan1@gmail.com","Pass1976", "Ben", "Corrigan", LocalDate.now(), 1 ))

            val testUser = golfPOIData.findUser("bencorrigan1@gmail.com")
            val testUser2 = golfPOIData.findUser("davidcorrigan1@gmail.com")

            if (testUser != null && testUser2 != null) {
                golfPOIData.setCurrentUser(testUser)
                golfPOIData.createPOI(GolfPOIModel(1000,"Wexford Golf Course", "Beautiful par 72 in the South East of Ireland. The greens on this course are like carpet and the fairways are immalculate.", "Leinster", 72, Uri.EMPTY, 52.32580421972287, -6.466550110840797, 16f, golfPOIData.getCurrentUser().id))
                golfPOIData.setCurrentUser(testUser2)
                golfPOIData.createPOI(GolfPOIModel(1001,"Kerry Golf Course", "Beautiful par 71 in the South west of this beautiful country. The greens on this course are like carpet and the fairways are immalculate.", "Munster", 71, Uri.EMPTY, 51.87514821972287, -9.672122510840797, 15f, golfPOIData.getCurrentUser().id))
                golfPOIData.setCurrentUser(testUser)
                golfPOIData.createPOI(GolfPOIModel(1002,"Westport Golf Club", "Beautiful par 70 in the west of Ireland. The greens on this course are like carpet and the fairways are immalculate.", "Connaght", 70, Uri.EMPTY, 53.81460421972287, -9.562150110840797, 15f, golfPOIData.getCurrentUser().id))
                golfPOIData.setCurrentUser(testUser2)
                golfPOIData.createPOI(GolfPOIModel(1003,"Royal Portrush Golf Club", "Beautiful par 72 in the North west of this beautiful country. The greens on this course are like carpet and the fairways are immalculate. I would totally recommend this course to anyone wishing to play", "Ulster", 72, Uri.EMPTY, 55.19960421972287, -6.635150110840797, 15f, golfPOIData.getCurrentUser().id))
                golfPOIData.setCurrentUser(testUser)
                golfPOIData.createPOI(GolfPOIModel(1004,"Clare Golf Course", "Beautiful par 71 in the west of the country. The greens on this course are like carpet and the fairways are immalculate. I would totally recommend this course to anyone wishing to play", "Connaght", 71, Uri.EMPTY, 52.24260421972287, -7.138150110840797, 15f, golfPOIData.getCurrentUser().id))
                golfPOIData.createPOI(GolfPOIModel(1005,"Roscommon Golf Course", "Beautiful par 70 in the Midlands of this beautiful country. The greens on this course are like carpet and the fairways are immalculate. I would totally recommend this course to anyone wishing to play", "Ulster", 70, Uri.EMPTY, 52.25260421972287, -7.138150110840797, 15f, golfPOIData.getCurrentUser().id))
                golfPOIData.createPOI(GolfPOIModel(1006,"Portmarnock Golf Club", "Beautiful par 72 in the East of Ireland. The greens on this course are like carpet and the fairways are immalculate. I would totally recommend this course to anyone wishing to play", "Leinster", 72, Uri.EMPTY, 53.40895571972287, -6.122448210840797, 15f, golfPOIData.getCurrentUser().id))
                golfPOIData.createPOI(GolfPOIModel(1007,"Macreddin Golf Course", "Beautiful par 71 in the East of this beautiful county. The greens on this course are like carpet and the fairways are immalculate. I would totally recommend this course to anyone wishing to play", "Leinster", 71, Uri.EMPTY, 52.88020421972287, -6.329150110840797, 15f, golfPOIData.getCurrentUser().id))
            }
        }

    }
}