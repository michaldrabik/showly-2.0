package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.PersonImage

interface PeopleImagesLocalDataSource {

  suspend fun getTimestampForPerson(personTmdbId: Long): Long?

  suspend fun getAll(personTmdbId: Long): List<PersonImage>

  suspend fun deleteAllForPerson(personTmdbId: Long)

  suspend fun insert(personTmdbId: Long, images: List<PersonImage>)
}
