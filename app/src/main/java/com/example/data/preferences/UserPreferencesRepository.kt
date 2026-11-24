package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "farmcare_prefs")

data class UserSettings(
    val loggedInUserId: String? = null,
    val activeFarmId: String? = null,
    val themeMode: String = "SYSTEM",
    val largeText: Boolean = false,
    val weightUnit: String = "kg",
    val notificationsEnabled: Boolean = true,
    val reminderLeadMinutes: Int = 30
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val LOGGED_IN_USER_ID = stringPreferencesKey("logged_in_user_id")
        val ACTIVE_FARM_ID = stringPreferencesKey("active_farm_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LARGE_TEXT = booleanPreferencesKey("large_text")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_LEAD_MINUTES = intPreferencesKey("reminder_lead_minutes")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            loggedInUserId = prefs[Keys.LOGGED_IN_USER_ID],
            activeFarmId = prefs[Keys.ACTIVE_FARM_ID],
            themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
            largeText = prefs[Keys.LARGE_TEXT] ?: false,
            weightUnit = prefs[Keys.WEIGHT_UNIT] ?: "kg",
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            reminderLeadMinutes = prefs[Keys.REMINDER_LEAD_MINUTES] ?: 30
        )
    }

    suspend fun setSession(userId: String?, farmId: String? = null) {
        context.dataStore.edit { prefs ->
            if (userId == null) {
                prefs.remove(Keys.LOGGED_IN_USER_ID)
                prefs.remove(Keys.ACTIVE_FARM_ID)
            } else {
                prefs[Keys.LOGGED_IN_USER_ID] = userId
                if (farmId != null) {
                    prefs[Keys.ACTIVE_FARM_ID] = farmId
                }
            }
        }
    }

    suspend fun setActiveFarmId(farmId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_FARM_ID] = farmId
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[Keys.THEME_MODE] = mode }
    }

    suspend fun setLargeText(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.LARGE_TEXT] = enabled }
    }

    suspend fun setWeightUnit(unit: String) {
        context.dataStore.edit { prefs -> prefs[Keys.WEIGHT_UNIT] = unit }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setReminderLeadMinutes(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.REMINDER_LEAD_MINUTES] = minutes }
    }
}
