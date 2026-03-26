package com.lmen918.pda.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class EventWithTags(
    @Embedded val event: EventEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EventTagCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)
