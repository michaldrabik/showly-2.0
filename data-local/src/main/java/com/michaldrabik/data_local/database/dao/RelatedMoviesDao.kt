package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.michaldrabik.data_local.database.model.RelatedMovie
import com.michaldrabik.data_local.sources.RelatedMoviesLocalDataSource

@Dao
interface RelatedMoviesDao : BaseDao<RelatedMovie>, RelatedMoviesLocalDataSource {

  @Query("SELECT * FROM movies_related WHERE id_trakt_related_movie == :traktId")
  override suspend fun getAllById(traktId: Long): List<RelatedMovie>

  @Query("SELECT * FROM movies_related")
  override suspend fun getAll(): List<RelatedMovie>

  @Query("DELETE FROM movies_related WHERE id_trakt_related_movie == :traktId")
  override suspend fun deleteById(traktId: Long)
}
