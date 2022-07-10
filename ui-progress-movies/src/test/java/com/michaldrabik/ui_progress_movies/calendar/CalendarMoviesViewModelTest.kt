package com.michaldrabik.ui_progress_movies.calendar

import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_progress_movies.BaseMockTest
import com.michaldrabik.ui_progress_movies.calendar.cases.CalendarMoviesRatingsCase
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesFutureCase
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesRecentsCase
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainUiState
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarMoviesViewModelTest : BaseMockTest() {

  @MockK lateinit var recentsCase: CalendarMoviesRecentsCase
  @MockK lateinit var futureCase: CalendarMoviesFutureCase
  @MockK lateinit var ratingsCase: CalendarMoviesRatingsCase
  @MockK lateinit var imagesProvider: MovieImagesProvider
  @MockK lateinit var translationsRepository: TranslationsRepository

  private lateinit var SUT: CalendarMoviesViewModel
  private val parentState = ProgressMoviesMainUiState(calendarMode = CalendarMode.PRESENT_FUTURE)

  private val stateResult = mutableListOf<CalendarMoviesUiState>()
  private val messagesResult = mutableListOf<MessageEvent>()

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { translationsRepository.getLanguage() } returns "en"

    SUT = CalendarMoviesViewModel(
      recentsCase,
      futureCase,
      ratingsCase,
      imagesProvider,
      translationsRepository
    )
  }

  @After
  fun tearDown() {
    stateResult.clear()
    messagesResult.clear()
    SUT.viewModelScope.cancel()
  }

  @Test
  fun `Should load items if parent timestamp changed`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val item = mockk<CalendarMovieListItem.MovieItem>()
    coEvery { futureCase.loadItems(any()) } returns listOf(item)

    SUT.onParentState(parentState.copy(timestamp = 123))

    assertThat(stateResult.last().items).containsExactly(item)
    coVerify(exactly = 1) { futureCase.loadItems(any()) }
    coVerify { recentsCase wasNot Called }
    job.cancel()
  }

  @Test
  fun `Should not reload items if parent timestamp is the same`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val item = mockk<CalendarMovieListItem.MovieItem>()
    coEvery { futureCase.loadItems(any()) } returns listOf(item)

    SUT.onParentState(parentState.copy(timestamp = 0))

    assertThat(stateResult.lastOrNull()?.items).isNull()
    coVerify { futureCase wasNot Called }
    job.cancel()
  }

  @Test
  fun `Should load items if calendar mode changed`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val item = mockk<CalendarMovieListItem.MovieItem>()
    coEvery { recentsCase.loadItems(any()) } returns listOf(item)

    SUT.onParentState(parentState.copy(timestamp = 0, calendarMode = CalendarMode.RECENTS))

    assertThat(stateResult.last().items).containsExactly(item)
    coVerify(exactly = 1) { recentsCase.loadItems(any()) }
    coVerify { futureCase wasNot Called }
    job.cancel()
  }

  @Test
  fun `Should load items if search query changed`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val item = mockk<CalendarMovieListItem.MovieItem>()
    coEvery { futureCase.loadItems(any()) } returns listOf(item)

    SUT.onParentState(parentState.copy(timestamp = 0, searchQuery = "test"))

    assertThat(stateResult.last().items).containsExactly(item)
    coVerify(exactly = 1) { futureCase.loadItems(any()) }
    coVerify { recentsCase wasNot Called }
    job.cancel()
  }

  @Test
  fun `Should not reload items if parent search query is the same`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val item = mockk<CalendarMovieListItem.MovieItem>()
    coEvery { futureCase.loadItems(any()) } returns listOf(item)

    SUT.onParentState(parentState.copy(timestamp = 0, searchQuery = "test"))
    SUT.onParentState(parentState.copy(timestamp = 0, searchQuery = "test"))

    assertThat(stateResult.last().items).containsExactly(item)
    coVerify(exactly = 1) { futureCase.loadItems(any()) }
    coVerify { recentsCase wasNot Called }
    job.cancel()
  }

  @Test
  fun `Should check quick rate option enabled`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    coEvery { ratingsCase.isQuickRateEnabled() } returns true
    assertThat(SUT.isQuickRateEnabled).isFalse()

    SUT.checkQuickRateEnabled()

    assertThat(SUT.isQuickRateEnabled).isTrue()
    coVerify(exactly = 1) { ratingsCase.isQuickRateEnabled() }
    job.cancel()
  }
}
