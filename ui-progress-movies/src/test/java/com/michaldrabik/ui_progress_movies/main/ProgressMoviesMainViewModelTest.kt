package com.michaldrabik.ui_progress_movies.main

import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_progress_movies.BaseMockTest
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesMainCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressMoviesMainViewModelTest : BaseMockTest() {

  @MockK lateinit var mainCase: ProgressMoviesMainCase
  @MockK lateinit var eventsManager: EventsManager
  @RelaxedMockK lateinit var workManager: WorkManager

  private lateinit var SUT: ProgressMoviesMainViewModel

  private val stateResult = mutableListOf<ProgressMoviesMainUiState>()
  private val messagesResult = mutableListOf<MessageEvent>()

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { eventsManager.events } returns MutableSharedFlow()

    SUT = ProgressMoviesMainViewModel(mainCase, eventsManager, workManager)
  }

  @After
  fun tearDown() {
    stateResult.clear()
    messagesResult.clear()
    SUT.viewModelScope.cancel()
  }

  @Test
  fun `Should emit current timestamp and calendar mode`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadProgress()

    with(stateResult.last()) {
      assertThat(timestamp).isGreaterThan(0)
      assertThat(calendarMode).isEqualTo(CalendarMode.PRESENT_FUTURE)
    }

    job.cancel()
  }

  @Test
  fun `Should emit search query`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.onSearchQuery("test")

    with(stateResult.last()) {
      assertThat(searchQuery).isEqualTo("test")
    }

    job.cancel()
  }

  @Test
  fun `Should toggle calendar mode properly`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.toggleCalendarMode()
    SUT.toggleCalendarMode()

    assertThat(stateResult[0].calendarMode).isEqualTo(null)
    assertThat(stateResult[1].calendarMode).isEqualTo(CalendarMode.RECENTS)
    assertThat(stateResult[2].calendarMode).isEqualTo(CalendarMode.PRESENT_FUTURE)

    job.cancel()
  }

  @Test
  fun `Should set watched movie properly and update timestamp`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    coEvery { mainCase.addToMyMovies(any<Movie>()) } just Runs

    SUT.setWatchedMovie(Movie.EMPTY)

    assertThat(stateResult[0].timestamp).isEqualTo(null)
    assertThat(stateResult[1].timestamp).isGreaterThan(0)

    coVerify(exactly = 1) { mainCase.addToMyMovies(any<Movie>()) }

    job.cancel()
  }
}
