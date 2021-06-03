package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.ShowStreaming

@Dao
interface ShowStreamingsDao : BaseDao<ShowStreaming> {

  @Transaction
  suspend fun replace(traktId: Long, entities: List<ShowStreaming>) {
    deleteById(traktId)
    insert(entities)
  }

  @Query("SELECT * FROM shows_streamings WHERE id_trakt == :traktId")
  suspend fun getById(traktId: Long): List<ShowStreaming>

  @Query("DELETE FROM shows_streamings WHERE id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)

  @Query("DELETE FROM shows_streamings")
  suspend fun deleteAll()
}
