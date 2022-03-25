package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.database.model.PersonCredits
import com.michaldrabik.data_local.database.model.Show

interface PeopleCreditsLocalDataSource {

  suspend fun getAllShowsForPerson(personTraktId: Long): List<Show>

  suspend fun getAllMoviesForPerson(personTraktId: Long): List<Movie>

  suspend fun getTimestampForPerson(personTraktId: Long): Long?

  suspend fun deleteAllForPerson(personTraktId: Long)

  suspend fun insert(personTraktId: Long, credits: List<PersonCredits>)
}
