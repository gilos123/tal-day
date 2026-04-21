package com.maayan.studytracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val useDynamicColorKey = booleanPreferencesKey("use_dynamic_color")

    /**
     * True when the user has opted into Android 12+ Material You dynamic colors.
     * Defaults to false so the Streak brand shows on fresh installs.
     */
    val useDynamicColor: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[useDynamicColorKey] ?: false }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[useDynamicColorKey] = enabled }
    }
}
