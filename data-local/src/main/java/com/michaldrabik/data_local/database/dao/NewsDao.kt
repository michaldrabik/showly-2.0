package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.News
import com.michaldrabik.data_local.sources.NewsLocalDataSource

@Dao
interface NewsDao : BaseDao<News>, NewsLocalDataSource {

  @Query("SELECT * FROM news WHERE type == :type ORDER BY dated_at DESC")
  override suspend fun getAllByType(type: String): List<News>

  @Transaction
  override suspend fun replaceForType(items: List<News>, type: String) {
    deleteAllByType(type)
    insert(items)
  }

  @Query("DELETE FROM news WHERE type == :type")
  override suspend fun deleteAllByType(type: String): Int
}
