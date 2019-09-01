package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.storage.database.model.Image

@Dao
interface ImagesDao {

  @Query("SELECT * FROM shows_images")
  suspend fun getAll(): List<Image>

  @Query("SELECT * FROM shows_images WHERE id_tvdb = :tvdbId AND type = :type")
  suspend fun getById(tvdbId: Long, type: String): Image?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(image: Image)

  @Query("DELETE FROM shows_images WHERE id_tvdb = :id AND type = :type")
  suspend fun deleteById(id: Long, type: String)
}