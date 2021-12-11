package com.michaldrabik.repository.mappers

import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Person
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Person as PersonDb

class PersonMapper @Inject constructor() {

  fun fromNetwork(person: TmdbPerson) =
    Person(
      ids = Ids.EMPTY.copy(
        tmdb = IdTmdb(person.id),
        imdb = IdImdb(person.imdb_id ?: "")
      ),
      name = person.name ?: "",
      department = typeToEnum(person.department ?: person.known_for_department),
      bio = person.biography,
      bioTranslation = null,
      birthplace = person.place_of_birth,
      imagePath = person.profile_path,
      homepage = person.homepage,
      characters = extractCharacters(person),
      jobs = extractJobs(person),
      episodesCount = person.total_episode_count ?: 0,
      birthday = person.birthday?.let { if (it.isNotBlank()) LocalDate.parse(it) else null },
      deathday = person.deathday?.let { if (it.isNotBlank()) LocalDate.parse(it) else null }
    )

  fun fromDatabase(personDb: PersonDb, characters: List<String> = emptyList()) =
    Person(
      ids = Ids.EMPTY.copy(
        trakt = IdTrakt(personDb.idTrakt ?: -1),
        tmdb = IdTmdb(personDb.idTmdb),
        imdb = IdImdb(personDb.idImdb ?: "")
      ),
      name = personDb.name,
      department = typeToEnum(personDb.department),
      bio = personDb.biography,
      bioTranslation = personDb.biographyTranslation,
      characters = if (characters.isNotEmpty()) characters else personDb.character?.split(",") ?: emptyList(),
      jobs = personDb.job?.split(",")?.map { Person.Job.fromSlug(it) } ?: emptyList(),
      episodesCount = personDb.episodesCount ?: 0,
      birthplace = personDb.birthplace,
      imagePath = personDb.image,
      homepage = personDb.homepage,
      birthday = personDb.birthday?.let { if (it.isNotBlank()) LocalDate.parse(it) else null },
      deathday = personDb.deathday?.let { if (it.isNotBlank()) LocalDate.parse(it) else null }
    )

  fun toDatabase(person: Person, detailsTimestamp: ZonedDateTime?): PersonDb {
    val idTrakt = if (person.ids.trakt.id != -1L) person.ids.trakt.id else null
    val idImdb = if (person.ids.imdb.id.isNotBlank()) person.ids.imdb.id else null
    return PersonDb(
      idTmdb = person.ids.tmdb.id,
      idTrakt = idTrakt,
      idImdb = idImdb,
      name = person.name,
      department = person.department.slug,
      biography = person.bio,
      biographyTranslation = person.bioTranslation,
      character = person.characters.joinToString(","),
      job = person.jobs.joinToString(",") { it.slug },
      episodesCount = person.episodesCount,
      birthday = person.birthday?.format(ISO_LOCAL_DATE),
      birthplace = person.birthplace,
      deathday = person.deathday?.format(ISO_LOCAL_DATE),
      image = person.imagePath,
      homepage = person.homepage,
      createdAt = nowUtc(),
      updatedAt = nowUtc(),
      detailsUpdatedAt = detailsTimestamp
    )
  }

  private fun extractCharacters(person: TmdbPerson) = when {
    person.roles != null -> person.roles?.mapNotNull { it.character } ?: emptyList()
    !person.character.isNullOrBlank() -> listOf(person.character!!)
    else -> emptyList()
  }

  private fun extractJobs(person: TmdbPerson) = when {
    person.jobs != null -> person.jobs?.map { Person.Job.fromSlug(it.job) } ?: emptyList()
    !person.job.isNullOrBlank() -> listOf(Person.Job.fromSlug(person.job))
    else -> emptyList()
  }

  private fun typeToEnum(type: String?) = when (type) {
    "Acting", "Actors" -> Person.Department.ACTING
    "Directing" -> Person.Department.DIRECTING
    "Writing" -> Person.Department.WRITING
    "Sound" -> Person.Department.SOUND
    else -> Person.Department.UNKNOWN
  }
}
