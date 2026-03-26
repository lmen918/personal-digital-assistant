package com.lmen918.pda.ui.events

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmen918.pda.domain.model.Event
import com.lmen918.pda.domain.repository.EventRepository
import com.lmen918.pda.domain.repository.TagRepository
import com.lmen918.pda.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val tagRepository: TagRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val events = eventRepository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags = tagRepository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getEventById(id: Long): Event? = eventRepository.getEventById(id)

    fun saveEvent(event: Event, syncToCalendar: Boolean) {
        viewModelScope.launch {
            val calendarEventId = if (syncToCalendar) {
                eventRepository.syncToCalendar(context, event)
            } else event.calendarEventId

            val eventToSave = event.copy(calendarEventId = calendarEventId)
            if (event.id == 0L) {
                val newId = eventRepository.insertEvent(eventToSave)
                if (event.notifyInApp) {
                    NotificationScheduler.scheduleNotification(context, eventToSave.copy(id = newId))
                }
            } else {
                eventRepository.updateEvent(eventToSave)
                NotificationScheduler.cancelNotification(context, event.id)
                if (event.notifyInApp) {
                    NotificationScheduler.scheduleNotification(context, eventToSave)
                }
            }
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            NotificationScheduler.cancelNotification(context, event.id)
            event.calendarEventId?.let { calId ->
                eventRepository.deleteCalendarEvent(context, calId)
            }
            eventRepository.deleteEvent(event)
        }
    }
}
