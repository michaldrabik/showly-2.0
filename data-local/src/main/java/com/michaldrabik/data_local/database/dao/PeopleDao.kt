package com.michaldrabik.data_local.database.dao

/* ktlint-disable */
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.Person

@Dao
interface PeopleDao : BaseDao<Person> {

  @Transaction
  suspend fun upsert(people: List<Person>) {
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
  suspend fun getById(tmdbId: Long): Person?

  @Query("SELECT people.*, people_shows_movies.type AS type, people_shows_movies.character AS character FROM people INNER JOIN people_shows_movies ON people_shows_movies.id_tmdb_person = people.id_tmdb WHERE people_shows_movies.id_trakt_show = :showTraktId")
  suspend fun getAllForShow(showTraktId: Long): List<Person>

  @Query("SELECT people.*, people_shows_movies.type AS type, people_shows_movies.character AS character FROM people INNER JOIN people_shows_movies ON people_shows_movies.id_tmdb_person = people.id_tmdb WHERE people_shows_movies.id_trakt_movie = :movieTraktId")
  suspend fun getAllForMovie(movieTraktId: Long): List<Person>

  @Query("SELECT * FROM people")
  suspend fun getAll(): List<Person>
}
