package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.RecentSearch
import com.michaldrabik.data_local.sources.RecentSearchLocalDataSource

@Dao
interface RecentSearchDao : RecentSearchLocalDataSource {

  @Query("SELECT * FROM recent_searches ORDER BY created_at DESC LIMIT :limit")
  override suspend fun getAll(limit: Int): List<RecentSearch>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun upsert(searches: List<RecentSearch>)

  @Query("DELETE FROM recent_searches")
  override suspend fun deleteAll()
}
