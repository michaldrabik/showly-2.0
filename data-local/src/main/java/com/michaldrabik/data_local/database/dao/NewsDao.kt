package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.News

@Dao
interface NewsDao : BaseDao<News> {

  @Query("SELECT * FROM news WHERE type == :type ORDER BY dated_at DESC")
  suspend fun getAllByType(type: String): List<News>

  @Transaction
  suspend fun replaceForType(items: List<News>, type: String) {
    deleteAllByType(type)
    insert(items)
  }

  @Query("DELETE FROM news WHERE type == :type")
  suspend fun deleteAllByType(type: String): Int
}
