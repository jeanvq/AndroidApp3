package com.jeancarlo.androidapp3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.jeancarlo.androidapp3.data.AppDatabase
import com.jeancarlo.androidapp3.data.TreasurePlace
import com.jeancarlo.androidapp3.data.TreasureRepository
import com.jeancarlo.androidapp3.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/** Main treasure-hunt screen with Maps, Room progress, and drawer navigation. */
class MainActivity : AppCompatActivity(),
    OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: TreasureRepository
    private lateinit var map: GoogleMap
    private var currentPlace: TreasurePlace? = null

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) enableMyLocation() else Toast.makeText(
                this,
                "Location permission was not granted. The hunt can still be used manually.",
                Toast.LENGTH_LONG
            ).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // ActionBarDrawerToggle creates its own icon, so tint it explicitly for contrast.
        drawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.accent_gold)
        binding.navigationView.setNavigationItemSelectedListener(this)

        val database = AppDatabase.getDatabase(applicationContext)
        repository = TreasureRepository(database.treasurePlaceDao())

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.markVisitedButton.setOnClickListener { markCurrentPlaceVisited() }
        binding.viewDetailsButton.setOnClickListener {
            currentPlace?.let { place ->
                startActivity(Intent(this, PlaceDetailActivity::class.java).apply {
                    putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, place.id)
                })
            }
        }
        binding.allStopsButton.setOnClickListener { startActivity(Intent(this, PlacesActivity::class.java)) }

        lifecycleScope.launch {
            repository.seedDatabaseIfNeeded()
            refreshHuntState()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) lifecycleScope.launch { refreshHuntState() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        requestLocationPermissionIfNeeded()
        lifecycleScope.launch {
            repository.seedDatabaseIfNeeded()
            refreshHuntState()
        }
    }

    /** Reads saved Room progress and refreshes the current hunt state. */
    private suspend fun refreshHuntState() {
        val visitedCount = repository.getVisitedCount()
        val nextPlace = repository.getCurrentPlace()
        currentPlace = nextPlace
        binding.progressText.text = getString(R.string.progress_format, visitedCount, TOTAL_STOPS)

        if (nextPlace == null) {
            showCompletedState()
        } else {
            binding.currentStopText.text = getString(R.string.current_stop_format, nextPlace.huntOrder, nextPlace.name)
            binding.clueText.text = nextPlace.clue
            binding.markVisitedButton.isEnabled = true
            binding.viewDetailsButton.isEnabled = true
            if (::map.isInitialized) showCurrentPlaceOnMap(nextPlace)
        }
    }

    /** Only the currently unlocked stop can be marked as visited. */
    private fun markCurrentPlaceVisited() {
        val place = currentPlace ?: return
        AlertDialog.Builder(this)
            .setTitle("Confirm Visit")
            .setMessage("Mark ${place.name} as visited?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    repository.markVisited(place.id)
                    Toast.makeText(this@MainActivity, "${place.name} completed!", Toast.LENGTH_SHORT).show()
                    refreshHuntState()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCurrentPlaceOnMap(place: TreasurePlace) {
        val location = LatLng(place.latitude, place.longitude)
        map.clear()
        map.addMarker(MarkerOptions().position(location).title("#${place.huntOrder} ${place.name}").snippet(place.address))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun showCompletedState() {
        binding.currentStopText.text = getString(R.string.hunt_complete)
        binding.clueText.text = getString(R.string.completion_message)
        binding.markVisitedButton.isEnabled = false
        binding.viewDetailsButton.isEnabled = false
        if (::map.isInitialized) map.clear()
        AlertDialog.Builder(this)
            .setTitle("Treasure Hunt Complete!")
            .setMessage(getString(R.string.completion_message))
            .setPositiveButton("Awesome!", null)
            .show()
    }

    private fun requestLocationPermissionIfNeeded() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) enableMyLocation() else locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                })
                finish()
            }
            R.id.nav_map -> Unit
            R.id.nav_places -> startActivity(Intent(this, PlacesActivity::class.java))
            R.id.nav_reset -> confirmReset()
        }
        binding.drawerLayout.closeDrawers()
        return true
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle("Reset Treasure Hunt")
            .setMessage("This will mark all 20 stops as not visited. Continue?")
            .setPositiveButton("Reset") { _, _ ->
                lifecycleScope.launch {
                    repository.resetProgress()
                    refreshHuntState()
                    Toast.makeText(this@MainActivity, "Progress reset.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        private const val TOTAL_STOPS = 20
    }
}
