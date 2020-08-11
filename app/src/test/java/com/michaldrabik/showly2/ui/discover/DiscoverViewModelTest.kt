package com.michaldrabik.showly2.ui.discover

import BaseMockTest
import TestData
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.model.DiscoverFilters
import com.michaldrabik.showly2.ui.discover.cases.DiscoverFiltersCase
import com.michaldrabik.showly2.ui.discover.cases.DiscoverShowsCase
import com.michaldrabik.showly2.utilities.MessageEvent
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

@Suppress("EXPERIMENTAL_API_USAGE")
class DiscoverViewModelTest : BaseMockTest() {

  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  @MockK lateinit var showsCase: DiscoverShowsCase
  @MockK lateinit var filtersCase: DiscoverFiltersCase
  @MockK lateinit var imagesProvider: ShowImagesProvider

  @MockK lateinit var mockUiObserver: Observer<DiscoverUiModel>
  @MockK lateinit var mockMessageObserver: Observer<MessageEvent>

  private lateinit var SUT: DiscoverViewModel

  @Before
  override fun setUp() {
    super.setUp()
    Dispatchers.setMain(testDispatcher)

    coEvery { filtersCase.loadFilters() } returns DiscoverFilters()
    coEvery { filtersCase.saveFilters(any()) } just Runs
    coEvery { showsCase.loadCachedShows(any()) } returns emptyList()
    coEvery { showsCase.loadRemoteShows(any()) } returns emptyList()

    every { mockUiObserver.onChanged(any()) } just Runs
    every { mockMessageObserver.onChanged(any()) } just Runs

    SUT = DiscoverViewModel(showsCase, filtersCase, imagesProvider)
    SUT.uiLiveData.observeForever(mockUiObserver)
    SUT.messageLiveData.observeForever(mockMessageObserver)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  internal fun `Should not pull to refresh data too often`() {
    SUT.lastPullToRefreshMs = nowUtcMillis() - TimeUnit.SECONDS.toMillis(5)
    SUT.loadDiscoverShows(true)

    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 0) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  internal fun `Should load cached data and not load remote data if cache is valid`() {
    coEvery { showsCase.isCacheValid() } returns true

    SUT.loadDiscoverShows()

    coVerify(exactly = 1) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 0) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  internal fun `Should load cached data and load remote data if cache is no longer valid`() {
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadDiscoverShows()

    coVerify(exactly = 1) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 1) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  internal fun `Should load remote data only if pull to refresh`() {
    coEvery { showsCase.isCacheValid() } returns true

    SUT.loadDiscoverShows(pullToRefresh = true)

    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 1) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  internal fun `Should load remote data only if skipping cache`() {
    coEvery { showsCase.isCacheValid() } returns true

    SUT.loadDiscoverShows(skipCache = true)

    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 1) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  internal fun `Should not load cached data if skipping cache`() {
    SUT.loadDiscoverShows(skipCache = true)
    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
  }

  @Test
  internal fun `Should update last PTR stamp if PTR`() {
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadDiscoverShows(pullToRefresh = true)
    assertThat(SUT.lastPullToRefreshMs).isGreaterThan(0)
  }

  @Test
  internal fun `Should not update last PTR stamp if was not PTR`() {
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadDiscoverShows(pullToRefresh = false)
    assertThat(SUT.lastPullToRefreshMs).isEqualTo(0)
  }

  @Test
  internal fun `Should hide loading state when PTR is run too often`() {
    SUT.lastPullToRefreshMs = nowUtcMillis() - TimeUnit.SECONDS.toMillis(5)
    SUT.loadDiscoverShows(true)

    val uiState = mutableListOf<DiscoverUiModel>()
    verify { mockUiObserver.onChanged(capture(uiState)) }
    assertThat(uiState.first().showLoading).isFalse()

    verify(exactly = 0) { mockMessageObserver.onChanged(any()) }
  }

  @Test
  internal fun `Should show loading state instantly if pull to refresh`() {
    SUT.loadDiscoverShows(true)

    val uiState = mutableListOf<DiscoverUiModel>()
    verify { mockUiObserver.onChanged(capture(uiState)) }
    assertThat(uiState.first().showLoading).isTrue()
    assertThat(uiState.last().showLoading).isFalse()

    verify(exactly = 0) { mockMessageObserver.onChanged(any()) }
  }

  @Test
  internal fun `Should not post cached results if pull to refresh`() {
    val cachedItem = TestData.DISCOVER_LIST_ITEM
    coEvery { showsCase.loadCachedShows(any()) } returns listOf(cachedItem)

    SUT.loadDiscoverShows(pullToRefresh = true)

    val uiState = mutableListOf<DiscoverUiModel>()
    verify { mockUiObserver.onChanged(capture(uiState)) }

    assertThat(uiState.any { it.shows != null }).isTrue()
    uiState
      .filter { it.shows != null }
      .forEach { state ->
        assertThat(state.shows).containsNoneIn(listOf(cachedItem))
      }

    verify(exactly = 0) { mockMessageObserver.onChanged(any()) }
  }

  @Test
  internal fun `Should not post cached results if skipping cache`() {
    val cachedItem = TestData.DISCOVER_LIST_ITEM
    coEvery { showsCase.loadCachedShows(any()) } returns listOf(cachedItem)

    SUT.loadDiscoverShows(skipCache = true)

    val uiState = mutableListOf<DiscoverUiModel>()
    verify { mockUiObserver.onChanged(capture(uiState)) }

    assertThat(uiState.any { it.shows != null }).isTrue()
    uiState
      .filter { it.shows != null }
      .forEach { state ->
        assertThat(state.shows).containsNoneIn(listOf(cachedItem))
      }

    verify(exactly = 0) { mockMessageObserver.onChanged(any()) }
  }

  @Test
  internal fun `Should not post cached results and then fresh remote results`() {
    val cachedItem = TestData.DISCOVER_LIST_ITEM
    val remoteItem = cachedItem.copy(isFollowed = true)
    coEvery { showsCase.loadCachedShows(any()) } returns listOf(cachedItem)
    coEvery { showsCase.loadRemoteShows(any()) } returns listOf(remoteItem)
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadDiscoverShows()

    val uiStates = mutableListOf<DiscoverUiModel>()
    verify { mockUiObserver.onChanged(capture(uiStates)) }

    assertThat(uiStates[2].shows!!.contains(cachedItem)).isTrue()
    assertThat(uiStates[3].shows!!.contains(remoteItem)).isTrue()

    verify(exactly = 0) { mockMessageObserver.onChanged(any()) }
  }

  @Test
  internal fun `Should post error message on error`() {
    coEvery { showsCase.loadCachedShows(any()) } throws Error()

    SUT.loadDiscoverShows()

    val messageStates = mutableListOf<MessageEvent>()
    verify(exactly = 1) { mockMessageObserver.onChanged(capture(messageStates)) }
    assertThat(messageStates).hasSize(1)
    assertThat(messageStates.first().consume()).isEqualTo(R.string.errorCouldNotLoadDiscover)
  }
}
