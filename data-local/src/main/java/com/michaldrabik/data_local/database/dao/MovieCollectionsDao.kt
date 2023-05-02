package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.MovieCollection
import com.michaldrabik.data_local.sources.MovieCollectionsLocalDataSource

@Dao
interface MovieCollectionsDao : BaseDao<MovieCollection>, MovieCollectionsLocalDataSource {

  @Query("SELECT * FROM movies_collections WHERE id_trakt == :traktId")
  override suspend fun getById(traktId: Long): MovieCollection?

  @Query("SELECT * FROM movies_collections WHERE id_trakt_movie == :movieTraktId")
  override suspend fun getByMovieId(movieTraktId: Long): List<MovieCollection>

  @Transaction
  override suspend fun replace(
    movieTraktId: Long,
    entities: List<MovieCollection>,
  ) {
    deleteByMovieId(movieTraktId)
    insert(entities)
  }

  @Query("DELETE FROM movies_collections WHERE id_trakt_movie == :movieTraktId")
  suspend fun deleteByMovieId(movieTraktId: Long)
}
