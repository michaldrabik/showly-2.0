package com.michaldrabik.ui_progress_movies.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_progress_movies.BaseMockTest
import com.michaldrabik.ui_progress_movies.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesMainCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class ProgressMoviesMainViewModelTest : BaseMockTest() {

  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  @MockK lateinit var mainCase: ProgressMoviesMainCase

  private lateinit var SUT: ProgressMoviesMainViewModel

  private val stateResult = mutableListOf<ProgressMoviesMainUiState>()
  private val messagesResult = mutableListOf<MessageEvent>()

  @Before
  override fun setUp() {
    super.setUp()
    SUT = ProgressMoviesMainViewModel(mainCase)
  }

  @After
  fun tearDown() {
    stateResult.clear()
    messagesResult.clear()
    SUT.viewModelScope.cancel()
    Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  fun `Should emit current timestamp and calendar mode`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.loadProgress()

    with(stateResult.last()) {
      assertThat(timestamp).isGreaterThan(0)
      assertThat(calendarMode).isEqualTo(CalendarMode.PRESENT_FUTURE)
    }

    job.cancel()
  }

  @Test
  fun `Should emit search query`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.onSearchQuery("test")

    with(stateResult.last()) {
      assertThat(searchQuery).isEqualTo("test")
    }

    job.cancel()
  }

  @Test
  fun `Should toggle calendar mode properly`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.toggleCalendarMode()
    SUT.toggleCalendarMode()

    assertThat(stateResult[0].calendarMode).isEqualTo(null)
    assertThat(stateResult[1].calendarMode).isEqualTo(CalendarMode.RECENTS)
    assertThat(stateResult[2].calendarMode).isEqualTo(CalendarMode.PRESENT_FUTURE)

    job.cancel()
  }

  @Test
  fun `Should set watched movie properly and update timestamp`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }
    coEvery { mainCase.addToMyMovies(any<Movie>()) } just Runs

    SUT.setWatchedMovie(Movie.EMPTY)

    assertThat(stateResult[0].timestamp).isEqualTo(null)
    assertThat(stateResult[1].timestamp).isGreaterThan(0)

    coVerify(exactly = 1) { mainCase.addToMyMovies(any<Movie>()) }

    job.cancel()
  }
}
