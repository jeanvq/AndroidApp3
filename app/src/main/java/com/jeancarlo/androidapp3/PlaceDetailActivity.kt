package com.jeancarlo.androidapp3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jeancarlo.androidapp3.data.AppDatabase
import com.jeancarlo.androidapp3.data.TreasureRepository
import com.jeancarlo.androidapp3.databinding.ActivityPlaceDetailBinding
import kotlinx.coroutines.launch

/**
 * Detail screen for one treasure-hunt location.
 * An external geo Intent lets the user open the stop in a mapping application.
 */
class PlaceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceDetailBinding
    private lateinit var repository: TreasureRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        repository = TreasureRepository(
            AppDatabase.getDatabase(applicationContext).treasurePlaceDao()
        )

        // Return to the screen that opened this place.
        binding.backButton.setOnClickListener {
            finish()
        }

        val placeId = intent.getIntExtra(EXTRA_PLACE_ID, -1)
        if (placeId == -1) {
            finish()
            return
        }

        lifecycleScope.launch {
            val place = repository.getPlaceById(placeId) ?: run {
                finish()
                return@launch
            }

            supportActionBar?.title = place.name
            binding.stopText.text = "Stop #${place.huntOrder} of 20"
            binding.nameText.text = place.name
            binding.addressText.text = place.address
            binding.clueText.text = place.clue

            val currentPlace = repository.getCurrentPlace()

            when {
                place.isVisited -> {
                    binding.statusText.text = "Visited"
                    binding.markVisitedButton.text = "ALREADY VISITED"
                    binding.markVisitedButton.isEnabled = false
                }

                currentPlace?.id == place.id -> {
                    binding.statusText.text = "Current stop"
                    binding.markVisitedButton.text = "MARK AS VISITED"
                    binding.markVisitedButton.isEnabled = true

                    binding.markVisitedButton.setOnClickListener {
                        lifecycleScope.launch {
                            repository.markVisited(place.id)
                            Toast.makeText(
                                this@PlaceDetailActivity,
                                "${place.name} completed!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Return to the previous screen so the next stop can be shown.
                            finish()
                        }
                    }
                }

                else -> {
                    binding.statusText.text = "Locked - complete the previous stop first"
                    binding.markVisitedButton.text = "LOCKED"
                    binding.markVisitedButton.isEnabled = false
                }
            }

            binding.openMapButton.setOnClickListener {
                // geo: URI works with Google Maps and other compatible map apps.
                val uri = Uri.parse(
                    "geo:${place.latitude},${place.longitude}?q=" +
                        Uri.encode("${place.latitude},${place.longitude}(${place.name})")
                )
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_PLACE_ID = "extra_place_id"
    }
}
