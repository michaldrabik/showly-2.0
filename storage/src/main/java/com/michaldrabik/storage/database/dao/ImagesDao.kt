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

  @Query("SELECT * FROM shows_images WHERE id_tvdb = :tvdbId AND type = :type AND family = 'show'")
  suspend fun getByShowId(tvdbId: Long, type: String): Image?

  @Query("SELECT * FROM shows_images WHERE id_tvdb = :tvdbId AND type = :type AND family = 'episode'")
  suspend fun getByEpisodeId(tvdbId: Long, type: String): Image?

  @Transaction
  suspend fun insertShowImage(image: Image) {
    val localImage = getByShowId(image.idTvdb, image.type)
    if (localImage != null) {
      val updated = image.copy(id = localImage.id)
      upsert(updated)
      return
    }
    upsert(image)
  }

  @Transaction
  suspend fun insertEpisodeImage(image: Image) {
    val localImage = getByEpisodeId(image.idTvdb, image.type)
    if (localImage != null) {
      val updated = image.copy(id = localImage.id)
      upsert(updated)
      return
    }
    upsert(image)
  }

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(image: Image)

  @Query("DELETE FROM shows_images WHERE id_tvdb = :id AND type = :type AND family = 'show'")
  suspend fun deleteByShowId(id: Long, type: String)

  @Query("DELETE FROM shows_images WHERE id_tvdb = :id AND type = :type AND family = 'episode'")
  suspend fun deleteByEpisodeId(id: Long, type: String)
}