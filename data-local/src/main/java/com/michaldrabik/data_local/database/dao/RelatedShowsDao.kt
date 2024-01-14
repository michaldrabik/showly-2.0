package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.RelatedShow
import com.michaldrabik.data_local.sources.RelatedShowsLocalDataSource

@Dao
interface RelatedShowsDao : RelatedShowsLocalDataSource {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  override suspend fun insert(items: List<RelatedShow>): List<Long>

  @Query("SELECT * FROM shows_related WHERE id_trakt_related_show == :traktId")
  override suspend fun getAllById(traktId: Long): List<RelatedShow>

  @Query("SELECT * FROM shows_related")
  override suspend fun getAll(): List<RelatedShow>

  @Query("DELETE FROM shows_related WHERE id_trakt_related_show == :traktId")
  override suspend fun deleteById(traktId: Long)
}
