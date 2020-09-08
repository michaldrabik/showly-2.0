package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.storage.database.model.ArchiveShow
import com.michaldrabik.storage.database.model.Show

@Dao
interface ArchiveShowsDao {

  @Query("SELECT shows.*, shows_archive.updated_at AS updated_at FROM shows INNER JOIN shows_archive USING(id_trakt)")
  suspend fun getAll(): List<Show>

  @Query("SELECT shows.*, shows_archive.updated_at AS updated_at FROM shows INNER JOIN shows_archive USING(id_trakt) WHERE id_trakt IN (:ids)")
  suspend fun getAll(ids: List<Long>): List<Show>

  @Query("SELECT shows.* FROM shows INNER JOIN shows_archive USING(id_trakt) WHERE id_trakt == :traktId")
  suspend fun getById(traktId: Long): Show?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(shows: List<ArchiveShow>)

  @Query("DELETE FROM shows_archive WHERE id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)
}
