package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.michaldrabik.data_local.database.model.RelatedMovie

@Dao
interface RelatedMoviesDao : BaseDao<RelatedMovie> {

  @Query("SELECT * FROM movies_related WHERE id_trakt_related_movie == :traktId")
  suspend fun getAllById(traktId: Long): List<RelatedMovie>

  @Query("SELECT * FROM movies_related")
  suspend fun getAll(): List<RelatedMovie>

  @Query("DELETE FROM movies_related WHERE id_trakt_related_movie == :traktId")
  suspend fun deleteById(traktId: Long)
}
