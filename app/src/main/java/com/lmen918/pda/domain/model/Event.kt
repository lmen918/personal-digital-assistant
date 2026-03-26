package com.lmen918.pda.domain.model

data class Event(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val tags: List<Tag> = emptyList(),
    val calendarEventId: Long? = null,
    val notifyInApp: Boolean = false,
    val notifyMinutesBefore: Int = 15,
    val isAllDay: Boolean = false
)
