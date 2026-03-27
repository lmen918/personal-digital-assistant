package com.lmen918.pda.reminder

import java.util.Calendar
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReminderSchedulerTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun weeklyReminder_schedulesLaterSameWeekWhenTargetStillAhead() {
        val now = utcMillis(2026, Calendar.MARCH, 27, 10, 0) // Friday
        val settings = ReminderSettings(
            enabled = true,
            frequency = ReminderFrequency.WEEKLY,
            dayOfWeek = Calendar.SATURDAY,
            hourOfDay = 8,
            minute = 0
        )

        val trigger = ReminderScheduler.calculateNextTrigger(settings, now)

        assertEquals(utcMillis(2026, Calendar.MARCH, 28, 8, 0), trigger)
    }

    @Test
    fun weeklyReminder_rollsToNextWeekWhenTodaysTimeHasPassed() {
        val now = utcMillis(2026, Calendar.MARCH, 28, 9, 30) // Saturday after 8am
        val settings = ReminderSettings(
            enabled = true,
            frequency = ReminderFrequency.WEEKLY,
            dayOfWeek = Calendar.SATURDAY,
            hourOfDay = 8,
            minute = 0
        )

        val trigger = ReminderScheduler.calculateNextTrigger(settings, now)

        assertEquals(utcMillis(2026, Calendar.APRIL, 4, 8, 0), trigger)
    }

    @Test
    fun monthlyReminder_staysInCurrentMonthWhenTargetStillAhead() {
        val now = utcMillis(2026, Calendar.MARCH, 10, 12, 0)
        val settings = ReminderSettings(
            enabled = true,
            frequency = ReminderFrequency.MONTHLY,
            dayOfMonth = 28,
            hourOfDay = 8,
            minute = 0
        )

        val trigger = ReminderScheduler.calculateNextTrigger(settings, now)

        assertEquals(utcMillis(2026, Calendar.MARCH, 28, 8, 0), trigger)
    }

    @Test
    fun monthlyReminder_rollsToLastDayForShortMonth() {
        val now = utcMillis(2026, Calendar.APRIL, 29, 9, 0)
        val settings = ReminderSettings(
            enabled = true,
            frequency = ReminderFrequency.MONTHLY,
            dayOfMonth = 31,
            hourOfDay = 8,
            minute = 0
        )

        val trigger = ReminderScheduler.calculateNextTrigger(settings, now)

        assertEquals(utcMillis(2026, Calendar.APRIL, 30, 8, 0), trigger)
    }

    @Test
    fun monthlyReminder_rollsToNextMonthAfterThisMonthsReminderPassed() {
        val now = utcMillis(2026, Calendar.MARCH, 28, 9, 0)
        val settings = ReminderSettings(
            enabled = true,
            frequency = ReminderFrequency.MONTHLY,
            dayOfMonth = 28,
            hourOfDay = 8,
            minute = 0
        )

        val trigger = ReminderScheduler.calculateNextTrigger(settings, now)

        assertEquals(utcMillis(2026, Calendar.APRIL, 28, 8, 0), trigger)
    }

    private fun utcMillis(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

