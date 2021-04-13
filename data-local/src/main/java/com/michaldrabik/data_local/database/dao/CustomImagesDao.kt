package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.CustomImage

@Dao
interface CustomImagesDao {

  @Query("SELECT * FROM custom_images WHERE id_trakt = :traktId AND type = :type AND family = :family")
  suspend fun getById(traktId: Long, family: String, type: String): CustomImage?

  @Query("DELETE FROM custom_images WHERE id_trakt = :traktId AND type = :type AND family = :family")
  suspend fun deleteById(traktId: Long, family: String, type: String)

  @Transaction
  suspend fun insertImage(image: CustomImage) {
    val localImage = getById(image.idTrakt, image.family, image.type)
    if (localImage != null) {
      val updated = image.copy(id = localImage.id)
      upsert(updated)
      return
    }
    upsert(image)
  }

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(image: CustomImage)

  @Query("DELETE FROM custom_images")
  suspend fun deleteAll()
}
