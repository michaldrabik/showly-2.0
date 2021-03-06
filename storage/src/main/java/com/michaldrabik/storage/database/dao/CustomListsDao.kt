package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.michaldrabik.storage.database.model.CustomList

@Dao
interface CustomListsDao : BaseDao<CustomList> {

  @Query("DELETE FROM custom_lists")
  suspend fun deleteAll()
}
