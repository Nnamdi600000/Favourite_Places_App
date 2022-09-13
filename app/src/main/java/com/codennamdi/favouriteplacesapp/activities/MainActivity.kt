package com.codennamdi.favouriteplacesapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codennamdi.favouriteplacesapp.databinding.ActivityMainBinding

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

    }
}