package com.codennamdi.favouriteplacesapp

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codennamdi.favouriteplacesapp.databinding.ActivityAddNewFavouritePlaceBinding
import java.text.SimpleDateFormat
import java.util.*

class AddNewFavouritePlace : AppCompatActivity() {
    private var calendar = Calendar.getInstance()
    private lateinit var binding: ActivityAddNewFavouritePlaceBinding
    private lateinit var dateSetOnClickListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewFavouritePlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.addFavouritePlaceToolbar)
        title = "Add Favourite Place"
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding.addFavouritePlaceToolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.textFieldDate.setOnClickListener {
            datePickerDialog()
        }
    }

    private fun datePickerDialog() {
        dateSetOnClickListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                formatDate()
            }

        DatePickerDialog(
            this@AddNewFavouritePlace,
            dateSetOnClickListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatDate() {
        val format = "dd/mm/yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        binding.textFieldDate.setText(sdf.format(calendar.time).toString())
    }

}