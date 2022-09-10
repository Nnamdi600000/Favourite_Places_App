package com.codennamdi.favouriteplacesapp

import android.Manifest
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.codennamdi.favouriteplacesapp.databinding.ActivityAddNewFavouritePlaceBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
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
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.addFavouritePlaceToolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.textFieldDate.setOnClickListener {
            datePickerDialog()
        }

        binding.addImageBtn.setOnClickListener {
            displayAlertDialog()
        }
    }

    private fun displayAlertDialog() {
        val imageDialog = AlertDialog.Builder(this@AddNewFavouritePlace)
        imageDialog.setTitle("Choose Action")
        val imageItems =
            arrayOf("Select from phone Gallery", "Use phone camera to the capture the image")

        imageDialog.setItems(imageItems) { _, which ->
            when (which) {
                0 -> {
                    choosePhotoFromGallery()
                }

                1 -> {
                    Toast.makeText(
                        this@AddNewFavouritePlace,
                        "The camera functionality is coming soon...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        imageDialog.show()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this@AddNewFavouritePlace)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        Toast.makeText(
                            this@AddNewFavouritePlace,
                            "All permissions are granted",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    displayRationalDialog()
                }
            }).onSameThread().check()
    }

    private fun displayRationalDialog() {
        AlertDialog.Builder(this@AddNewFavouritePlace)
            .setMessage("You did not enable the permissions for that, you can do that by heading to settings of the app")

            .setPositiveButton("GOTO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
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