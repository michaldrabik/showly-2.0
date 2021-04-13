package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.michaldrabik.data_local.database.model.CustomList

@Dao
interface CustomListsDao : BaseDao<CustomList> {

  @Query("SELECT * FROM custom_lists ORDER BY created_at DESC")
  suspend fun getAll(): List<CustomList>

  @Query("SELECT * FROM custom_lists WHERE id == :id")
  suspend fun getById(id: Long): CustomList?

  @Query("UPDATE custom_lists SET id_trakt = :idTrakt, id_slug = :idSlug, updated_at = :timestamp WHERE id == :id")
  suspend fun updateTraktId(id: Long, idTrakt: Long, idSlug: String, timestamp: Long)

  @Query("UPDATE custom_lists SET updated_at = :timestamp WHERE id == :id")
  suspend fun updateTimestamp(id: Long, timestamp: Long)

  @Query("UPDATE custom_lists SET sort_by_local = :sortBy, updated_at = :timestamp WHERE id == :id")
  suspend fun updateSortByLocal(id: Long, sortBy: String, timestamp: Long)

  @Query("UPDATE custom_lists SET filter_type_local = :filterType, updated_at = :timestamp WHERE id == :id")
  suspend fun updateFilterTypeLocal(id: Long, filterType: String, timestamp: Long)

  @Query("DELETE FROM custom_lists WHERE id == :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM custom_lists")
  suspend fun deleteAll()
}
