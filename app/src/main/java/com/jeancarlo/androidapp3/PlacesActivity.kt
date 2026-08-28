package com.jeancarlo.androidapp3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeancarlo.androidapp3.data.AppDatabase
import com.jeancarlo.androidapp3.data.TreasureRepository
import com.jeancarlo.androidapp3.databinding.ActivityPlacesBinding
import kotlinx.coroutines.launch

/**
 * Shows all 20 hunt locations using RecyclerView.
 * This gives the user a progress overview while the MainActivity controls the sequence.
 */
class PlacesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlacesBinding
    private lateinit var repository: TreasureRepository
    private lateinit var adapter: TreasurePlaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "All Hunt Stops"

        repository = TreasureRepository(
            AppDatabase.getDatabase(applicationContext).treasurePlaceDao()
        )

        adapter = TreasurePlaceAdapter { place ->
            val intent = Intent(this, PlaceDetailActivity::class.java)
            intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, place.id)
            startActivity(intent)
        }

        binding.placesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.placesRecyclerView.adapter = adapter

        // Return to the previous screen.
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            repository.seedDatabaseIfNeeded()
            adapter.submitPlaces(repository.getAllPlaces())
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
