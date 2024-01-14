package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.MovieStreaming
import com.michaldrabik.data_local.sources.MovieStreamingsLocalDataSource

@Dao
interface MovieStreamingsDao : BaseDao<MovieStreaming>, MovieStreamingsLocalDataSource {

  @Transaction
  override suspend fun replace(traktId: Long, entities: List<MovieStreaming>) {
    deleteById(traktId)
    insert(entities)
  }

  @Query("SELECT * FROM movies_streamings WHERE id_trakt == :traktId")
  override suspend fun getById(traktId: Long): List<MovieStreaming>

  @Query("DELETE FROM movies_streamings WHERE id_trakt == :traktId")
  override suspend fun deleteById(traktId: Long)

  @Query("DELETE FROM movies_streamings")
  override suspend fun deleteAll()
}
