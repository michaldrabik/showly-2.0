package com.michaldrabik.ui_progress_movies.calendar.cases

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_progress_movies.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
  fun `Should check quick rate enabled`() = runTest {
    val result = SUT.isQuickRateEnabled()
    assertThat(result).isTrue()
  }

  @Test
  fun `Should be false if user is not signed in`() = runTest {
    coEvery { userTraktManager.isAuthorized() } returns false

    val result = SUT.isQuickRateEnabled()
    assertThat(result).isFalse()
  }

  @Test
  fun `Should be false if user is not premium`() = runTest {
    coEvery { settingsRepository.isPremium } returns false

    val result = SUT.isQuickRateEnabled()
    assertThat(result).isFalse()
  }

  @Test
  fun `Should be false if user is feature is disabled`() = runTest {
    coEvery { settingsRepository.load().traktQuickRateEnabled } returns false

    val result = SUT.isQuickRateEnabled()
    assertThat(result).isFalse()
  }
}
