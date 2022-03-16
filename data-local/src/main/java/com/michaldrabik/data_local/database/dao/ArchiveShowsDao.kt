// ktlint-disable max-line-length
package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.ArchiveShow
import com.michaldrabik.data_local.database.model.Show
import com.michaldrabik.data_local.sources.ArchiveShowsLocalDataSource

@Dao
interface ArchiveShowsDao : ArchiveShowsLocalDataSource {

  @Query("SELECT shows.*, shows_archive.created_at AS created_at, shows_archive.updated_at AS updated_at FROM shows INNER JOIN shows_archive USING(id_trakt)")
  override suspend fun getAll(): List<Show>

  @Query("SELECT shows.*, shows_archive.created_at AS created_at, shows_archive.updated_at AS updated_at FROM shows INNER JOIN shows_archive USING(id_trakt) WHERE id_trakt IN (:ids)")
  override suspend fun getAll(ids: List<Long>): List<Show>

  @Query("SELECT shows.id_trakt FROM shows INNER JOIN shows_archive USING(id_trakt)")
  override suspend fun getAllTraktIds(): List<Long>

  @Query("SELECT shows.* FROM shows INNER JOIN shows_archive USING(id_trakt) WHERE id_trakt == :traktId")
  override suspend fun getById(traktId: Long): Show?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insert(show: ArchiveShow)

  @Query("DELETE FROM shows_archive WHERE id_trakt == :traktId")
  override suspend fun deleteById(traktId: Long)
}
