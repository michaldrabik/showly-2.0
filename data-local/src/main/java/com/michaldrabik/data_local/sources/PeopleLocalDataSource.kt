package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.Person

interface PeopleLocalDataSource {

  suspend fun upsert(people: List<Person>)

  suspend fun getById(tmdbId: Long): Person?

  suspend fun getAllForShow(showTraktId: Long): List<Person>

  suspend fun getAllForMovie(movieTraktId: Long): List<Person>

  suspend fun getAll(): List<Person>

  suspend fun updateTraktId(idTrakt: Long, idTmdb: Long)

  suspend fun deleteTranslations()
}
