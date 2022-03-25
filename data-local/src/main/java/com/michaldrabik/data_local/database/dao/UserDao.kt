package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.User
import com.michaldrabik.data_local.sources.UserLocalDataSource

@Dao
interface UserDao : UserLocalDataSource {

  @Query("SELECT * FROM user WHERE id == 1")
  override suspend fun get(): User?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun upsert(user: User)
}
