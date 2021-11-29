package com.michaldrabik.repository

import androidx.room.withTransaction
import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.PersonShowMovie
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.PersonCredit
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

class PeopleRepository @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val database: AppDatabase,
  private val cloud: Cloud,
  private val mappers: Mappers
) {

  suspend fun loadDetails(person: Person): Person {
    val local = database.peopleDao().getById(person.ids.tmdb.id)
    if (local?.detailsUpdatedAt != null) {
      return mappers.person.fromDatabase(local, person.character)
    }

    val language = settingsRepository.language
    val remotePerson = cloud.tmdbApi.fetchPersonDetails(person.ids.tmdb.id)
    var bioTranslation: String? = null
    if (language != Config.DEFAULT_LANGUAGE) {
      val translations = cloud.tmdbApi.fetchPersonTranslations(person.ids.tmdb.id)
      bioTranslation = translations[language]?.biography
    }

    val personUi = mappers.person.fromNetwork(remotePerson).copy(
      bioTranslation = bioTranslation,
      imagePath = person.imagePath ?: remotePerson.profile_path
    )
    val dbPerson = mappers.person.toDatabase(personUi, nowUtc())

    database.peopleDao().upsert(listOf(dbPerson))

    return personUi
  }

  suspend fun loadCredits(person: Person) = coroutineScope {
    val idTmdb = person.ids.tmdb.id
    var idTrakt: Long?

    val localPerson = database.peopleDao().getById(idTmdb)
    idTrakt = localPerson?.idTrakt
    if (idTrakt == null) {
      val ids = cloud.traktApi.fetchPersonIds("tmdb", idTmdb.toString())
      ids?.trakt?.let {
        idTrakt = it
        database.peopleDao().updateTraktId(it, idTmdb)
      }
    }
    if (idTrakt == null) return@coroutineScope emptyList()

    val showsCreditsAsync = async { cloud.traktApi.fetchPersonShowsCredits(idTrakt!!) }
    val moviesCreditsAsync = async { cloud.traktApi.fetchPersonMoviesCredits(idTrakt!!) }
    val remoteCredits = awaitAll(showsCreditsAsync, moviesCreditsAsync)
      .flatten()
      .map {
        PersonCredit(
          show = it.show?.let { show -> mappers.show.fromNetwork(show) },
          movie = it.movie?.let { movie -> mappers.movie.fromNetwork(movie) },
          image = Image.createUnknown(ImageType.POSTER)
        )
      }

    return@coroutineScope remoteCredits
  }

  suspend fun loadAllForShow(showIds: Ids): List<Person> {
    val timestamp = nowUtc()

    val localTimestamp = database.peopleShowsMoviesDao().getTimestampForShow(showIds.trakt.id) ?: 0
    val local = database.peopleDao().getAllForShow(showIds.trakt.id)
    if (local.isNotEmpty() && localTimestamp + Config.ACTORS_CACHE_DURATION > timestamp.toMillis()) {
      Timber.d("Returning cached result. Cache still valid for ${(localTimestamp + Config.ACTORS_CACHE_DURATION) - timestamp.toMillis()} ms")
      return local
        .sortedWith(compareBy { it.image.isNullOrBlank() })
        .map { mappers.person.fromDatabase(it) }
    }

    val remoteTmdbActors = cloud.tmdbApi.fetchShowActors(showIds.tmdb.id)
      .sortedWith(compareBy { it.profile_path.isNullOrBlank() })
      .map { mappers.person.fromNetwork(it) }

    val dbTmdbActors = remoteTmdbActors.map { mappers.person.toDatabase(it, null) }
    val dbTmdbActorsShows = remoteTmdbActors.map {
      PersonShowMovie(
        id = 0,
        idTmdbPerson = it.ids.tmdb.id,
        mode = Mode.SHOWS.type,
        type = Person.Type.ACTING.slug,
        character = it.character,
        idTraktShow = showIds.trakt.id,
        idTraktMovie = null,
        createdAt = timestamp,
        updatedAt = timestamp
      )
    }

    with(database) {
      withTransaction {
        peopleDao().upsert(dbTmdbActors)
        peopleShowsMoviesDao().insertForShow(dbTmdbActorsShows, showIds.trakt.id)
      }
    }

    Timber.d("Returning remote result.")
    return remoteTmdbActors
  }

  suspend fun loadAllForMovie(movieIds: Ids): List<Person> {
    val timestamp = nowUtc()

    val localTimestamp = database.peopleShowsMoviesDao().getTimestampForMovie(movieIds.trakt.id) ?: 0
    val local = database.peopleDao().getAllForMovie(movieIds.trakt.id)
    if (local.isNotEmpty() && localTimestamp + Config.ACTORS_CACHE_DURATION > timestamp.toMillis()) {
      Timber.d("Returning cached result. Cache still valid for ${(localTimestamp + Config.ACTORS_CACHE_DURATION) - timestamp.toMillis()} ms")
      return local
        .sortedWith(compareBy { it.image.isNullOrBlank() })
        .map { mappers.person.fromDatabase(it) }
    }

    val remoteTmdbActors = cloud.tmdbApi.fetchMovieActors(movieIds.tmdb.id)
      .sortedWith(compareBy { it.profile_path.isNullOrBlank() })
      .map { mappers.person.fromNetwork(it) }

    val dbTmdbActors = remoteTmdbActors.map { mappers.person.toDatabase(it, null) }
    val dbTmdbActorsShows = remoteTmdbActors.map {
      PersonShowMovie(
        id = 0,
        idTmdbPerson = it.ids.tmdb.id,
        mode = Mode.MOVIES.type,
        type = Person.Type.ACTING.slug,
        character = it.character,
        idTraktShow = null,
        idTraktMovie = movieIds.trakt.id,
        createdAt = timestamp,
        updatedAt = timestamp
      )
    }

    with(database) {
      withTransaction {
        peopleDao().upsert(dbTmdbActors)
        peopleShowsMoviesDao().insertForMovie(dbTmdbActorsShows, movieIds.trakt.id)
      }
    }

    Timber.d("Returning remote result.")
    return remoteTmdbActors
  }
}
