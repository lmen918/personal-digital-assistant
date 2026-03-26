package com.lmen918.pda.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["eventId", "tagId"],
    indices = [Index("eventId"), Index("tagId")]
)
data class EventTagCrossRef(
    val eventId: Long,
    val tagId: Long
)
