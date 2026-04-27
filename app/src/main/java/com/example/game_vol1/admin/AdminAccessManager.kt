package com.example.game_vol1.admin

import android.content.Context

object AdminAccessManager {
    private const val PREFS = "admin_session"
    private const val KEY_IS_ADMIN = "is_admin"

    // Default admin credentials for demo – change in production
    const val ADMIN_EMAIL = "admin@geoguesser.com"
    private const val ADMIN_PASSWORD = "Admin@2024"

    fun login(context: Context, email: String, password: String): Boolean {
        val ok = email.trim().equals(ADMIN_EMAIL, ignoreCase = true) && password == ADMIN_PASSWORD
        if (ok) context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_IS_ADMIN, true).apply()
        return ok
    }

    fun isAdmin(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_IS_ADMIN, false)

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .remove(KEY_IS_ADMIN).apply()
    }
}
