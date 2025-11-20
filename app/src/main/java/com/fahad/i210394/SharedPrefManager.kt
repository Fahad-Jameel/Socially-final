package com.fahad.i210394

import android.content.Context

object SharedPrefManager {
    private const val PREF_NAME = "MyAppPrefs"
    private const val USER_ID = "user_id"

    fun saveUserId(context: Context, id: Int) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(pref.edit()) {
            putInt(USER_ID, id)
            apply()
        }
    }

    fun getUserId(context: Context): Int {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getInt(USER_ID, -1)
    }
}
