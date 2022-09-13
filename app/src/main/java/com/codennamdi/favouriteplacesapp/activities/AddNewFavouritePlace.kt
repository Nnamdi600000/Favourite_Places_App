package com.codennamdi.favouriteplacesapp.activities

import android.Manifest
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
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
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceApp
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceDao
import com.codennamdi.favouriteplacesapp.database.FavouritePlaceEntity
import com.codennamdi.favouriteplacesapp.databinding.ActivityAddNewFavouritePlaceBinding
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

    companion object {
        private const val IMAGE_DIRECTORY = "FavouritePlacesImages"
    }

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
        formatDate()
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
            addDetailsToDataBase(favouritePlaceDao)
        }
    }

    private fun addDetailsToDataBase(favouritePlaceDao: FavouritePlaceDao) {
        val title = binding.textFieldTitle.text.toString()
        val description = binding.textFieldDescription.text.toString()
        val date = binding.textFieldDate.text.toString()
        val location = binding.textFieldLocation.text.toString()
        val image = saveImageToInternalStorage.toString()

        when {
            binding.textFieldTitle.text.isNullOrEmpty() -> {
                Toast.makeText(this@AddNewFavouritePlace, "Please add a title", Toast.LENGTH_LONG)
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
                Toast.makeText(this@AddNewFavouritePlace, "Details saved", Toast.LENGTH_LONG).show()
                binding.textFieldTitle.text?.clear()
                binding.textFieldDescription.text?.clear()
                binding.textFieldLocation.text?.clear()
                binding.textFieldDate.text?.clear()
                val intent = Intent(this@AddNewFavouritePlace, MainActivity::class.java)
                startActivity(intent)
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