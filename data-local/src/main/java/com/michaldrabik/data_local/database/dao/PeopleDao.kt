package com.michaldrabik.data_local.database.dao

/* ktlint-disable */
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.Person
import com.michaldrabik.data_local.sources.PeopleLocalDataSource

@Dao
interface PeopleDao : BaseDao<Person>, PeopleLocalDataSource {

  @Transaction
  override suspend fun upsert(people: List<Person>) {
    val result = insert(people)
    val updateList = mutableListOf<Person>()
    result.forEachIndexed { index, id ->
      if (id == -1L) {
        updateList.add(people[index])
      }
    }
    if (updateList.isNotEmpty()) update(updateList)
  }

  @Query("SELECT * FROM people WHERE id_tmdb = :tmdbId")
  override suspend fun getById(tmdbId: Long): Person?

  @Query("SELECT people.*, people_shows_movies.department AS department, people_shows_movies.character AS character, people_shows_movies.job AS job, people_shows_movies.episodes_count AS episodes_count FROM people INNER JOIN people_shows_movies ON people_shows_movies.id_tmdb_person = people.id_tmdb WHERE people_shows_movies.id_trakt_show = :showTraktId")
  override suspend fun getAllForShow(showTraktId: Long): List<Person>

  @Query("SELECT people.*, people_shows_movies.department AS department, people_shows_movies.character AS character, people_shows_movies.job AS job, people_shows_movies.episodes_count AS episodes_count FROM people INNER JOIN people_shows_movies ON people_shows_movies.id_tmdb_person = people.id_tmdb WHERE people_shows_movies.id_trakt_movie = :movieTraktId")
  override suspend fun getAllForMovie(movieTraktId: Long): List<Person>

  @Query("SELECT * FROM people")
  override suspend fun getAll(): List<Person>

  @Query("UPDATE people SET id_trakt = :idTrakt WHERE id_tmdb = :idTmdb")
  override suspend fun updateTraktId(idTrakt: Long, idTmdb: Long)

  @Query("UPDATE people SET biography_translation = NULL, details_updated_at = NULL")
  override suspend fun deleteTranslations()
}
