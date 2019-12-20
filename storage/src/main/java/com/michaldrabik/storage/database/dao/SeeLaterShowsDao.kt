package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.storage.database.model.SeeLaterShow
import com.michaldrabik.storage.database.model.Show

@Dao
interface SeeLaterShowsDao {

  @Query("SELECT shows.*, shows_see_later.updated_at FROM shows INNER JOIN shows_see_later USING(id_trakt)")
  suspend fun getAll(): List<Show>

  @Query("SELECT shows.* FROM shows INNER JOIN shows_see_later ON shows_see_later.id_trakt == shows.id_trakt WHERE shows.id_trakt == :traktId")
  suspend fun getById(traktId: Long): Show?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(show: SeeLaterShow)

  @Query("DELETE FROM shows_see_later WHERE id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)
}