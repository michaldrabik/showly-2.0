package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.PersonShowMovie

@Dao
interface PeopleShowsMoviesDao : BaseDao<PersonShowMovie> {

  @Query("SELECT updated_at FROM people_shows_movies WHERE id_trakt_show == :showTraktId LIMIT 1")
  suspend fun getTimestampForShow(showTraktId: Long): Long?

  @Query("SELECT updated_at FROM people_shows_movies WHERE id_trakt_movie == :movieTraktId LIMIT 1")
  suspend fun getTimestampForMovie(movieTraktId: Long): Long?

  @Query("DELETE FROM people_shows_movies WHERE id_trakt_show == :showTraktId")
  suspend fun deleteAllForShow(showTraktId: Long)

  @Query("DELETE FROM people_shows_movies WHERE id_trakt_movie == :movieTraktId")
  suspend fun deleteAllForMovie(movieTraktId: Long)

  @Transaction
  suspend fun insertForShow(people: List<PersonShowMovie>, showTraktId: Long) {
    deleteAllForShow(showTraktId)
    insert(people)
  }

  @Transaction
  suspend fun insertForMovie(people: List<PersonShowMovie>, movieTraktId: Long) {
    deleteAllForMovie(movieTraktId)
    insert(people)
  }
}
