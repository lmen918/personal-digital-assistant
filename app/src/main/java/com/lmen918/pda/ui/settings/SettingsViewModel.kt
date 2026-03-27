package com.lmen918.pda.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmen918.pda.reminder.ReminderFrequency
import com.lmen918.pda.reminder.ReminderPreferencesRepository
import com.lmen918.pda.reminder.ReminderScheduler
import com.lmen918.pda.reminder.ReminderSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val reminderPreferencesRepository: ReminderPreferencesRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    var reminderEnabled by mutableStateOf(false)
        private set
    var frequency by mutableStateOf(ReminderFrequency.WEEKLY)
        private set
    var dayOfWeek by mutableIntStateOf(Calendar.SATURDAY)
        private set
    var dayOfMonth by mutableIntStateOf(28)
        private set
    var hourOfDay by mutableIntStateOf(8)
        private set
    var minute by mutableIntStateOf(0)
        private set

    init {
        viewModelScope.launch {
            val settings = reminderPreferencesRepository.settings.first()
            apply(settings)
        }
    }

    fun updateReminderEnabled(enabled: Boolean) {
        reminderEnabled = enabled
    }

    fun updateFrequency(value: ReminderFrequency) {
        frequency = value
    }

    fun updateDayOfWeek(value: Int) {
        dayOfWeek = value
    }

    fun updateDayOfMonth(value: Int) {
        dayOfMonth = value.coerceIn(1, 31)
    }

    fun setTime(hour: Int, minute: Int) {
        hourOfDay = hour
        this.minute = minute
    }

    suspend fun saveReminderSettings(): String {
        val settings = ReminderSettings(
            enabled = reminderEnabled,
            frequency = frequency,
            dayOfWeek = dayOfWeek,
            dayOfMonth = dayOfMonth,
            hourOfDay = hourOfDay,
            minute = minute
        )

        reminderPreferencesRepository.save(settings)
        reminderScheduler.scheduleNext(settings)
        return if (settings.enabled) "Reminder saved" else "Reminder disabled"
    }

    private fun apply(settings: ReminderSettings) {
        reminderEnabled = settings.enabled
        frequency = settings.frequency
        dayOfWeek = settings.dayOfWeek
        dayOfMonth = settings.dayOfMonth
        hourOfDay = settings.hourOfDay
        minute = settings.minute
    }
}


