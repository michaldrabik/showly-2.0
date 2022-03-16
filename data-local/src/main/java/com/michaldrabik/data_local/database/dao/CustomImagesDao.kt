package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.CustomImage
import com.michaldrabik.data_local.sources.CustomImagesLocalDataSource

@Dao
interface CustomImagesDao : CustomImagesLocalDataSource {

  @Query("SELECT * FROM custom_images WHERE id_trakt = :traktId AND type = :type AND family = :family")
  override suspend fun getById(traktId: Long, family: String, type: String): CustomImage?

  @Query("DELETE FROM custom_images WHERE id_trakt = :traktId AND type = :type AND family = :family")
  override suspend fun deleteById(traktId: Long, family: String, type: String)

  @Transaction
  override suspend fun insertImage(image: CustomImage) {
    val localImage = getById(image.idTrakt, image.family, image.type)
    if (localImage != null) {
      val updated = image.copy(id = localImage.id)
      upsert(updated)
      return
    }
    upsert(image)
  }

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun upsert(image: CustomImage)

  @Query("DELETE FROM custom_images")
  override suspend fun deleteAll()
}
