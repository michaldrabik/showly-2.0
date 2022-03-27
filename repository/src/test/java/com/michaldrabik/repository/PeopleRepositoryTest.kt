package com.michaldrabik.repository

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.database.dao.MoviesDao
import com.michaldrabik.data_local.database.dao.PeopleCreditsDao
import com.michaldrabik.data_local.database.dao.PeopleDao
import com.michaldrabik.data_local.database.dao.PeopleShowsMoviesDao
import com.michaldrabik.data_local.database.dao.ShowsDao
import com.michaldrabik.data_local.database.model.Movie
import com.michaldrabik.data_local.database.model.Show
import com.michaldrabik.data_remote.tmdb.TmdbRemoteDataSource
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
import com.michaldrabik.data_remote.trakt.model.PersonCredit
import com.michaldrabik.repository.common.BaseMockTest
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Person.Department
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.michaldrabik.data_local.database.model.Person as PersonDb
import com.michaldrabik.data_remote.trakt.model.Ids as IdsRemote

class PeopleRepositoryTest : BaseMockTest() {

  @RelaxedMockK lateinit var peopleDao: PeopleDao
  @RelaxedMockK lateinit var showsDao: ShowsDao
  @RelaxedMockK lateinit var moviesDao: MoviesDao
  @RelaxedMockK lateinit var peopleShowsMoviesDao: PeopleShowsMoviesDao
  @RelaxedMockK lateinit var peopleCreditsDao: PeopleCreditsDao
  @RelaxedMockK lateinit var person: PersonDb
  @RelaxedMockK lateinit var tmdbApi: TmdbRemoteDataSource
  @RelaxedMockK lateinit var traktApi: TraktRemoteDataSource
  @RelaxedMockK lateinit var settingsRepository: SettingsRepository

  private lateinit var SUT: PeopleRepository

  @Before
  override fun setUp() {
    super.setUp()
    SUT = PeopleRepository(settingsRepository, database, cloud, transactions, mappers)
    coEvery { database.people } returns peopleDao
    coEvery { database.shows } returns showsDao
    coEvery { database.movies } returns moviesDao
    coEvery { database.peopleCredits } returns peopleCreditsDao
    coEvery { database.peopleShowsMovies } returns peopleShowsMoviesDao
    coEvery { cloud.tmdb } returns tmdbApi
    coEvery { cloud.trakt } returns traktApi
  }

  @After
  fun confirmSutVerified() {
    confirmVerified(peopleDao)
  }

  @Test
  fun `Should return local data for shows properly`() = runBlocking {
    coEvery { peopleShowsMoviesDao.getTimestampForShow(any()) } returns nowUtc().minusHours(10).toMillis()
    coEvery { peopleDao.getAllForShow(any()) } returns listOf(person)

    SUT.loadAllForShow(Ids.EMPTY.copy(trakt = IdTrakt(11)))

    coVerifyOrder {
      peopleShowsMoviesDao.getTimestampForShow(11)
      peopleDao.getAllForShow(11)
    }
    coVerify(exactly = 0) { tmdbApi.fetchShowPeople(any()) }
  }

  @Test
  fun `Should return remote data for shows properly`() = runBlocking {
    coEvery { peopleShowsMoviesDao.getTimestampForShow(any()) } returns nowUtc().minusDays(10).toMillis()
    coEvery { peopleDao.getAllForShow(any()) } returns listOf(person)

    SUT.loadAllForShow(Ids.EMPTY.copy(trakt = IdTrakt(11), tmdb = IdTmdb(12)))

    coVerifyOrder {
      peopleShowsMoviesDao.getTimestampForShow(11)
      peopleDao.getAllForShow(11)
      tmdbApi.fetchShowPeople(12)
      peopleDao.upsert(any())
      peopleShowsMoviesDao.insertForShow(any(), 11)
    }
  }

  @Test
  fun `Should return local data for movies properly`() = runBlocking {
    coEvery { peopleShowsMoviesDao.getTimestampForMovie(any()) } returns nowUtc().minusHours(10).toMillis()
    coEvery { peopleDao.getAllForMovie(any()) } returns listOf(person)

    SUT.loadAllForMovie(Ids.EMPTY.copy(trakt = IdTrakt(11)))

    coVerifyOrder {
      peopleShowsMoviesDao.getTimestampForMovie(11)
      peopleDao.getAllForMovie(11)
    }
    coVerify(exactly = 0) { tmdbApi.fetchMoviePeople(any()) }
  }

  @Test
  fun `Should return remote data for movies properly`() = runBlocking {
    coEvery { peopleShowsMoviesDao.getTimestampForMovie(any()) } returns nowUtc().minusDays(10).toMillis()
    coEvery { peopleDao.getAllForMovie(any()) } returns listOf(person)

    SUT.loadAllForMovie(Ids.EMPTY.copy(trakt = IdTrakt(11), tmdb = IdTmdb(12)))

    coVerifyOrder {
      peopleShowsMoviesDao.getTimestampForMovie(11)
      peopleDao.getAllForMovie(11)
      tmdbApi.fetchMoviePeople(12)
      peopleDao.upsert(any())
      peopleShowsMoviesDao.insertForMovie(any(), 11)
    }
  }

