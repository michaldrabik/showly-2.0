package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.DiscoverMovie
import com.michaldrabik.data_local.sources.DiscoverMoviesLocalDataSource

@Dao
interface DiscoverMoviesDao : DiscoverMoviesLocalDataSource {

  @Query("SELECT * FROM movies_discover ORDER BY id")
  override suspend fun getAll(): List<DiscoverMovie>

  @Query("SELECT * from movies_discover ORDER BY created_at DESC LIMIT 1")
  override suspend fun getMostRecent(): DiscoverMovie?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun upsert(movies: List<DiscoverMovie>)

  @Query("DELETE FROM movies_discover")
  override suspend fun deleteAll()

  @Transaction
  override suspend fun replace(movies: List<DiscoverMovie>) {
    deleteAll()
    upsert(movies)
  }
}
