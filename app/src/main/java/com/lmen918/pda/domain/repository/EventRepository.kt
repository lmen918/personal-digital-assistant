package com.lmen918.pda.domain.repository

import android.content.Context
import com.lmen918.pda.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getAllEvents(): Flow<List<Event>>
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun getEventById(id: Long): Event?
    fun syncToCalendar(context: Context, event: Event): Long?
    suspend fun deleteCalendarEvent(context: Context, calendarEventId: Long)
}
