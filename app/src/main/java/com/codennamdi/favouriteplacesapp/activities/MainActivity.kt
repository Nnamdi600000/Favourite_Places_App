package com.codennamdi.favouriteplacesapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codennamdi.favouriteplacesapp.adapters.ItemListFavouritePlacesAdapter
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceApp
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceEntity
import com.codennamdi.favouriteplacesapp.databinding.ActivityMainBinding
import com.codennamdi.favouriteplacesapp.utils.SwipeToDeleteCallback
import com.codennamdi.favouriteplacesapp.utils.SwipeToEditCallback
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingButtonId.setOnClickListener {
            val intent = Intent(this@MainActivity, AddNewFavouritePlace::class.java)
            startActivity(intent)
        }

        val favouritePlaceDao = (application as FavouritePlaceApp).db.favouritePlaceDao()
        lifecycleScope.launch {
            favouritePlaceDao.fetchAllFavouritePlaces().collect {
                val list = ArrayList(it)
                setUpRecyclerViewAdapter(list)
            }
        }
    }

    private fun setUpRecyclerViewAdapter(favouritePlacesList: ArrayList<FavouritePlaceEntity>) {
        if (favouritePlacesList.isNotEmpty()) {
            val favouritePlacesAdapter =
                ItemListFavouritePlacesAdapter(favouritePlacesList, this@MainActivity)
            binding.recyclerViewWidgetId.layoutManager = LinearLayoutManager(this)
            binding.textViewNoPlaceAvailable.visibility = View.GONE
            binding.recyclerViewWidgetId.visibility = View.VISIBLE
            binding.recyclerViewWidgetId.adapter = favouritePlacesAdapter

            //For the recyclerview clickListener
            favouritePlacesAdapter.setOnClickListener(object :
                ItemListFavouritePlacesAdapter.OnClickListener {
                override fun onClick(position: Int, entityItems: FavouritePlaceEntity) {
                    val intent = Intent(this@MainActivity, FavouritePlaceDetails::class.java)
                    intent.putExtra(EXTRA_FAVOURITE_PLACE_DETAILS, entityItems)
                    startActivity(intent)
                }
            })

            val editSwipeHandler = object : SwipeToEditCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter =
                        binding.recyclerViewWidgetId.adapter as ItemListFavouritePlacesAdapter
                    adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition)
                }
            }

            val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter =
                        binding.recyclerViewWidgetId.adapter as ItemListFavouritePlacesAdapter
                    val favouritePlaceDao =
                        (application as FavouritePlaceApp).db.favouritePlaceDao()
                    lifecycleScope.launch {
                        adapter.notifyDeletedItem(viewHolder.adapterPosition, favouritePlaceDao)
                    }
                    Toast.makeText(this@MainActivity, "Deleted!", Toast.LENGTH_LONG).show()
                }
            }

            val editTouchHelper = ItemTouchHelper(editSwipeHandler)
            val deleteTouchHelper = ItemTouchHelper(deleteSwipeHandler)
            editTouchHelper.attachToRecyclerView(binding.recyclerViewWidgetId)
            deleteTouchHelper.attachToRecyclerView(binding.recyclerViewWidgetId)
        } else {
            binding.recyclerViewWidgetId.visibility = View.GONE
            binding.textViewNoPlaceAvailable.visibility = View.VISIBLE
        }
    }

    companion object {
        var EXTRA_FAVOURITE_PLACE_DETAILS = "favourite place details"
    }
}