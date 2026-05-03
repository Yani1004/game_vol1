package com.example.game_vol1.admin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.game_vol1.BuildConfig
import com.example.game_vol1.GeoMenuActivity

object AdminAccessManager {
    private const val PREFS = "admin_session"
    private const val KEY_IS_ADMIN = "is_admin"

    // Demo credentials are injected from Gradle properties / BuildConfig.
    val ADMIN_EMAIL: String = BuildConfig.ADMIN_EMAIL
    private val adminPassword: String
        get() = BuildConfig.ADMIN_PASSWORD

    fun login(context: Context, email: String, password: String): Boolean {
        if (ADMIN_EMAIL.isBlank() || adminPassword.isBlank()) return false
        val isValid = email.trim().equals(ADMIN_EMAIL, ignoreCase = true) && password == adminPassword
        if (isValid) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_IS_ADMIN, true)
                .apply()
        }
        return isValid
    }

    fun isAdmin(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_IS_ADMIN, false)

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_IS_ADMIN)
            .apply()
    }

    fun enforceAdminOrRedirect(activity: Activity): Boolean {
        if (isAdmin(activity)) return true

        Toast.makeText(activity, "Access denied", Toast.LENGTH_LONG).show()
        activity.startActivity(
            Intent(activity, GeoMenuActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
        )
        activity.finish()
        return false
    }
}
