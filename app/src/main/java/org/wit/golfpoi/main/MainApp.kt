package org.wit.golfpoi.main

import android.app.Application
import android.net.Uri
import org.wit.golfpoi.models.*
import timber.log.Timber
import timber.log.Timber.i
import java.time.LocalDate

class MainApp : Application() {


    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        i("Golf POI started")


    }
}