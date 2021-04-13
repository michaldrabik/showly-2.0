package com.michaldrabik.data_local.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(items: List<T>): List<Long>

  @Update(onConflict = OnConflictStrategy.REPLACE)
  suspend fun update(items: List<T>)

  @Delete
  suspend fun delete(items: List<T>)
}
