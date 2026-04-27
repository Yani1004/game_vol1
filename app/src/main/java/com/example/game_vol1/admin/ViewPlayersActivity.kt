package com.example.game_vol1.admin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.game_vol1.R
import com.example.game_vol1.admin.adapter.PlayerAdapter
import com.example.game_vol1.admin.viewmodel.PlayerViewModel

class ViewPlayersActivity : AppCompatActivity() {

    private val vm: PlayerViewModel by viewModels()
    private lateinit var adapter: PlayerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_players)

        val toolbar = findViewById<Toolbar>(R.id.playersToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Players"

        adapter = PlayerAdapter { player ->
            startActivity(Intent(this, PlayerDetailsActivity::class.java).apply {
                putExtra(PlayerDetailsActivity.EXTRA_PLAYER_ID, player.id)
                putExtra(PlayerDetailsActivity.EXTRA_PLAYER_NAME, player.username)
            })
        }

        val rv = findViewById<RecyclerView>(R.id.rvPlayers)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        vm.players.observe(this) { adapter.submitList(it) }

        val searchView = findViewById<SearchView>(R.id.searchPlayers)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true.also { vm.search(q.orEmpty()) }
            override fun onQueryTextChange(q: String?) = true.also { vm.search(q.orEmpty()) }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
