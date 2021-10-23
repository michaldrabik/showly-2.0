// ktlint-disable max-line-length
package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.ArchiveMovie
import com.michaldrabik.data_local.database.model.Movie

@Dao
interface ArchiveMoviesDao {

  @Query("SELECT movies.*, movies_archive.created_at AS created_at, movies_archive.updated_at AS updated_at FROM movies INNER JOIN movies_archive USING(id_trakt)")
  suspend fun getAll(): List<Movie>

  @Query("SELECT movies.*, movies_archive.created_at AS created_at, movies_archive.updated_at AS updated_at FROM movies INNER JOIN movies_archive USING(id_trakt) WHERE id_trakt IN (:ids)")
  suspend fun getAll(ids: List<Long>): List<Movie>

  @Query("SELECT movies.id_trakt FROM movies INNER JOIN movies_archive USING(id_trakt)")
  suspend fun getAllTraktIds(): List<Long>

  @Query("SELECT movies.* FROM movies INNER JOIN movies_archive USING(id_trakt) WHERE id_trakt == :traktId")
  suspend fun getById(traktId: Long): Movie?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(movie: ArchiveMovie)

  @Query("DELETE FROM movies_archive WHERE id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)
}
