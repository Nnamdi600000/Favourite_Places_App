package com.codennamdi.favouriteplacesapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codennamdi.favouriteplacesapp.R
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceApp
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceDao
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceEntity
import com.codennamdi.favouriteplacesapp.databinding.ActivityAddNewFavouritePlaceBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddNewFavouritePlace : AppCompatActivity() {
    private var calendar = Calendar.getInstance()
    private lateinit var binding: ActivityAddNewFavouritePlaceBinding
    private lateinit var dateSetOnClickListener: DatePickerDialog.OnDateSetListener
    private lateinit var saveImageToInternalStorage: Uri
    private var mLongitude: Double = 0.0
    private var mLatitude: Double = 0.0
    private var favouritePlacesDetails: FavouritePlaceEntity? = null
    private lateinit var mFusedClientLocation: FusedLocationProviderClient

    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBackground: ImageView = binding.placeImage
                imageBackground.setImageURI(result.data?.data)
                val imageAsBitmap = (imageBackground.drawable as BitmapDrawable).bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(imageAsBitmap)
                Log.e("Saved image", "$saveImageToInternalStorage")
            }
        }

    private var getAction =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val thumbNail = it!!.data!!.extras!!.get("data") as Bitmap
            binding.placeImage.setImageBitmap(thumbNail)
            saveImageToInternalStorage = saveImageToInternalStorage(thumbNail)
            Log.e("Saved image", "$saveImageToInternalStorage")
        }

    private var placeAutoCompleteResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == PLACE_AUTO_COMPLETE_REQUEST_CODE) {
//                val place: Place = Autocomplete.getPlaceFromIntent(result.data!!)
//                binding.textFieldLocation.setText(place.address)
//                mLatitude = place.latLng!!.latitude
//                mLongitude = place.latLng!!.longitude
//            }

            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    result.data?.let {
                        val place: Place = Autocomplete.getPlaceFromIntent(result.data!!)
                        binding.textFieldLocation.setText(place.address)
                        mLatitude = place.latLng!!.latitude
                        mLongitude = place.latLng!!.longitude
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    result.data?.let {
                        val status = Autocomplete.getStatusFromIntent(result.data!!)
                        Log.i(TAG, status.statusMessage ?: "")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(
                        this@AddNewFavouritePlace,
                        "The user cancelled the search",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    companion object {
        private const val IMAGE_DIRECTORY = "FavouritePlacesImages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewFavouritePlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedClientLocation = LocationServices.getFusedLocationProviderClient(this)

        setSupportActionBar(binding.addFavouritePlaceToolbar)
        title = "Add Favourite Place"
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        if (intent.hasExtra(MainActivity.EXTRA_FAVOURITE_PLACE_DETAILS)) {
            favouritePlacesDetails =
                intent.getSerializableExtra(MainActivity.EXTRA_FAVOURITE_PLACE_DETAILS) as FavouritePlaceEntity
            setUpdateDetailsValue()
        }

        if (!Places.isInitialized()) {
            Places.initialize(this@AddNewFavouritePlace, getString(R.string.places_api_key))
        }

        setOnClickListeners()
        formatDate()
    }

    private fun setUpdateDetailsValue() {
        supportActionBar?.title = "Edit Favourite Place Details"
        binding.textFieldTitle.setText(favouritePlacesDetails?.title)
        binding.textFieldDescription.setText(favouritePlacesDetails?.description)
        binding.textFieldDate.setText(favouritePlacesDetails?.date)
        binding.textFieldLocation.setText(favouritePlacesDetails?.location)

        //To display the image
        saveImageToInternalStorage = Uri.parse(favouritePlacesDetails?.image)
        binding.placeImage.setImageURI(saveImageToInternalStorage)

        binding.addButton.text = "Update"
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

        binding.addButton.setOnClickListener {
            val favouritePlaceDao = (application as FavouritePlaceApp).db.favouritePlaceDao()
            // val favourPlaceDetailId = favouritePlacesDetails!!.id
            addDetailsToDataBase(favouritePlaceDao)
        }

        binding.textFieldLocation.setOnClickListener {
            try {
                val fields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this@AddNewFavouritePlace)
                placeAutoCompleteResultLauncher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.setCurrentLocation.setOnClickListener {
            getUserLocationPermission()
        }
    }

    private fun addDetailsToDataBase(favouritePlaceDao: FavouritePlaceDao) {
        if (favouritePlacesDetails == null) {
            val title = binding.textFieldTitle.text.toString()
            val description = binding.textFieldDescription.text.toString()
            val date = binding.textFieldDate.text.toString()
            val location = binding.textFieldLocation.text.toString()
            val image = saveImageToInternalStorage.toString()

            when {
                binding.textFieldTitle.text.isNullOrEmpty() -> {
                    Toast.makeText(
                        this@AddNewFavouritePlace,
                        "Please add a title",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                binding.textFieldDescription.text.isNullOrEmpty() -> {
                    Toast.makeText(
                        this@AddNewFavouritePlace,
                        "Please add a description",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                binding.textFieldLocation.text.isNullOrEmpty() -> {
                    Toast.makeText(
                        this@AddNewFavouritePlace,
                        "Please add a location",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                saveImageToInternalStorage == null -> {
                    Toast.makeText(
                        this@AddNewFavouritePlace,
                        "Please select an image",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                else -> {
                    lifecycleScope.launch {
                        favouritePlaceDao.insert(
                            FavouritePlaceEntity(
                                image = image,
                                title = title,
                                description = description,
                                date = date,
                                location = location,
                                longitude = mLongitude,
                                latitude = mLatitude
                            )
                        )
                    }

                    Toast.makeText(this@AddNewFavouritePlace, "Details saved", Toast.LENGTH_LONG)
                        .show()
                    binding.textFieldTitle.text?.clear()
                    binding.textFieldDescription.text?.clear()
                    binding.textFieldLocation.text?.clear()
                    binding.textFieldDate.text?.clear()
                    val intent = Intent(this@AddNewFavouritePlace, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        } else {
            val updateTitle = binding.textFieldTitle.text.toString()
            val updateDescription = binding.textFieldDescription.text.toString()
            val updateDate = binding.textFieldDate.text.toString()
            val updateLocation = binding.textFieldLocation.text.toString()
            val updateImage = saveImageToInternalStorage.toString()

            if (updateTitle.isNotEmpty() && updateDescription.isNotEmpty() && updateDate.isNotEmpty()
                && updateLocation.isNotEmpty() && updateImage.isNotEmpty()
            ) {
                lifecycleScope.launch {
                    favouritePlaceDao.update(
                        FavouritePlaceEntity(
                            //id = id,
                            image = updateImage,
                            title = updateTitle,
                            description = updateDescription,
                            location = updateLocation,
                            date = updateDate,
                            longitude = 0.0,
                            latitude = 0.0
                        )
                    )
                }
                Toast.makeText(this@AddNewFavouritePlace, "Details Updated!", Toast.LENGTH_LONG)
                    .show()
                binding.textFieldTitle.text?.clear()
                binding.textFieldDescription.text?.clear()
                binding.textFieldLocation.text?.clear()
                binding.textFieldDate.text?.clear()
                val intent = Intent(this@AddNewFavouritePlace, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@AddNewFavouritePlace,
                    "Please input a valid text!",
                    Toast.LENGTH_LONG
                ).show()
            }
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
                    launchTheCameraApp()
                }
            }
        }
        imageDialog.show()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this@AddNewFavouritePlace)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val pickIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickIntent)
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

    private fun launchTheCameraApp() {
        Dexter.withContext(this@AddNewFavouritePlace)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        getAction.launch(intent)
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

    private fun getUserLocationPermission() {
        if (!isLocationEnabled()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Please enable your location provider, it is turned off.",
                Snackbar.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withContext(this@AddNewFavouritePlace)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestClientLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                this@AddNewFavouritePlace,
                                "Please grant all permissions, if not the app won't work.",
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
    }

    //This function gets the location of the user.
    @SuppressLint("MissingPermission")
    private fun requestClientLocationData() {
        val mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority =
            com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedClientLocation.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    //Here we are getting the users current location.
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation

            val latitude = mLastLocation?.latitude
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation?.longitude
            Log.i("Current Longitude", "$longitude")
        }
    }

    private fun isLocationEnabled(): Boolean {
        //This provide access to the system location service.
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
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

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
}