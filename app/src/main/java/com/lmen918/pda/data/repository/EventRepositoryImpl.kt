package com.lmen918.pda.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.lmen918.pda.data.local.dao.EventDao
import com.lmen918.pda.data.local.entity.EventEntity
import com.lmen918.pda.data.local.entity.EventTagCrossRef
import com.lmen918.pda.data.local.entity.EventWithTags
import com.lmen918.pda.data.local.entity.TagEntity
import com.lmen918.pda.domain.model.Event
import com.lmen918.pda.domain.model.Tag
import com.lmen918.pda.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.TimeZone
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {

    override fun getAllEvents(): Flow<List<Event>> =
        eventDao.getAllEventsWithTags().map { list -> list.map { it.toDomain() } }

    override suspend fun insertEvent(event: Event): Long {
        val entity = event.toEntity()
        val id = eventDao.insertEvent(entity)
        event.tags.forEach { tag ->
            eventDao.insertEventTagCrossRef(EventTagCrossRef(eventId = id, tagId = tag.id))
        }
        return id
    }

    override suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event.toEntity())
        eventDao.deleteTagsForEvent(event.id)
        event.tags.forEach { tag ->
            eventDao.insertEventTagCrossRef(EventTagCrossRef(eventId = event.id, tagId = tag.id))
        }
    }

    override suspend fun deleteEvent(event: Event) =
        eventDao.deleteEvent(event.toEntity())

    override suspend fun getEventById(id: Long): Event? =
        eventDao.getEventById(id)?.let { entity ->
            Event(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                startTime = entity.startTime,
                endTime = entity.endTime,
                calendarEventId = entity.calendarEventId,
                notifyInApp = entity.notifyInApp,
                notifyMinutesBefore = entity.notifyMinutesBefore,
                isAllDay = entity.isAllDay
            )
        }

    override fun syncToCalendar(context: Context, event: Event): Long? {
        return try {
            val calendarId = getDefaultCalendarId(context) ?: return null
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description)
                put(CalendarContract.Events.DTSTART, event.startTime)
                put(CalendarContract.Events.DTEND, event.endTime)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                if (event.isAllDay) {
                    put(CalendarContract.Events.ALL_DAY, 1)
                }
            }
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri?.lastPathSegment?.toLongOrNull()
        } catch (e: SecurityException) {
            null
        }
    }

    override suspend fun deleteCalendarEvent(context: Context, calendarEventId: Long) {
        try {
            val uri = android.net.Uri.withAppendedPath(
                CalendarContract.Events.CONTENT_URI,
                calendarEventId.toString()
            )
            context.contentResolver.delete(uri, null, null)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    private fun getDefaultCalendarId(context: Context): Long? {
        return try {
            val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                "${CalendarContract.Calendars.IS_PRIMARY} = 1",
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getLong(0)
                } else null
            }
        } catch (e: SecurityException) {
            null
        }
    }

    private fun EventWithTags.toDomain() = Event(
        id = event.id,
        title = event.title,
        description = event.description,
        startTime = event.startTime,
        endTime = event.endTime,
        tags = tags.map { it.toDomain() },
        calendarEventId = event.calendarEventId,
        notifyInApp = event.notifyInApp,
        notifyMinutesBefore = event.notifyMinutesBefore,
        isAllDay = event.isAllDay
    )

    private fun TagEntity.toDomain() = Tag(id = id, name = name, colorHex = colorHex)

    private fun Event.toEntity() = EventEntity(
        id = id,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        calendarEventId = calendarEventId,
        notifyInApp = notifyInApp,
        notifyMinutesBefore = notifyMinutesBefore,
        isAllDay = isAllDay
    )
}
