package com.michaldrabik.repository

import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.PersonCredits
import com.michaldrabik.data_local.database.model.PersonShowMovie
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Person.Department
import com.michaldrabik.ui_model.PersonCredit
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

class PeopleRepository @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val localSource: LocalDataSource,
  private val remoteSource: RemoteDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers
) {

  companion object {
    const val ACTORS_DISPLAY_LIMIT = 30
    const val CREW_DISPLAY_LIMIT = 20
  }

  suspend fun loadDetails(person: Person): Person {
    val local = localSource.people.getById(person.ids.tmdb.id)
    if (local?.detailsUpdatedAt != null) {
      return mappers.person.fromDatabase(local, person.characters)
    }

    val language = settingsRepository.language
    val remotePerson = remoteSource.tmdb.fetchPersonDetails(person.ids.tmdb.id)
    var bioTranslation: String? = null
    if (language != Config.DEFAULT_LANGUAGE) {
      val translations = remoteSource.tmdb.fetchPersonTranslations(person.ids.tmdb.id)
      bioTranslation = translations[language]?.biography
    }

    val personUi = mappers.person.fromNetwork(remotePerson).copy(
      bioTranslation = bioTranslation,
      imagePath = person.imagePath ?: remotePerson.profile_path
    )
    val dbPerson = mappers.person.toDatabase(personUi, nowUtc())

    localSource.people.upsert(listOf(dbPerson))

    return personUi
  }

  suspend fun loadCredits(person: Person) = coroutineScope {
    val idTmdb = person.ids.tmdb.id
    var idTrakt: Long?

    val localPerson = localSource.people.getById(idTmdb)
    idTrakt = localPerson?.idTrakt
    if (idTrakt == null) {
      val ids = remoteSource.trakt.fetchPersonIds("tmdb", idTmdb.toString())
      ids?.trakt?.let {
        idTrakt = it
        localSource.people.updateTraktId(it, idTmdb)
      }
    }
    if (idTrakt == null) return@coroutineScope emptyList()

    // Return locally cached data if available
    val timestamp = localSource.peopleCredits.getTimestampForPerson(idTrakt!!)
    if (timestamp != null && nowUtcMillis() - timestamp < Config.PEOPLE_CREDITS_CACHE_DURATION) {
      val localCredits = mutableListOf<PersonCredit>()

      val showsCreditsAsync = async { localSource.peopleCredits.getAllShowsForPerson(idTrakt!!) }
      val moviesCreditsAsync = async { localSource.peopleCredits.getAllMoviesForPerson(idTrakt!!) }
      val shows = showsCreditsAsync.await()
      val movies = moviesCreditsAsync.await()

      shows.mapTo(localCredits) {
        PersonCredit(
          show = mappers.show.fromDatabase(it),
          movie = null,
          image = Image.createUnknown(ImageType.POSTER),
          translation = null
        )
      }
      movies.mapTo(localCredits) {
        PersonCredit(
          movie = mappers.movie.fromDatabase(it),
          show = null,
          image = Image.createUnknown(ImageType.POSTER),
          translation = null
        )
      }

      return@coroutineScope localCredits
    }

    // Return remote fetched data if available and cache it locally
    val type = if (person.department == Department.ACTING) TmdbPerson.Type.CAST else TmdbPerson.Type.CREW
    val showsCreditsAsync = async { remoteSource.trakt.fetchPersonShowsCredits(idTrakt!!, type) }
    val moviesCreditsAsync = async { remoteSource.trakt.fetchPersonMoviesCredits(idTrakt!!, type) }
    val remoteCredits = awaitAll(showsCreditsAsync, moviesCreditsAsync)
      .flatten()
      .map {
        PersonCredit(
          show = it.show?.let { show -> mappers.show.fromNetwork(show) },
          movie = it.movie?.let { movie -> mappers.movie.fromNetwork(movie) },
          image = Image.createUnknown(ImageType.POSTER),
          translation = null
        )
      }

    val localCredits = remoteCredits.map {
      PersonCredits(
        id = 0,
        idTraktPerson = idTrakt!!,
        idTraktShow = it.show?.traktId,
        idTraktMovie = it.movie?.traktId,
        type = if (it.show != null) Mode.SHOWS.type else Mode.MOVIES.type,
        createdAt = nowUtc(),
        updatedAt = nowUtc()
      )
    }

    with(localSource) {
      val remoteShows = remoteCredits.filter { it.show != null }.map { it.show!! }
      val remoteMovies = remoteCredits.filter { it.movie != null }.map { it.movie!! }

      transactions.withTransaction {
        shows.upsert(remoteShows.map { mappers.show.toDatabase(it) })
        movies.upsert(remoteMovies.map { mappers.movie.toDatabase(it) })
        peopleCredits.insert(idTrakt!!, localCredits)
      }
    }

    return@coroutineScope remoteCredits
  }

  suspend fun loadAllForShow(showIds: Ids): Map<Department, List<Person>> {
    val timestamp = nowUtc()

    val localTimestamp = localSource.peopleShowsMovies.getTimestampForShow(showIds.trakt.id) ?: 0
    val local = localSource.people.getAllForShow(showIds.trakt.id)
    if (local.isNotEmpty() && localTimestamp + Config.ACTORS_CACHE_DURATION > timestamp.toMillis()) {
      Timber.d("Returning cached result. Cache still valid for ${(localTimestamp + Config.ACTORS_CACHE_DURATION) - timestamp.toMillis()} ms")
      return local
        .map { mappers.person.fromDatabase(it) }
        .groupBy { it.department }
        .mapValues { v -> v.value.sortedWith(compareBy { it.imagePath.isNullOrBlank() }) }
    }

    val remoteTmdbPeople = remoteSource.tmdb.fetchShowPeople(showIds.tmdb.id)

    val remoteTmdbActors = remoteTmdbPeople
      .getOrDefault(TmdbPerson.Type.CAST, emptyList())
      .sortedWith(compareBy { it.profile_path.isNullOrBlank() })
      .map { mappers.person.fromNetwork(it) }
      .take(ACTORS_DISPLAY_LIMIT)

    val crewFilter = arrayOf(Department.DIRECTING, Department.WRITING, Department.SOUND).map { it.slug }
    val jobsFilter = Person.Job.values().map { it.slug }
    val remoteTmdbCrew = remoteTmdbPeople
      .getOrDefault(TmdbPerson.Type.CREW, emptyList())
      .asSequence()
      .filter { it.department in crewFilter }
      .filter { it.jobs?.any { job -> job.job ?: "" in jobsFilter } == true }
      .sortedWith(compareBy { it.profile_path.isNullOrBlank() })
      .map { mappers.person.fromNetwork(it) }
      .groupBy { it.department }

    val directors = remoteTmdbCrew[Department.DIRECTING]?.take(CREW_DISPLAY_LIMIT)?.distinctBy { it.ids.tmdb } ?: emptyList()
    val writers = remoteTmdbCrew[Department.WRITING]?.take(CREW_DISPLAY_LIMIT)?.distinctBy { it.ids.tmdb } ?: emptyList()
    val sound = remoteTmdbCrew[Department.SOUND]?.take(CREW_DISPLAY_LIMIT)?.distinctBy { it.ids.tmdb } ?: emptyList()

    val filteredTmdbPeople = remoteTmdbActors + directors + writers + sound
    val dbTmdbPeople = filteredTmdbPeople.map { mappers.person.toDatabase(it, null) }
    val dbTmdbPeopleShows = filteredTmdbPeople.map {
      PersonShowMovie(
        id = 0,
        idTmdbPerson = it.ids.tmdb.id,
        mode = Mode.SHOWS.type,
        department = it.department.slug,
        character = it.characters.joinToString(","),
        job = it.jobs.joinToString(",") { job -> job.slug },
        episodesCount = it.episodesCount,
        idTraktShow = showIds.trakt.id,
        idTraktMovie = null,
        createdAt = timestamp,
        updatedAt = timestamp
      )
    }

    with(localSource) {
      transactions.withTransaction {
        people.upsert(dbTmdbPeople)
        peopleShowsMovies.insertForShow(dbTmdbPeopleShows, showIds.trakt.id)
      }
    }

    Timber.d("Returning remote result.")
    return filteredTmdbPeople.groupBy { it.department }
  }

  suspend fun loadAllForMovie(movieIds: Ids): Map<Department, List<Person>> {
    val timestamp = nowUtc()

    val localTimestamp = localSource.peopleShowsMovies.getTimestampForMovie(movieIds.trakt.id) ?: 0
    val local = localSource.people.getAllForMovie(movieIds.trakt.id)
    if (local.isNotEmpty() && localTimestamp + Config.ACTORS_CACHE_DURATION > timestamp.toMillis()) {
      Timber.d("Returning cached result. Cache still valid for ${(localTimestamp + Config.ACTORS_CACHE_DURATION) - timestamp.toMillis()} ms")
      return local
        .map { mappers.person.fromDatabase(it) }
        .groupBy { it.department }
        .mapValues { v -> v.value.sortedWith(compareBy { it.imagePath.isNullOrBlank() }) }
    }

    val remoteTmdbPeople = remoteSource.tmdb.fetchMoviePeople(movieIds.tmdb.id)

    val remoteTmdbActors = remoteTmdbPeople
      .getOrDefault(TmdbPerson.Type.CAST, emptyList())
      .sortedWith(compareBy { it.profile_path.isNullOrBlank() })
      .map { mappers.person.fromNetwork(it) }
      .take(ACTORS_DISPLAY_LIMIT)

    val crewFilter = arrayOf(Department.DIRECTING, Department.WRITING, Department.SOUND).map { it.slug }
    val jobsFilter = Person.Job.values().map { it.slug }
    val remoteTmdbCrew = remoteTmdbPeople
      .getOrDefault(TmdbPerson.Type.CREW, emptyList())
      .asSequence()
      .filter { it.department in crewFilter }
      .filter { it.job in jobsFilter }
      .sortedWith(compareBy { it.profile_path.isNullOrBlank() })
      .map { mappers.person.fromNetwork(it) }
      .groupBy { it.department }

    val directors = remoteTmdbCrew[Department.DIRECTING]?.take(CREW_DISPLAY_LIMIT)?.distinctBy { it.ids.tmdb } ?: emptyList()
    val writers = remoteTmdbCrew[Department.WRITING]?.take(CREW_DISPLAY_LIMIT)?.distinctBy { it.ids.tmdb } ?: emptyList()
    val sound = remoteTmdbCrew[Department.SOUND]?.take(CREW_DISPLAY_LIMIT)?.distinctBy { it.ids.tmdb } ?: emptyList()

    val filteredTmdbPeople = remoteTmdbActors + directors + writers + sound
    val dbTmdbPeople = filteredTmdbPeople.map { mappers.person.toDatabase(it, null) }
    val dbTmdbPeopleMovies = filteredTmdbPeople.map {
      PersonShowMovie(
        id = 0,
        idTmdbPerson = it.ids.tmdb.id,
        mode = Mode.MOVIES.type,
        department = it.department.slug,
        character = it.characters.joinToString(","),
        job = it.jobs.joinToString(",") { job -> job.slug },
        episodesCount = it.episodesCount,
        idTraktShow = null,
        idTraktMovie = movieIds.trakt.id,
        createdAt = timestamp,
        updatedAt = timestamp
      )
    }

    with(localSource) {
      transactions.withTransaction {
        people.upsert(dbTmdbPeople)
        peopleShowsMovies.insertForMovie(dbTmdbPeopleMovies, movieIds.trakt.id)
      }
    }

    Timber.d("Returning remote result.")
    return filteredTmdbPeople.groupBy { it.department }
  }
}
