package org.wit.golfpoi

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.wit.golfpoi.models.GolfPOIMemStore
import java.time.LocalDate

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class GolfPOIInstrumentedTest {
    lateinit var golfPOIData: GolfPOIStore

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.wit.golfpoi", appContext.packageName)
    }

    @Test
    fun testUserCreated () {
        golfPOIData = GolfPOIMemStore()
        golfPOIData.createUser(GolfUserModel(1, "test1@gmail.com", "password", "TestFirstName", "TestLastName", LocalDate.now(), 1 ))
        val foundUser: GolfUserModel? = golfPOIData.findUser("test1@gmail.com")
        assertEquals("TestFirstName", foundUser?.firstName)
        assertEquals("TestLastName", foundUser?.lastName)
        assertEquals("password", foundUser?.userPassword)
        assertEquals(1L, foundUser?.loginCount)
    }

    @Test
    fun testPOICreated() {
        golfPOIData = GolfPOIMemStore()
        golfPOIData.createUser(GolfUserModel(1, "test1@gmail.com", "password", "TestFirstName", "TestLastName", LocalDate.now(), 1 ))
        val newUser: GolfUserModel? = golfPOIData.findUser("test1@gmail.com")
        if (newUser != null) {
            golfPOIData.createPOI(
                GolfPOIModel(
                    1000,
                    "Test Golf Course Name",
                    "Test Desc",
                    "Munster",
                    72,
                    Uri.EMPTY,
                    52.25260421972287,
                    -7.338150110840797,
                    16f,
                    newUser.id
                )
            )
        }
        val foundPOIs = golfPOIData.findAllPOIs()

        assertEquals("Test Golf Course Name", foundPOIs[0]?.courseTitle)
        assertEquals("Test Desc", foundPOIs[0]?.courseDescription)
        assertEquals(52.25260421972287, foundPOIs[0]?.lat, 0.0000001)

    }

    @Test
    fun testFindPOIById() {
        golfPOIData = GolfPOIMemStore()
        golfPOIData.createUser(GolfUserModel(1, "test1@gmail.com", "password", "TestFirstName", "TestLastName", LocalDate.now(), 1 ))
        val newUser: GolfUserModel? = golfPOIData.findUser("test1@gmail.com")
        if (newUser != null) {
            golfPOIData.createPOI(
                GolfPOIModel(
                    1000,
                    "Test Golf Course Name",
                    "Test Desc",
                    "Munster",
                    72,
                    Uri.EMPTY,
                    52.25260421972287,
                    -7.338150110840797,
                    16f,
                    newUser.id
                )
            )
        }
        val foundPOIs = golfPOIData.findAllPOIs()
        val foundPOIById = golfPOIData.findPOI(foundPOIs[0].id)

        assertEquals(foundPOIs[0]?.courseTitle, foundPOIById?.courseTitle)
    }

}