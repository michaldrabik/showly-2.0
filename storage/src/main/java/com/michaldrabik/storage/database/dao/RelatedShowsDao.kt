package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.storage.database.model.RelatedShow
import com.michaldrabik.storage.database.model.Show

@Dao
interface RelatedShowsDao {

  @Query("SELECT * FROM shows INNER JOIN shows_related ON shows_related.id_trakt == shows.id_trakt WHERE shows_related.id_trakt_related_show == :traktId")
  suspend fun getById(traktId: Long): List<Show>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(shows: List<RelatedShow>)

  @Query("DELETE FROM shows_related WHERE id_trakt_related_show == :traktId")
  suspend fun deleteById(traktId: Long)
}