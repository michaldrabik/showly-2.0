package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.EpisodesSyncLog
import com.michaldrabik.data_local.sources.EpisodesSyncLogLocalDataSource

@Dao
interface EpisodesSyncLogDao : EpisodesSyncLogLocalDataSource {

  @Query("SELECT * from sync_episodes_log")
  override suspend fun getAll(): List<EpisodesSyncLog>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun upsert(log: EpisodesSyncLog)
}
