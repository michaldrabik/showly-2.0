package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.DiscoverMovie

@Dao
interface DiscoverMoviesDao {

  @Query("SELECT * FROM movies_discover ORDER BY id")
  suspend fun getAll(): List<DiscoverMovie>

  @Query("SELECT * from movies_discover ORDER BY created_at DESC LIMIT 1")
  suspend fun getMostRecent(): DiscoverMovie?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(movies: List<DiscoverMovie>)

  @Query("DELETE FROM movies_discover")
  suspend fun deleteAll()

  @Transaction
  suspend fun replace(movies: List<DiscoverMovie>) {
    deleteAll()
    upsert(movies)
  }
}
