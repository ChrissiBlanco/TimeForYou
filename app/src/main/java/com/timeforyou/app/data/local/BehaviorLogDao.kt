package com.timeforyou.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BehaviorLogDao {
    @Query("SELECT * FROM behavior_logs ORDER BY timestampEpochMillis DESC")
    fun observeAll(): Flow<List<BehaviorLogEntity>>

    @Insert
    suspend fun insert(entity: BehaviorLogEntity)

    @Query("DELETE FROM behavior_logs")
    suspend fun clearAll()
}
