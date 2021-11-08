package org.wit.golfpoi.models

// Overall Data class which define the List of Golf Course and the List of Users

data class GolfPOIDataModel(var golfPOIs: MutableList<GolfPOIModel> = mutableListOf<GolfPOIModel>(),
                            var users: MutableList<GolfUserModel> = mutableListOf<GolfUserModel>())
