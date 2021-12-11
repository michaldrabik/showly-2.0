package com.michaldrabik.repository

import androidx.room.withTransaction
import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.PersonCredits
import com.michaldrabik.data_local.database.model.PersonImage
import com.michaldrabik.data_local.database.model.PersonShowMovie
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageSource
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
  private val database: AppDatabase,
  private val cloud: Cloud,
  private val mappers: Mappers
) {

  companion object {
    const val ACTORS_DISPLAY_LIMIT = 30
    const val CREW_DISPLAY_LIMIT = 20
  }

  suspend fun loadDetails(person: Person): Person {
    val local = database.peopleDao().getById(person.ids.tmdb.id)
    if (local?.detailsUpdatedAt != null) {
      return mappers.person.fromDatabase(local, person.characters)
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

    // Return locally cached data if available
    val timestamp = database.peopleCreditsDao().getTimestampForPerson(idTrakt!!)
    if (timestamp != null && nowUtcMillis() - timestamp < Config.PEOPLE_CREDITS_CACHE_DURATION) {
      val localCredits = mutableListOf<PersonCredit>()

      val showsCreditsAsync = async { database.peopleCreditsDao().getAllShowsForPerson(idTrakt!!) }
      val moviesCreditsAsync = async { database.peopleCreditsDao().getAllMoviesForPerson(idTrakt!!) }
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
    val showsCreditsAsync = async { cloud.traktApi.fetchPersonShowsCredits(idTrakt!!, type) }
    val moviesCreditsAsync = async { cloud.traktApi.fetchPersonMoviesCredits(idTrakt!!, type) }
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

    with(database) {
      val shows = remoteCredits.filter { it.show != null }.map { it.show!! }
      val movies = remoteCredits.filter { it.movie != null }.map { it.movie!! }

      withTransaction {
        showsDao().upsert(shows.map { mappers.show.toDatabase(it) })
        moviesDao().upsert(movies.map { mappers.movie.toDatabase(it) })
        peopleCreditsDao().insert(idTrakt!!, localCredits)
      }
    }

    return@coroutineScope remoteCredits
  }

  suspend fun loadDefaultImage(personTmdbId: IdTmdb): Image? {
    val localPerson = database.peopleDao().getById(personTmdbId.id)
    return localPerson?.image?.let {
      return Image.createAvailable(
        ids = Ids.EMPTY,
        type = ImageType.PROFILE,
        family = ImageFamily.PROFILE,
        path = it,
        source = ImageSource.TMDB
      )
    }
  }

  suspend fun loadImages(personTmdbId: IdTmdb): List<Image> {
    val localTimestamp = database.peopleImagesDao().getTimestampForPerson(personTmdbId.id) ?: 0
    if (localTimestamp + Config.PEOPLE_IMAGES_CACHE_DURATION > nowUtcMillis()) {
      Timber.d("Returning cached result. Cache still valid for ${(localTimestamp + Config.PEOPLE_IMAGES_CACHE_DURATION) - nowUtcMillis()} ms")
      val local = database.peopleImagesDao().getAll(personTmdbId.id)
      return local.map {
        Image.createAvailable(
          ids = Ids.EMPTY,
          type = ImageType.PROFILE,
          family = ImageFamily.PROFILE,
          path = it.filePath,
          source = ImageSource.TMDB
        )
      }
    }

    val images = (cloud.tmdbApi.fetchPersonImages(personTmdbId.id).profiles ?: emptyList())
      .filter { it.file_path.isNotBlank() }
    val dbImages = images.map {
      PersonImage(
        id = 0,
        idTmdb = personTmdbId.id,
        filePath = it.file_path,
        createdAt = nowUtc(),
        updatedAt = nowUtc()
      )
    }

    with(database) {
      withTransaction {
        peopleImagesDao().insert(personTmdbId.id, dbImages)
      }
    }

    return images.map {
      Image.createAvailable(
        ids = Ids.EMPTY,
        type = ImageType.PROFILE,
        family = ImageFamily.PROFILE,
        path = it.file_path,
        source = ImageSource.TMDB
      )
    }
  }

  suspend fun loadAllForShow(showIds: Ids): Map<Department, List<Person>> {
    val timestamp = nowUtc()

    val localTimestamp = database.peopleShowsMoviesDao().getTimestampForShow(showIds.trakt.id) ?: 0
    val local = database.peopleDao().getAllForShow(showIds.trakt.id)
    if (local.isNotEmpty() && localTimestamp + Config.ACTORS_CACHE_DURATION > timestamp.toMillis()) {
      Timber.d("Returning cached result. Cache still valid for ${(localTimestamp + Config.ACTORS_CACHE_DURATION) - timestamp.toMillis()} ms")
      return local
        .sortedWith(compareBy { it.image.isNullOrBlank() })
        .map { mappers.person.fromDatabase(it) }
        .groupBy { it.department }
    }

    val remoteTmdbPeople = cloud.tmdbApi.fetchShowPeople(showIds.tmdb.id)

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

    val directors = remoteTmdbCrew[Department.DIRECTING]?.take(CREW_DISPLAY_LIMIT) ?: emptyList()
    val writers = remoteTmdbCrew[Department.WRITING]?.take(CREW_DISPLAY_LIMIT) ?: emptyList()
    val sound = remoteTmdbCrew[Department.SOUND]?.take(CREW_DISPLAY_LIMIT) ?: emptyList()

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

    with(database) {
      withTransaction {
        peopleDao().upsert(dbTmdbPeople)
        peopleShowsMoviesDao().insertForShow(dbTmdbPeopleShows, showIds.trakt.id)
      }
    }

    Timber.d("Returning remote result.")
    return filteredTmdbPeople.groupBy { it.department }
  }

  suspend fun loadAllForMovie(movieIds: Ids): Map<Department, List<Person>> {
    val timestamp = nowUtc()

    val localTimestamp = database.peopleShowsMoviesDao().getTimestampForMovie(movieIds.trakt.id) ?: 0
    val local = database.peopleDao().getAllForMovie(movieIds.trakt.id)
    if (local.isNotEmpty() && localTimestamp + Config.ACTORS_CACHE_DURATION > timestamp.toMillis()) {
      Timber.d("Returning cached result. Cache still valid for ${(localTimestamp + Config.ACTORS_CACHE_DURATION) - timestamp.toMillis()} ms")
      return local
        .sortedWith(compareBy { it.image.isNullOrBlank() })
        .map { mappers.person.fromDatabase(it) }
        .groupBy { it.department }
    }

    val remoteTmdbPeople = cloud.tmdbApi.fetchMoviePeople(movieIds.tmdb.id)

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

    val directors = remoteTmdbCrew[Department.DIRECTING]?.take(CREW_DISPLAY_LIMIT) ?: emptyList()
    val writers = remoteTmdbCrew[Department.WRITING]?.take(CREW_DISPLAY_LIMIT) ?: emptyList()
    val sound = remoteTmdbCrew[Department.SOUND]?.take(CREW_DISPLAY_LIMIT) ?: emptyList()

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

    with(database) {
      withTransaction {
        peopleDao().upsert(dbTmdbPeople)
        peopleShowsMoviesDao().insertForMovie(dbTmdbPeopleMovies, movieIds.trakt.id)
      }
    }

    Timber.d("Returning remote result.")
    return filteredTmdbPeople.groupBy { it.department }
  }
}
