package com.michaldrabik.data_local.database.dao

/* ktlint-disable */
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.PersonImage
import com.michaldrabik.data_local.sources.PeopleImagesLocalDataSource

@Dao
interface PeopleImagesDao : BaseDao<PersonImage>, PeopleImagesLocalDataSource {

  @Query("SELECT updated_at FROM people_images WHERE id_tmdb = :personTmdbId LIMIT 1")
  override suspend fun getTimestampForPerson(personTmdbId: Long): Long?

  @Query("SELECT * FROM people_images WHERE id_tmdb = :personTmdbId")
  override suspend fun getAll(personTmdbId: Long): List<PersonImage>

  @Query("DELETE FROM people_images WHERE id_tmdb == :personTmdbId")
  override suspend fun deleteAllForPerson(personTmdbId: Long)

  @Transaction
  override suspend fun insert(personTmdbId: Long, images: List<PersonImage>) {
    deleteAllForPerson(personTmdbId)
    insert(images)
  }
}
