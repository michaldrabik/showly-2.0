package com.michaldrabik.ui_progress_movies.calendar.cases

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_progress_movies.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class CalendarMoviesRatingsCaseTest : BaseMockTest() {

  @RelaxedMockK lateinit var userTraktManager: UserTraktManager
  @RelaxedMockK lateinit var settingsRepository: SettingsRepository

  private lateinit var SUT: CalendarMoviesRatingsCase

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { userTraktManager.isAuthorized() } returns true
    coEvery { settingsRepository.isPremium } returns true
    coEvery { settingsRepository.load().traktQuickRateEnabled } returns true

    SUT = CalendarMoviesRatingsCase(userTraktManager, settingsRepository)
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should check quick rate enabled`() = runBlockingTest {
    val result = SUT.isQuickRateEnabled()
    assertThat(result).isTrue()
  }

  @Test
  fun `Should be false if user is not signed in`() = runBlockingTest {
    coEvery { userTraktManager.isAuthorized() } returns false

    val result = SUT.isQuickRateEnabled()
    assertThat(result).isFalse()
  }

  @Test
  fun `Should be false if user is not premium`() = runBlockingTest {
    coEvery { settingsRepository.isPremium } returns false

    val result = SUT.isQuickRateEnabled()
    assertThat(result).isFalse()
  }

  @Test
  fun `Should be false if user is feature is disabled`() = runBlockingTest {
    coEvery { settingsRepository.load().traktQuickRateEnabled } returns false

    val result = SUT.isQuickRateEnabled()
    assertThat(result).isFalse()
  }
}
