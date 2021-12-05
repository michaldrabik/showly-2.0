package com.michaldrabik.repository

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.database.dao.PeopleDao
import com.michaldrabik.data_local.database.dao.PeopleShowsMoviesDao
import com.michaldrabik.data_local.database.model.Person
import com.michaldrabik.data_remote.tmdb.api.TmdbApi
import com.michaldrabik.repository.common.BaseMockTest
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
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

class PeopleRepositoryTest : BaseMockTest() {

  @RelaxedMockK lateinit var peopleDao: PeopleDao
  @RelaxedMockK lateinit var peopleShowsMoviesDao: PeopleShowsMoviesDao
  @RelaxedMockK lateinit var person: Person
  @RelaxedMockK lateinit var tmdbApi: TmdbApi
  @RelaxedMockK lateinit var settingsRepository: SettingsRepository

  private lateinit var SUT: PeopleRepository

  @Before
  override fun setUp() {
    super.setUp()
    SUT = PeopleRepository(settingsRepository, database, cloud, mappers)
    coEvery { database.peopleDao() } returns peopleDao
    coEvery { database.peopleShowsMoviesDao() } returns peopleShowsMoviesDao
    coEvery { cloud.tmdbApi } returns tmdbApi
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
    coVerify(exactly = 0) { tmdbApi.fetchShowActors(any()) }
  }

  @Test
  fun `Should return remote data for shows properly`() = runBlocking {
    coEvery { peopleShowsMoviesDao.getTimestampForShow(any()) } returns nowUtc().minusDays(10).toMillis()
    coEvery { peopleDao.getAllForShow(any()) } returns listOf(person)

    SUT.loadAllForShow(Ids.EMPTY.copy(trakt = IdTrakt(11), tmdb = IdTmdb(12)))

    coVerifyOrder {
      peopleShowsMoviesDao.getTimestampForShow(11)
      peopleDao.getAllForShow(11)
      tmdbApi.fetchShowActors(12)
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
    coVerify(exactly = 0) { tmdbApi.fetchMovieActors(any()) }
  }

  @Test
  fun `Should return remote data for movies properly`() = runBlocking {
    coEvery { peopleShowsMoviesDao.getTimestampForMovie(any()) } returns nowUtc().minusDays(10).toMillis()
    coEvery { peopleDao.getAllForMovie(any()) } returns listOf(person)

    SUT.loadAllForMovie(Ids.EMPTY.copy(trakt = IdTrakt(11), tmdb = IdTmdb(12)))

    coVerifyOrder {
      peopleShowsMoviesDao.getTimestampForMovie(11)
      peopleDao.getAllForMovie(11)
      tmdbApi.fetchMovieActors(12)
      peopleDao.upsert(any())
      peopleShowsMoviesDao.insertForMovie(any(), 11)
    }
  }

  @Test
  fun `Should return shows items with image in the first place`() = runBlocking {
    coEvery { peopleShowsMoviesDao.getTimestampForShow(any()) } returns nowUtc().minusHours(10).toMillis()

    val person1 = mockk<Person>(relaxed = true) { coEvery { image } returns null }
    val person2 = mockk<Person>(relaxed = true) { coEvery { image } returns "test" }
    val person3 = mockk<Person>(relaxed = true) { coEvery { image } returns "test" }
    coEvery { peopleDao.getAllForShow(any()) } returns listOf(person1, person2, person3)

    val result = SUT.loadAllForShow(Ids.EMPTY.copy(trakt = IdTrakt(11)))
    assertThat(result.last().imagePath).isNull()

    coVerify { peopleDao.getAllForShow(any()) }
  }

  @Test
  fun `Should return movies items with image in the first place`() = runBlocking {
    coEvery { peopleShowsMoviesDao.getTimestampForMovie(any()) } returns nowUtc().minusHours(10).toMillis()

    val person1 = mockk<Person>(relaxed = true) { coEvery { image } returns null }
    val person2 = mockk<Person>(relaxed = true) { coEvery { image } returns "test" }
    val person3 = mockk<Person>(relaxed = true) { coEvery { image } returns "test" }
    coEvery { peopleDao.getAllForMovie(any()) } returns listOf(person1, person2, person3)

    val result = SUT.loadAllForMovie(Ids.EMPTY.copy(trakt = IdTrakt(11)))
    assertThat(result.last().imagePath).isNull()

    coVerify { peopleDao.getAllForMovie(any()) }
  }
}
