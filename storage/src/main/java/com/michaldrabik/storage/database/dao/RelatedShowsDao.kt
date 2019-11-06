package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.storage.database.model.RelatedShow

@Dao
interface RelatedShowsDao {

  @Query("SELECT * FROM shows_related WHERE id_trakt_related_show == :traktId")
  suspend fun getAllById(traktId: Long): List<RelatedShow>

  @Query("SELECT * FROM shows_related")
  suspend fun getAll(): List<RelatedShow>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(shows: List<RelatedShow>)

  @Query("DELETE FROM shows_related WHERE id_trakt_related_show == :traktId")
  suspend fun deleteById(traktId: Long)
}