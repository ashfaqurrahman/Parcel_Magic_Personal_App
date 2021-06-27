package com.airposted.bohon.data.network.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferenceProvider(
    context: Context
) {

    private val appContext = context.applicationContext

    private val preference: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(appContext)


    fun saveSharedPreferences(key: String, savedAt: String) {
        preference.edit().putString(
            key,
            savedAt
        ).apply()
    }

    fun getSharedPreferences(key: String): String? {
        return preference.getString(key, null)
    }

    fun deleteSharedPreferences(key: String) {
        preference.edit().remove(
            key
        ).apply()
    }
}