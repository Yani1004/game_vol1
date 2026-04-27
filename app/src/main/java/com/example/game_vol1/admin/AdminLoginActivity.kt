package com.example.game_vol1.admin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.game_vol1.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AdminLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AdminAccessManager.isAdmin(this)) {
            goToDashboard()
            return
        }

        setContentView(R.layout.activity_admin_login)

        val toolbar = findViewById<Toolbar>(R.id.adminLoginToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val tilEmail = findViewById<TextInputLayout>(R.id.tilAdminEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilAdminPassword)
        val etEmail = findViewById<TextInputEditText>(R.id.etAdminEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etAdminPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnAdminLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            var valid = true
            if (email.isBlank()) { tilEmail.error = "Enter admin email"; valid = false } else tilEmail.error = null
            if (password.isBlank()) { tilPassword.error = "Enter password"; valid = false } else tilPassword.error = null
            if (!valid) return@setOnClickListener

            if (AdminAccessManager.login(this, email, password)) {
                goToDashboard()
            } else {
                Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show()
                tilPassword.error = "Wrong email or password"
            }
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, AdminDashboardActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
