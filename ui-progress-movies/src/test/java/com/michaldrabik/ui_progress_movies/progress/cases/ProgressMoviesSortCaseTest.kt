package com.michaldrabik.ui_progress_movies.progress.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_progress_movies.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class ProgressMoviesSortCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var settingsRepository: SettingsRepository

  private lateinit var SUT: ProgressMoviesSortCase

  @Before
  override fun setUp() {
    super.setUp()
    SUT = ProgressMoviesSortCase(settingsRepository)
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should set sorting order properly`() = runBlockingTest {
    SUT.setSortOrder(SortOrder.RANK, SortType.DESCENDING)

    coVerify { settingsRepository.sorting setProperty "progressMoviesSortOrder" value SortOrder.RANK }
    coVerify { settingsRepository.sorting setProperty "progressMoviesSortType" value SortType.DESCENDING }
  }
}
