package com.lmen918.pda.data.local.dao

import androidx.room.*
import com.lmen918.pda.data.local.entity.EventEntity
import com.lmen918.pda.data.local.entity.EventTagCrossRef
import com.lmen918.pda.data.local.entity.EventWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Transaction
    @Query("SELECT * FROM events ORDER BY startTime ASC")
    fun getAllEventsWithTags(): Flow<List<EventWithTags>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventTagCrossRef(crossRef: EventTagCrossRef)

    @Query("DELETE FROM EventTagCrossRef WHERE eventId = :eventId")
    suspend fun deleteTagsForEvent(eventId: Long)

    @Transaction
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventWithTagsById(id: Long): EventWithTags?
}
