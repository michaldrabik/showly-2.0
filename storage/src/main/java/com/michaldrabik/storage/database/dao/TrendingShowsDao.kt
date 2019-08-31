package com.michaldrabik.storage.database.dao

import androidx.room.*
import com.michaldrabik.storage.database.model.Show
import com.michaldrabik.storage.database.model.TrendingShow

@Dao
interface TrendingShowsDao {

  @Query("SELECT * FROM shows INNER JOIN shows_trending ON shows_trending.id_trakt == shows.id_trakt")
  suspend fun getAll(): List<Show>

  @Query("SELECT * from shows_trending ORDER BY created_at LIMIT 1")
  suspend fun getMostRecent(): TrendingShow?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(shows: List<TrendingShow>)

  @Query("DELETE FROM shows_trending")
  suspend fun deleteAll()

  @Transaction
  suspend fun deleteAllAndInsert(shows: List<TrendingShow>) {
    deleteAll()
    insert(shows)
  }
}