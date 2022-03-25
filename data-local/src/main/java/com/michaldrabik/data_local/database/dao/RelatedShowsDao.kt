package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.michaldrabik.data_local.database.model.RelatedShow
import com.michaldrabik.data_local.sources.RelatedShowsLocalDataSource

@Dao
interface RelatedShowsDao : BaseDao<RelatedShow>, RelatedShowsLocalDataSource {

  @Query("SELECT * FROM shows_related WHERE id_trakt_related_show == :traktId")
  override suspend fun getAllById(traktId: Long): List<RelatedShow>

  @Query("SELECT * FROM shows_related")
  override suspend fun getAll(): List<RelatedShow>

  @Query("DELETE FROM shows_related WHERE id_trakt_related_show == :traktId")
  override suspend fun deleteById(traktId: Long)
}
