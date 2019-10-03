package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.storage.database.model.FollowedShow
import com.michaldrabik.storage.database.model.Show

@Dao
interface FollowedShowsDao {

  @Query("SELECT * FROM shows INNER JOIN shows_followed ON shows_followed.id_trakt == shows.id_trakt")
  suspend fun getAll(): List<Show>

  @Query("SELECT * FROM shows INNER JOIN shows_followed ON shows_followed.id_trakt == shows.id_trakt ORDER BY id DESC")
  suspend fun getAllRecent(): List<Show>

  @Query("SELECT * FROM shows INNER JOIN shows_followed ON shows_followed.id_trakt == shows.id_trakt WHERE shows.id_trakt == :traktId")
  suspend fun getById(traktId: Long): Show?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(show: FollowedShow)

  @Query("DELETE FROM shows_followed WHERE id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)
}