  @Test
  fun `Should return shows items with image in the first place`() = runBlocking {
    coEvery { peopleShowsMoviesDao.getTimestampForShow(any()) } returns nowUtc().minusHours(10).toMillis()

    val person1 = mockk<PersonDb>(relaxed = true) {
      coEvery { image } returns null
      coEvery { department } returns "Acting"
    }
    val person2 = mockk<PersonDb>(relaxed = true) {
      coEvery { image } returns "test"
      coEvery { department } returns "Acting"
    }
    val person3 = mockk<PersonDb>(relaxed = true) {
      coEvery { image } returns "test"
      coEvery { department } returns "Acting"
    }
    coEvery { peopleDao.getAllForShow(any()) } returns listOf(person1, person2, person3)

    val result = SUT.loadAllForShow(Ids.EMPTY.copy(trakt = IdTrakt(11)))
    assertThat(result[Department.ACTING]!!.first().imagePath).isNotNull()

    coVerify { peopleDao.getAllForShow(any()) }
  }

  @Test
  fun `Should return movies items with image in the first place`() = runBlocking {
    coEvery { peopleShowsMoviesDao.getTimestampForMovie(any()) } returns nowUtc().minusHours(10).toMillis()

    val person1 = mockk<PersonDb>(relaxed = true) {
      coEvery { image } returns null
      coEvery { department } returns "Acting"
    }
    val person2 = mockk<PersonDb>(relaxed = true) {
      coEvery { image } returns "test"
      coEvery { department } returns "Acting"
    }
    val person3 = mockk<PersonDb>(relaxed = true) {
      coEvery { image } returns "test"
      coEvery { department } returns "Acting"
    }
    coEvery { peopleDao.getAllForMovie(any()) } returns listOf(person1, person2, person3)

    val result = SUT.loadAllForMovie(Ids.EMPTY.copy(trakt = IdTrakt(11)))
    assertThat(result[Department.ACTING]!!.first().imagePath).isNotNull()

    coVerify { peopleDao.getAllForMovie(any()) }
  }

  @Test
  fun `Should return empty credits if Trakt ID is not found for given TMDB ID`() = runBlocking {
    val person = mockk<Person>(relaxed = true)
    val personDb = mockk<PersonDb>(relaxed = true) {
      coEvery { idTrakt } returns null
    }
    val ids = mockk<IdsRemote>(relaxed = true) {
      coEvery { trakt } returns null
    }
    coEvery { peopleDao.getById(any()) } returns personDb
    coEvery { traktApi.fetchPersonIds(any(), any()) } returns ids

    val result = SUT.loadCredits(person)

    assertThat(result).isEmpty()
    coVerify { peopleDao.getById(any()) }
    coVerify(exactly = 0) { peopleDao.updateTraktId(any(), any()) }
  }

  @Test
  fun `Should return locally cached credits if Trakt ID is found and cache is valid`() = runBlocking {
    val person = mockk<Person>(relaxed = true)
    val personDb = mockk<PersonDb>(relaxed = true) {
      coEvery { idTrakt } returns 1
    }
    val ids = mockk<IdsRemote>(relaxed = true) {
      coEvery { trakt } returns 1
    }
    val show = mockk<Show>(relaxed = true)
    val movie = mockk<Movie>(relaxed = true)
    coEvery { peopleDao.getById(any()) } returns personDb
    coEvery { traktApi.fetchPersonIds(any(), any()) } returns ids
    coEvery { peopleCreditsDao.getTimestampForPerson(any()) } returns nowUtcMillis() - 100
    coEvery { peopleCreditsDao.getAllShowsForPerson(any()) } returns listOf(show)
    coEvery { peopleCreditsDao.getAllMoviesForPerson(any()) } returns listOf(movie)

    val result = SUT.loadCredits(person)

    assertThat(result).hasSize(2)
    assertThat(result[0].show).isNotNull()
    assertThat(result[1].movie).isNotNull()
    coVerify { peopleDao.getById(any()) }
    coVerify(exactly = 0) { peopleDao.updateTraktId(any(), any()) }
    coVerify(exactly = 0) { traktApi.fetchPersonShowsCredits(any(), any()) }
    coVerify(exactly = 0) { traktApi.fetchPersonMoviesCredits(any(), any()) }
  }

  @Test
  fun `Should return remote credits if Trakt ID is found and cache is invalid`() = runBlocking {
    val person = mockk<Person>(relaxed = true)
    val personDb = mockk<PersonDb>(relaxed = true) {
      coEvery { idTrakt } returns 1
    }
    val ids = mockk<IdsRemote>(relaxed = true) {
      coEvery { trakt } returns 1
    }
    val creditsShows = mockk<PersonCredit>(relaxed = true)
    val creditsMovies = mockk<PersonCredit>(relaxed = true)
    coEvery { peopleDao.getById(any()) } returns personDb
    coEvery { traktApi.fetchPersonIds(any(), any()) } returns ids
    coEvery { traktApi.fetchPersonShowsCredits(any(), any()) } returns listOf(creditsShows)
    coEvery { traktApi.fetchPersonMoviesCredits(any(), any()) } returns listOf(creditsMovies)

    val result = SUT.loadCredits(person)

    assertThat(result).hasSize(2)
    assertThat(result[0].show).isNotNull()
    assertThat(result[1].movie).isNotNull()
    coVerify { peopleDao.getById(any()) }
    coVerify(exactly = 1) { showsDao.upsert(any()) }
    coVerify(exactly = 1) { moviesDao.upsert(any()) }
    coVerify(exactly = 1) { peopleCreditsDao.insert(any(), any()) }
    coVerify(exactly = 0) { peopleDao.updateTraktId(any(), any()) }
  }
}
