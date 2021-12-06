package com.michaldrabik.data_local.database.dao

/* ktlint-disable */
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.PersonImage

@Dao
interface PeopleImagesDao : BaseDao<PersonImage> {

  @Query("SELECT updated_at FROM people_images WHERE id_tmdb = :personTmdbId LIMIT 1")
  suspend fun getTimestampForPerson(personTmdbId: Long): Long?

  @Query("SELECT * FROM people_images WHERE id_tmdb = :personTmdbId")
  suspend fun getAll(personTmdbId: Long): List<PersonImage>

  @Query("DELETE FROM people_images WHERE id_tmdb == :personTmdbId")
  suspend fun deleteAllForPerson(personTmdbId: Long)

  @Transaction
  suspend fun insert(personTmdbId: Long, images: List<PersonImage>) {
    deleteAllForPerson(personTmdbId)
    insert(images)
  }
}
