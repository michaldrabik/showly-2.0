package com.michaldrabik.storage.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(seasons: List<T>): List<Long>

  @Update(onConflict = OnConflictStrategy.REPLACE)
  suspend fun update(seasons: List<T>)

  @Delete
  suspend fun delete(seasons: List<T>)
}