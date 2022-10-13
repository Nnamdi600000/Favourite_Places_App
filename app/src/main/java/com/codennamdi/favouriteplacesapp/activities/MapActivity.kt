package com.codennamdi.favouriteplacesapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codennamdi.favouriteplacesapp.R
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceEntity
import com.codennamdi.favouriteplacesapp.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private var favouritePlaceEntityItems: FavouritePlaceEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (favouritePlaceEntityItems != null) {
            setSupportActionBar(binding.favouritePlaceMapToolbar)
            title = favouritePlaceEntityItems!!.title
            if (supportActionBar != null) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            }
            binding.favouritePlaceMapToolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        if (intent.hasExtra(MainActivity.EXTRA_FAVOURITE_PLACE_DETAILS)) {
            favouritePlaceEntityItems =
                intent.getSerializableExtra(MainActivity.EXTRA_FAVOURITE_PLACE_DETAILS) as FavouritePlaceEntity
        }

        val supportMapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position =
            LatLng(favouritePlaceEntityItems!!.latitude, favouritePlaceEntityItems!!.longitude)
        googleMap.addMarker(
            MarkerOptions().position(position).title(favouritePlaceEntityItems!!.location)
        )
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 10f)
        googleMap.animateCamera(newLatLngZoom)
    }
}