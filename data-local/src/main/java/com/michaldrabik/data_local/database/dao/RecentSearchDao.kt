package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.RecentSearch

@Dao
interface RecentSearchDao {

  @Query("SELECT * FROM recent_searches ORDER BY created_at DESC LIMIT :limit")
  suspend fun getAll(limit: Int): List<RecentSearch>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(searches: List<RecentSearch>)

  @Query("DELETE FROM recent_searches")
  suspend fun deleteAll()
}
