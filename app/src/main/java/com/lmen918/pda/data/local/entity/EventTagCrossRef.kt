package com.lmen918.pda.data.local.entity

import androidx.room.Entity

@Entity(primaryKeys = ["eventId", "tagId"])
data class EventTagCrossRef(
    val eventId: Long,
    val tagId: Long
)
