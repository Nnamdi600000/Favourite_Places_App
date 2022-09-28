package com.codennamdi.favouriteplacesapp.activities

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.codennamdi.favouriteplacesapp.R
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceEntity
import com.codennamdi.favouriteplacesapp.databinding.ActivityFavouritePlaceDetailsBinding

class FavouritePlaceDetails : AppCompatActivity() {
    private lateinit var binding: ActivityFavouritePlaceDetailsBinding
    private lateinit var favouritePlaceEntityItems: FavouritePlaceEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouritePlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(MainActivity.EXTRA_FAVOURITE_PLACE_DETAILS)) {
            favouritePlaceEntityItems =
                intent.getSerializableExtra(MainActivity.EXTRA_FAVOURITE_PLACE_DETAILS) as FavouritePlaceEntity
        }

        if (favouritePlaceEntityItems != null) {
            setSupportActionBar(binding.favouritePlaceDetailsToolbar)
            title = ""
            if (supportActionBar != null) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            }
            binding.favouritePlaceDetailsToolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            binding.imageViewPlaceImage.setImageURI(favouritePlaceEntityItems.image.toUri())
            binding.textViewTitle.text = favouritePlaceEntityItems.title
            binding.textViewDescription.text = favouritePlaceEntityItems.description
        }
    }
}