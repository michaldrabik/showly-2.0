package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.MovieStreaming

@Dao
interface MovieStreamingsDao : BaseDao<MovieStreaming> {

  @Transaction
  suspend fun replace(traktId: Long, entities: List<MovieStreaming>) {
    deleteById(traktId)
    insert(entities)
  }

  @Query("SELECT * FROM movies_streamings WHERE id_trakt == :traktId")
  suspend fun getById(traktId: Long): List<MovieStreaming>

  @Query("DELETE FROM movies_streamings WHERE id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)

  @Query("DELETE FROM movies_streamings")
  suspend fun deleteAll()
}
