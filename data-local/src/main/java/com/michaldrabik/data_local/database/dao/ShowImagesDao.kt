package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.ShowImage

@Dao
interface ShowImagesDao {

  @Query("SELECT * FROM shows_images WHERE id_tmdb = :tmdbId AND type = :type AND family = 'show'")
  suspend fun getByShowId(tmdbId: Long, type: String): ShowImage?

  @Query("SELECT * FROM shows_images WHERE id_tmdb = :tmdbId AND type = :type AND family = 'episode'")
  suspend fun getByEpisodeId(tmdbId: Long, type: String): ShowImage?

  @Transaction
  suspend fun insertShowImage(image: ShowImage) {
    val localImage = getByShowId(image.idTmdb, image.type)
    if (localImage != null) {
      val updated = image.copy(id = localImage.id)
      upsert(updated)
      return
    }
    upsert(image)
  }

  @Transaction
  suspend fun insertEpisodeImage(image: ShowImage) {
    val localImage = getByEpisodeId(image.idTmdb, image.type)
    if (localImage != null) {
      val updated = image.copy(id = localImage.id)
      upsert(updated)
      return
    }
    upsert(image)
  }

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(image: ShowImage)

  @Query("DELETE FROM shows_images WHERE id_tmdb = :id AND type = :type AND family = 'show'")
  suspend fun deleteByShowId(id: Long, type: String)

  @Query("DELETE FROM shows_images WHERE id_tmdb = :id AND type = :type AND family = 'episode'")
  suspend fun deleteByEpisodeId(id: Long, type: String)

  @Query("DELETE FROM shows_images WHERE type = 'poster'")
  suspend fun deleteAll()
}
