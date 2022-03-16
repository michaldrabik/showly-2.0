// ktlint-disable max-line-length
package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.ArchiveMovie
import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.sources.ArchiveMoviesLocalDataSource

@Dao
interface ArchiveMoviesDao : ArchiveMoviesLocalDataSource {

  @Query("SELECT movies.*, movies_archive.created_at AS created_at, movies_archive.updated_at AS updated_at FROM movies INNER JOIN movies_archive USING(id_trakt)")
  override suspend fun getAll(): List<Movie>

  @Query("SELECT movies.*, movies_archive.created_at AS created_at, movies_archive.updated_at AS updated_at FROM movies INNER JOIN movies_archive USING(id_trakt) WHERE id_trakt IN (:ids)")
  override suspend fun getAll(ids: List<Long>): List<Movie>

  @Query("SELECT movies.id_trakt FROM movies INNER JOIN movies_archive USING(id_trakt)")
  override suspend fun getAllTraktIds(): List<Long>

  @Query("SELECT movies.* FROM movies INNER JOIN movies_archive USING(id_trakt) WHERE id_trakt == :traktId")
  override suspend fun getById(traktId: Long): Movie?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insert(movie: ArchiveMovie)

  @Query("DELETE FROM movies_archive WHERE id_trakt == :traktId")
  override suspend fun deleteById(traktId: Long)
}
