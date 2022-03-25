package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.MovieImage
import com.michaldrabik.data_local.sources.MovieImagesLocalDataSource

@Dao
interface MovieImagesDao : MovieImagesLocalDataSource {

  @Query("SELECT * FROM movies_images WHERE id_tmdb = :tmdbId AND type = :type")
  override suspend fun getByMovieId(tmdbId: Long, type: String): MovieImage?

  @Transaction
  override suspend fun insertMovieImage(image: MovieImage) {
    val localImage = getByMovieId(image.idTmdb, image.type)
    if (localImage != null) {
      val updated = image.copy(id = localImage.id)
      upsert(updated)
      return
    }
    upsert(image)
  }

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun upsert(image: MovieImage)

  @Query("DELETE FROM movies_images WHERE id_tmdb = :id AND type = :type")
  override suspend fun deleteByMovieId(id: Long, type: String)

  @Query("DELETE FROM movies_images WHERE type = 'poster'")
  override suspend fun deleteAll()
}
