package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.michaldrabik.storage.database.model.CustomList

@Dao
interface CustomListsDao : BaseDao<CustomList> {

  @Query("SELECT * FROM custom_lists ORDER BY created_at DESC")
  suspend fun getAll(): List<CustomList>

  @Query("SELECT * FROM custom_lists WHERE id == :id")
  suspend fun getById(id: Long): CustomList?

  @Query("UPDATE custom_lists SET updated_at = :timestamp WHERE id == :id")
  suspend fun updateTimestamp(id: Long, timestamp: Long)

  @Query("DELETE FROM custom_lists WHERE id == :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM custom_lists")
  suspend fun deleteAll()
}