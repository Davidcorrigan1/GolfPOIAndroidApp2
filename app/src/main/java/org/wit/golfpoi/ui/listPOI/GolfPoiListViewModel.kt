package org.wit.golfpoi.ui.listPOI

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.golfpoi.models.GolfPOIJSONStore
import org.wit.golfpoi.models.GolfPOIModel

class GolfPoiListViewModel : ViewModel() {

    private val golfPOIs = MutableLiveData<List<GolfPOIModel>>()

    val observableGolfPOIs: LiveData<List<GolfPOIModel>>
        get() =golfPOIs

    init {
        load()

    }

    fun load() {
        //golfPOIs.value = GolfPOIJSONStore.findAllPOIs()
    }

}