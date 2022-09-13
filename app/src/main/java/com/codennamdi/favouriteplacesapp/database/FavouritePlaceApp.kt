package com.codennamdi.favouriteplacesapp.database

import android.app.Application

class FavouritePlaceApp : Application() {
    val db by lazy {
        FavouritePlaceDatabase.getInstance(this)
    }
}