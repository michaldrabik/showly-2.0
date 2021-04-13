package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.michaldrabik.data_local.database.model.RelatedShow

@Dao
interface RelatedShowsDao : BaseDao<RelatedShow> {

  @Query("SELECT * FROM shows_related WHERE id_trakt_related_show == :traktId")
  suspend fun getAllById(traktId: Long): List<RelatedShow>

  @Query("SELECT * FROM shows_related")
  suspend fun getAll(): List<RelatedShow>

  @Query("DELETE FROM shows_related WHERE id_trakt_related_show == :traktId")
  suspend fun deleteById(traktId: Long)
}
