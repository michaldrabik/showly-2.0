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

  @Query("SELECT * FROM shows INNER JOIN shows_followed ON shows_followed.id_trakt == :traktId")
  suspend fun getById(traktId: Long): Show?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(shows: List<FollowedShow>)

  @Query("DELETE FROM shows_followed WHERE id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)
}