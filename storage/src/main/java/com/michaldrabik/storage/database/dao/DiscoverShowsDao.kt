package com.michaldrabik.storage.database.dao

import androidx.room.*
import com.michaldrabik.storage.database.model.DiscoverShow
import com.michaldrabik.storage.database.model.Show

@Dao
interface DiscoverShowsDao {

  @Query("SELECT * FROM shows INNER JOIN shows_discover ON shows_discover.id_trakt == shows.id_trakt")
  suspend fun getAll(): List<Show>

  @Query("SELECT * from shows_discover ORDER BY created_at LIMIT 1")
  suspend fun getMostRecent(): DiscoverShow?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(shows: List<DiscoverShow>)

  @Query("DELETE FROM shows_discover")
  suspend fun deleteAll()

  @Transaction
  suspend fun deleteAllAndInsert(shows: List<DiscoverShow>) {
    deleteAll()
    insert(shows)
  }
}