package com.michaldrabik.ui_search.cases

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.dao.MoviesDao
import com.michaldrabik.data_local.database.dao.ShowsDao
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_search.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class SearchSuggestionsCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var database: AppDatabase
  @RelaxedMockK lateinit var showsDao: ShowsDao
  @RelaxedMockK lateinit var moviesDao: MoviesDao
  @RelaxedMockK lateinit var mappers: Mappers
  @RelaxedMockK lateinit var settingsRepository: SettingsRepository
  @RelaxedMockK lateinit var translationsRepository: TranslationsRepository

  private lateinit var SUT: SearchSuggestionsCase

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { settingsRepository.isMoviesEnabled } returns true
    coEvery { translationsRepository.getLanguage() } returns "en"
    coEvery { database.showsDao() } returns showsDao
    coEvery { database.moviesDao() } returns moviesDao

    SUT = SearchSuggestionsCase(database, mappers, translationsRepository, settingsRepository)
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should skip preload local shows cache if already loaded`() = runBlockingTest {
    SUT.preloadCache() // Initial preload. Db data should be loaded
    SUT.preloadCache() // Further preload. Db data should not be loaded
    coVerify(exactly = 1) { showsDao.getAll() }
  }

  @Test
  fun `Should skip preload local movies cache if already loaded`() = runBlockingTest {
    SUT.preloadCache() // Initial preload. Db data should be loaded
    SUT.preloadCache() // Further preload. Db data should not be loaded
    coVerify(exactly = 1) { moviesDao.getAll() }
  }

  @Test
  fun `Should skip preload local movies cache if movies disabled`() = runBlockingTest {
    coEvery { settingsRepository.isMoviesEnabled } returns false

    SUT.preloadCache()
    coVerify(exactly = 0) { moviesDao.getAll() }
  }

  @Test
  fun `Should skip preload local shows translations cache if default language`() = runBlockingTest {
    SUT.preloadCache()
    coVerify(exactly = 0) { translationsRepository.loadAllShowsLocal(any()) }
  }

  @Test
  fun `Should skip preload local movies translations cache if default language`() = runBlockingTest {
    SUT.preloadCache()
    coVerify(exactly = 0) { translationsRepository.loadAllMoviesLocal(any()) }
  }

  @Test
  fun `Should skip preload local movies translations cache if not default language but movies are disabled`() =
    runBlockingTest {
      coEvery { translationsRepository.getLanguage() } returns "br"
      coEvery { settingsRepository.isMoviesEnabled } returns false

      SUT.preloadCache()
      coVerify(exactly = 0) { translationsRepository.loadAllMoviesLocal(any()) }
    }

  @Test
  fun `Should preload local cache`() = runBlockingTest {
    SUT.preloadCache()
    coVerify(exactly = 1) { showsDao.getAll() }
    coVerify(exactly = 1) { moviesDao.getAll() }
  }

  @Test
  fun `Should preload local translations cache`() = runBlockingTest {
    coEvery { translationsRepository.getLanguage() } returns "br"

    SUT.preloadCache()

    coVerify(exactly = 1) { translationsRepository.loadAllShowsLocal("br") }
    coVerify(exactly = 1) { translationsRepository.loadAllMoviesLocal("br") }
  }

  @Test
  fun `Should return empty list if query is blank`() = runBlockingTest {
    val result1 = SUT.loadShows("   ", 10)
    val result2 = SUT.loadMovies("   ", 10)

    assertThat(result1).isEmpty()
    assertThat(result2).isEmpty()
    coVerify(exactly = 0) { showsDao.getAll() }
    coVerify(exactly = 0) { moviesDao.getAll() }
  }

  @Test
  fun `Should clear local caches properly`() = runBlockingTest {
    coEvery { translationsRepository.getLanguage() } returns "br"

    SUT.preloadCache()
    SUT.clearCache()
    SUT.preloadCache()

    coVerify(exactly = 2) { showsDao.getAll() }
    coVerify(exactly = 2) { moviesDao.getAll() }
    coVerify(exactly = 2) { translationsRepository.loadAllShowsLocal("br") }
    coVerify(exactly = 2) { translationsRepository.loadAllMoviesLocal("br") }
  }
}
