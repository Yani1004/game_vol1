package com.example.game_vol1

import android.content.Context

object UiLanguageStore {
    private const val PREFS_NAME = "ui_language_prefs"
    private const val KEY_LANGUAGE = "language"
    private const val LANGUAGE_BG = "bg"
    private const val LANGUAGE_EN = "en"

    fun isBulgarian(context: Context): Boolean =
        prefs(context).getString(KEY_LANGUAGE, LANGUAGE_EN) == LANGUAGE_BG

    fun toggle(context: Context): Boolean {
        val nowBulgarian = !isBulgarian(context)
        prefs(context).edit()
            .putString(KEY_LANGUAGE, if (nowBulgarian) LANGUAGE_BG else LANGUAGE_EN)
            .apply()
        return nowBulgarian
    }

    fun pick(context: Context, bg: String, en: String): String =
        if (isBulgarian(context)) bg else en

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
