package com.lmen918.retro.reminder

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.reminderDataStore by preferencesDataStore(name = "reminder_settings")

class ReminderPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val enabledKey = booleanPreferencesKey("enabled")
    private val frequencyKey = stringPreferencesKey("frequency")
    private val dayOfWeekKey = intPreferencesKey("day_of_week")
    private val dayOfMonthKey = intPreferencesKey("day_of_month")
    private val hourOfDayKey = intPreferencesKey("hour_of_day")
    private val minuteKey = intPreferencesKey("minute")
    private val sessionDurationMinutesKey = intPreferencesKey("session_duration_minutes")

    val settings: Flow<ReminderSettings> = context.reminderDataStore.data.map { prefs ->
        ReminderSettings(
            enabled = prefs[enabledKey] ?: false,
            frequency = ReminderFrequency.entries.firstOrNull { it.name == prefs[frequencyKey] }
                ?: ReminderFrequency.WEEKLY,
            dayOfWeek = prefs[dayOfWeekKey] ?: java.util.Calendar.SATURDAY,
            dayOfMonth = prefs[dayOfMonthKey] ?: 28,
            hourOfDay = prefs[hourOfDayKey] ?: 8,
            minute = prefs[minuteKey] ?: 0,
            sessionDurationMinutes = (prefs[sessionDurationMinutesKey] ?: 1).coerceIn(1, 60)
        )
    }

    suspend fun save(settings: ReminderSettings) {
        context.reminderDataStore.edit { prefs ->
            prefs[enabledKey] = settings.enabled
            prefs[frequencyKey] = settings.frequency.name
            prefs[dayOfWeekKey] = settings.dayOfWeek
            prefs[dayOfMonthKey] = settings.dayOfMonth
            prefs[hourOfDayKey] = settings.hourOfDay
            prefs[minuteKey] = settings.minute
            prefs[sessionDurationMinutesKey] = settings.sessionDurationMinutes.coerceIn(1, 60)
        }
    }
}

