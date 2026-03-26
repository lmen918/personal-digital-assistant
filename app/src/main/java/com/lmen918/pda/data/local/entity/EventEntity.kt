package com.lmen918.pda.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val calendarEventId: Long? = null,
    val notifyInApp: Boolean = false,
    val notifyMinutesBefore: Int = 15,
    val isAllDay: Boolean = false
)
