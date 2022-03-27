package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.CustomList

interface CustomListsLocalDataSource {

  suspend fun insert(items: List<CustomList>): List<Long>

  suspend fun update(items: List<CustomList>)

  suspend fun getAll(): List<CustomList>

  suspend fun getById(id: Long): CustomList?

  suspend fun updateTraktId(id: Long, idTrakt: Long, idSlug: String, timestamp: Long)

  suspend fun updateTimestamp(id: Long, timestamp: Long)

  suspend fun updateSortByLocal(id: Long, sortBy: String, sortHow: String, timestamp: Long)

  suspend fun updateFilterTypeLocal(id: Long, filterType: String, timestamp: Long)

  suspend fun deleteById(id: Long)

  suspend fun deleteAll()
}
