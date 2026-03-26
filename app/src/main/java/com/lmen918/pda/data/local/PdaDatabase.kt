package com.lmen918.pda.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lmen918.pda.data.local.dao.EventDao
import com.lmen918.pda.data.local.dao.TagDao
import com.lmen918.pda.data.local.entity.EventEntity
import com.lmen918.pda.data.local.entity.EventTagCrossRef
import com.lmen918.pda.data.local.entity.TagEntity

@Database(
    entities = [TagEntity::class, EventEntity::class, EventTagCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class PdaDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
    abstract fun eventDao(): EventDao
}
