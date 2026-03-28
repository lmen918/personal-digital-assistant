package com.lmen918.retro.reminder

enum class ReminderFrequency {
    WEEKLY,
    MONTHLY
}

data class ReminderSettings(
    val enabled: Boolean = false,
    val frequency: ReminderFrequency = ReminderFrequency.WEEKLY,
    val dayOfWeek: Int = java.util.Calendar.SATURDAY,
    val dayOfMonth: Int = 28,
    val hourOfDay: Int = 8,
    val minute: Int = 0,
    val sessionDurationMinutes: Int = 1
)
