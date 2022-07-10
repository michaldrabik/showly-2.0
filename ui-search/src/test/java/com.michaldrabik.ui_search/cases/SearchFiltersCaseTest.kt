package com.michaldrabik.ui_search.cases

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.common.Mode
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_search.BaseMockTest
import com.michaldrabik.ui_search.recycler.SearchListItem
import com.michaldrabik.ui_search.utilities.SearchOptions
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("EXPERIMENTAL_API_USAGE")
class SearchFiltersCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var settingsRepository: SettingsRepository
  @RelaxedMockK lateinit var item: SearchListItem

  private lateinit var SUT: SearchFiltersCase

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { settingsRepository.isMoviesEnabled } returns true
    SUT = SearchFiltersCase(settingsRepository)
  }

  @After
  fun tearDown() {
    confirmVerified(settingsRepository)
    clearAllMocks()
  }

  @Test
  fun `Should pass shows and movies if filters are empty`() = runTest {
    val options = SearchOptions()
    val result = SUT.filter(options, item)
    assertThat(result).isTrue()
  }

  @Test
  fun `Should pass shows and movies if filters contain shows and movies`() = runTest {
    val options = SearchOptions(filters = listOf(Mode.SHOWS, Mode.MOVIES))
    val result = SUT.filter(options, item)
    assertThat(result).isTrue()
  }

  @Test
  fun `Should not pass shows if filters contain only movie`() = runTest {
    val options = SearchOptions(filters = listOf(Mode.MOVIES))
    coEvery { item.isShow } returns true
    coEvery { item.isMovie } returns false

    val result = SUT.filter(options, item)

    coVerify(exactly = 1) { settingsRepository.isMoviesEnabled }
    assertThat(result).isFalse()
  }

  @Test
  fun `Should not pass movies if filters contain only show`() = runTest {
    val options = SearchOptions(filters = listOf(Mode.SHOWS))
    coEvery { item.isShow } returns false
    coEvery { item.isMovie } returns true

    val result = SUT.filter(options, item)

    coVerify(exactly = 0) { settingsRepository.isMoviesEnabled }
    assertThat(result).isFalse()
  }

  @Test
  fun `Should not pass movies if movies are disabled`() = runTest {
    val options = SearchOptions(filters = listOf(Mode.MOVIES))
    coEvery { settingsRepository.isMoviesEnabled } returns false

    coEvery { item.isShow } returns false
    coEvery { item.isMovie } returns true

    val result = SUT.filter(options, item)

    coVerify(exactly = 1) { settingsRepository.isMoviesEnabled }
    assertThat(result).isFalse()
  }
}
