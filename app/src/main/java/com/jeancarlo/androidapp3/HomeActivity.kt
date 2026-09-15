package com.jeancarlo.androidapp3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jeancarlo.androidapp3.data.AppDatabase
import com.jeancarlo.androidapp3.data.TreasureRepository
import com.jeancarlo.androidapp3.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch

/**
 * Welcome screen for the treasure hunt.
 * It gives the app a clear starting point and shows saved Room progress.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var repository: TreasureRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(applicationContext)
        repository = TreasureRepository(database.treasurePlaceDao())

        binding.continueHuntButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.viewStopsButton.setOnClickListener {
            startActivity(Intent(this, PlacesActivity::class.java))
        }

        lifecycleScope.launch {
            repository.seedDatabaseIfNeeded()
            refreshProgress()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) {
            lifecycleScope.launch { refreshProgress() }
        }
    }

    /** Reads the saved hunt progress so Home always reflects the latest visit. */
    private suspend fun refreshProgress() {
        val visitedCount = repository.getVisitedCount()
        binding.progressCountText.text = getString(
            R.string.home_progress_format,
            visitedCount,
            TOTAL_STOPS
        )
        binding.progressBar.max = TOTAL_STOPS
        binding.progressBar.progress = visitedCount
        binding.continueHuntButton.text = if (visitedCount == 0) {
            getString(R.string.start_hunt)
        } else {
            getString(R.string.continue_hunt)
        }
    }

    companion object {
        private const val TOTAL_STOPS = 20
    }
}
