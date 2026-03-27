package com.lmen918.pda.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleNext(settings: ReminderSettings) {
        if (!settings.enabled) {
            cancel()
            return
        }

        val triggerAtMillis = calculateNextTrigger(settings, System.currentTimeMillis())
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = reminderPendingIntent(context)

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancel() {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(reminderPendingIntent(context))
    }

    private fun reminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val REMINDER_REQUEST_CODE = 42021

        internal fun calculateNextTrigger(settings: ReminderSettings, nowMillis: Long): Long {
            val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val next = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.HOUR_OF_DAY, settings.hourOfDay)
                set(Calendar.MINUTE, settings.minute)
            }

            when (settings.frequency) {
                ReminderFrequency.WEEKLY -> {
                    next.set(Calendar.DAY_OF_WEEK, settings.dayOfWeek)
                    if (next.timeInMillis <= now.timeInMillis) {
                        next.add(Calendar.WEEK_OF_YEAR, 1)
                    }
                }

                ReminderFrequency.MONTHLY -> {
                    setMonthlyDay(next, settings.dayOfMonth)
                    if (next.timeInMillis <= now.timeInMillis) {
                        next.add(Calendar.MONTH, 1)
                        setMonthlyDay(next, settings.dayOfMonth)
                    }
                }
            }

            return next.timeInMillis
        }

        private fun setMonthlyDay(calendar: Calendar, preferredDay: Int) {
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            calendar.set(Calendar.DAY_OF_MONTH, preferredDay.coerceIn(1, maxDay))
        }
    }
}

