package com.example.game_vol1.admin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.game_vol1.R
import com.example.game_vol1.admin.adapter.GeoLocationAdapter
import com.example.game_vol1.admin.viewmodel.GeoLocationViewModel
import com.example.game_vol1.database.entity.GeoLocationEntity
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageGeoLocationsActivity : AppCompatActivity() {

    private val vm: GeoLocationViewModel by viewModels()
    private lateinit var adapter: GeoLocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_geolocations)

        val toolbar = findViewById<Toolbar>(R.id.manageGeoToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Geolocations"

        adapter = GeoLocationAdapter(onEdit = ::openEdit, onDelete = ::confirmDelete)
        val rv = findViewById<RecyclerView>(R.id.rvGeoLocations)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        vm.geoLocations.observe(this) { adapter.submitList(it) }
        vm.operationResult.observe(this) { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }

        // Declare chipGroup first so the search listener can reference it
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupDifficulty)

        // Text search — clears any active difficulty chip
        val searchView = findViewById<SearchView>(R.id.searchGeo)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true.also {
                chipGroup.clearCheck()
                vm.search(q.orEmpty())
            }
            override fun onQueryTextChange(q: String?) = true.also {
                if (q.orEmpty().isNotBlank()) chipGroup.clearCheck()
                vm.search(q.orEmpty())
            }
        })

        // Difficulty filter chips — use filterByDifficulty, not text search
        chipGroup.setOnCheckedStateChangeListener { _, ids ->
            val selected = when (ids.firstOrNull()) {
                R.id.chipEasy -> "Easy"
                R.id.chipMedium -> "Medium"
                R.id.chipHard -> "Hard"
                else -> null
            }
            vm.filterByDifficulty(selected)
        }

        findViewById<FloatingActionButton>(R.id.fabAddGeo).setOnClickListener {
            startActivity(Intent(this, AddGeoLocationActivity::class.java))
        }
    }

    private fun openEdit(geo: GeoLocationEntity) {
        startActivity(Intent(this, AddGeoLocationActivity::class.java).apply {
            putExtra(AddGeoLocationActivity.EXTRA_EDIT_ID, geo.id)
            putExtra(AddGeoLocationActivity.EXTRA_NAME, geo.name)
            putExtra(AddGeoLocationActivity.EXTRA_COUNTRY, geo.country)
            putExtra(AddGeoLocationActivity.EXTRA_CITY, geo.city)
            putExtra(AddGeoLocationActivity.EXTRA_LAT, geo.latitude.toString())
            putExtra(AddGeoLocationActivity.EXTRA_LNG, geo.longitude.toString())
            putExtra(AddGeoLocationActivity.EXTRA_DIFFICULTY, geo.difficulty)
            putExtra(AddGeoLocationActivity.EXTRA_IMAGE_URL, geo.imageUrl)
            putExtra(AddGeoLocationActivity.EXTRA_DESCRIPTION, geo.description)
        })
    }

    private fun confirmDelete(geo: GeoLocationEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete Location")
            .setMessage("Remove \"${geo.name}\" from the game?")
            .setPositiveButton("Delete") { _, _ -> vm.delete(geo) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
