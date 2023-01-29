package com.michaldrabik.ui_progress_movies.progress

import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_progress_movies.BaseMockTest
import com.michaldrabik.ui_progress_movies.main.MovieCheckActionUiEvent
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainUiState
import com.michaldrabik.ui_progress_movies.progress.cases.ProgressMoviesItemsCase
import com.michaldrabik.ui_progress_movies.progress.cases.ProgressMoviesPinnedCase
import com.michaldrabik.ui_progress_movies.progress.cases.ProgressMoviesSortCase
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
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
class ProgressMoviesViewModelTest : BaseMockTest() {

  @MockK lateinit var itemsCase: ProgressMoviesItemsCase
  @MockK lateinit var sortCase: ProgressMoviesSortCase
  @MockK lateinit var pinnedCase: ProgressMoviesPinnedCase
  @MockK lateinit var imagesProvider: MovieImagesProvider
  @MockK lateinit var userTraktManager: UserTraktManager
  @MockK lateinit var workManager: WorkManager
  @MockK lateinit var settingsRepository: SettingsRepository
  @MockK lateinit var translationsRepository: TranslationsRepository

  private lateinit var SUT: ProgressMoviesViewModel
  private val parentState = ProgressMoviesMainUiState()

  private val stateResult = mutableListOf<ProgressMoviesUiState>()
  private val eventsResult = mutableListOf<Event<*>>()
  private val messagesResult = mutableListOf<MessageEvent>()

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { translationsRepository.getLanguage() } returns "en"
    coEvery { userTraktManager.isAuthorized() } returns false

    SUT = ProgressMoviesViewModel(
      itemsCase,
      sortCase,
      pinnedCase,
      imagesProvider,
      userTraktManager,
      workManager,
      settingsRepository,
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
    val item = mockk<ProgressMovieListItem.MovieItem>()
    coEvery { itemsCase.loadItems(any()) } returns listOf(item)

    SUT.onParentState(parentState.copy(timestamp = 123))

    assertThat(stateResult.last().items).containsExactly(item)
    coVerify(exactly = 1) { itemsCase.loadItems(any()) }
    job.cancel()
  }

  @Test
  fun `Should not reload items if parent timestamp is the same`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val item = mockk<ProgressMovieListItem.MovieItem>()
    coEvery { itemsCase.loadItems(any()) } returns listOf(item)

    SUT.onParentState(parentState.copy(timestamp = 0))

    assertThat(stateResult.lastOrNull()?.items).isNull()
    coVerify { itemsCase wasNot Called }
    job.cancel()
  }

  @Test
  fun `Should load items if search query changed`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val item = mockk<ProgressMovieListItem.MovieItem>()
    coEvery { itemsCase.loadItems(any()) } returns listOf(item)

    SUT.onParentState(parentState.copy(timestamp = 0, searchQuery = "test"))

    assertThat(stateResult.last().items).containsExactly(item)
    coVerify(exactly = 1) { itemsCase.loadItems(any()) }
    job.cancel()
  }

  @Test
  fun `Should not reload items if parent search query is the same`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val item = mockk<ProgressMovieListItem.MovieItem>()
    coEvery { itemsCase.loadItems(any()) } returns listOf(item)

    SUT.onParentState(parentState.copy(timestamp = 0, searchQuery = "test"))
    SUT.onParentState(parentState.copy(timestamp = 0, searchQuery = "test"))

    assertThat(stateResult.last().items).containsExactly(item)
    coVerify(exactly = 1) { itemsCase.loadItems(any()) }
    job.cancel()
  }

  @Test
  fun `Should toggle pinned item if pinned`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    coEvery { pinnedCase.addPinnedItem(any()) } just Runs
    coEvery { pinnedCase.removePinnedItem(any()) } just Runs
    coEvery { itemsCase.loadItems(any()) } returns listOf(mockk())

    val item = mockk<ProgressMovieListItem.MovieItem> {
      coEvery { isPinned } returns true
      coEvery { movie } returns Movie.EMPTY
    }

    SUT.togglePinItem(item)

    coVerify(exactly = 1) { pinnedCase.removePinnedItem(any()) }
    coVerify(exactly = 0) { pinnedCase.addPinnedItem(any()) }
    coVerify(exactly = 1) { itemsCase.loadItems(any()) }
    job.cancel()
  }

  @Test
  fun `Should toggle pinned item if not pinned`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    coEvery { pinnedCase.addPinnedItem(any()) } just Runs
    coEvery { pinnedCase.removePinnedItem(any()) } just Runs
    coEvery { itemsCase.loadItems(any()) } returns listOf(mockk())

    val item = mockk<ProgressMovieListItem.MovieItem> {
      coEvery { isPinned } returns false
      coEvery { movie } returns Movie.EMPTY
    }

    SUT.togglePinItem(item)

    coVerify(exactly = 0) { pinnedCase.removePinnedItem(any()) }
    coVerify(exactly = 1) { pinnedCase.addPinnedItem(any()) }
    coVerify(exactly = 1) { itemsCase.loadItems(any()) }
    job.cancel()
  }

  @Test
  fun `Should check quick rate option enabled`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.eventFlow.toList(eventsResult) }
    coEvery { userTraktManager.isAuthorized() } returns true
    coEvery { settingsRepository.isPremium } returns true
    coEvery { settingsRepository.load() } returns Settings.createInitial().copy(traktQuickRateEnabled = true)

    SUT.onMovieChecked(Movie.EMPTY)

    assertThat(eventsResult.last()).isInstanceOf(MovieCheckActionUiEvent::class.java)
    assertThat((eventsResult.last() as MovieCheckActionUiEvent).isQuickRate).isTrue()
    coVerify(exactly = 1) { userTraktManager.isAuthorized() }
    coVerify(exactly = 1) { settingsRepository.load() }
    job.cancel()
  }

  @Test
  fun `Should check quick rate option not enabled`() = runTest {
    val job = launch(UnconfinedTestDispatcher()) { SUT.eventFlow.toList(eventsResult) }
    coEvery { userTraktManager.isAuthorized() } returns true
    coEvery { settingsRepository.isPremium } returns true
    coEvery { settingsRepository.load() } returns Settings.createInitial().copy(traktQuickRateEnabled = false)

    SUT.onMovieChecked(Movie.EMPTY)

    assertThat(eventsResult.last()).isInstanceOf(MovieCheckActionUiEvent::class.java)
    assertThat((eventsResult.last() as MovieCheckActionUiEvent).isQuickRate).isFalse()
    coVerify(exactly = 1) { userTraktManager.isAuthorized() }
    coVerify(exactly = 1) { settingsRepository.load() }
    job.cancel()
  }
}
