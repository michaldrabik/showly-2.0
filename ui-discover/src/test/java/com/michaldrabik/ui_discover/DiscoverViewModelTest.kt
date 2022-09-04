package com.michaldrabik.ui_discover

import BaseMockTest
import TestData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_discover.cases.DiscoverFiltersCase
import com.michaldrabik.ui_discover.cases.DiscoverShowsCase
import com.michaldrabik.ui_discover.cases.DiscoverTwitterCase
import com.michaldrabik.ui_model.DiscoverFilters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("EXPERIMENTAL_API_USAGE")
class DiscoverViewModelTest : BaseMockTest() {

  @MockK lateinit var showsCase: DiscoverShowsCase
  @MockK lateinit var filtersCase: DiscoverFiltersCase
  @MockK lateinit var twitterCase: DiscoverTwitterCase
  @MockK lateinit var imagesProvider: ShowImagesProvider
  @RelaxedMockK lateinit var workManager: WorkManager

  private lateinit var SUT: DiscoverViewModel

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { filtersCase.loadFilters() } returns DiscoverFilters()
    coEvery { showsCase.loadCachedShows(any()) } returns emptyList()
    coEvery { showsCase.loadRemoteShows(any()) } returns emptyList()

    SUT = DiscoverViewModel(showsCase, filtersCase, twitterCase, imagesProvider, workManager)
  }

  @After
  fun tearDown() {
    SUT.viewModelScope.cancel()
  }

  @Test
  fun `Should not pull to refresh data too often`() = runTest {
    SUT.lastPullToRefreshMs = nowUtcMillis() - TimeUnit.SECONDS.toMillis(5)
    SUT.loadShows(pullToRefresh = true)

    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 0) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  fun `Should load cached data and not load remote data if cache is valid`() {
    coEvery { showsCase.isCacheValid() } returns true

    SUT.loadShows()

    coVerify(exactly = 1) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 0) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  fun `Should load cached data and load remote data if cache is no longer valid`() {
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadShows()

    coVerify(exactly = 1) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 1) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  fun `Should load remote data only if pull to refresh`() {
    coEvery { showsCase.isCacheValid() } returns true

    SUT.loadShows(pullToRefresh = true)

    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 1) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  fun `Should load remote data only if skipping cache`() {
    coEvery { showsCase.isCacheValid() } returns true

    SUT.loadShows(skipCache = true)

    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 1) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  fun `Should not load cached data if skipping cache`() {
    SUT.loadShows(skipCache = true)
    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
  }

  @Test
  fun `Should update last PTR stamp if PTR`() = runTest {
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadShows(pullToRefresh = true)
    assertThat(SUT.lastPullToRefreshMs).isGreaterThan(nowUtcMillis() - TimeUnit.MINUTES.toMillis(1))
  }

  @Test
  fun `Should not update last PTR stamp if was not PTR`() {
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadShows(pullToRefresh = false)
    assertThat(SUT.lastPullToRefreshMs).isEqualTo(0)
  }

  @Test
  fun `Should hide loading state when PTR is run too often`() = runTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val job2 = launch(UnconfinedTestDispatcher()) { SUT.messageFlow.toList(messagesResult) }

    SUT.lastPullToRefreshMs = nowUtcMillis() - TimeUnit.SECONDS.toMillis(5)
    SUT.loadShows(pullToRefresh = true)

    assertThat(stateResult[0].isLoading).isNull()
    assertThat(stateResult[1].isLoading).isFalse()
    assertThat(stateResult[2].isLoading).isTrue()
    assertThat(stateResult[3].isLoading).isFalse()
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  fun `Should show loading state instantly if pull to refresh`() = runTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val job2 = launch(UnconfinedTestDispatcher()) { SUT.messageFlow.toList(messagesResult) }

    SUT.loadShows(pullToRefresh = true)

    assertThat(stateResult[0].isLoading).isNull()
    assertThat(stateResult[1].isLoading).isFalse()
    assertThat(stateResult[2].isLoading).isTrue()
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  fun `Should not emit cached results if pull to refresh`() = runTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageFlow.toList(messagesResult) }

    val cachedItem = TestData.DISCOVER_LIST_ITEM
    coEvery { showsCase.loadCachedShows(any()) } returns listOf(cachedItem)

    SUT.loadShows(pullToRefresh = true)

    stateResult.forEach {
      assertThat(it.items.isNullOrEmpty()).isTrue()
    }
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  fun `Should not post cached results if skipping cache`() = runTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageFlow.toList(messagesResult) }

    val cachedItem = TestData.DISCOVER_LIST_ITEM
    coEvery { showsCase.loadCachedShows(any()) } returns listOf(cachedItem)

    SUT.loadShows(skipCache = true)

    stateResult.forEach {
      assertThat(it.items.isNullOrEmpty()).isTrue()
    }
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  fun `Should post cached results and then fresh remote results`() = runTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val job2 = launch(UnconfinedTestDispatcher()) { SUT.messageFlow.toList(messagesResult) }

    val cachedItem = TestData.DISCOVER_LIST_ITEM
    val remoteItem = cachedItem.copy(isFollowed = true)
    coEvery { showsCase.loadCachedShows(any()) } returns listOf(cachedItem)
    coEvery { showsCase.loadRemoteShows(any()) } coAnswers {
      delay(1000)
      listOf(remoteItem)
    }
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadShows()
    advanceUntilIdle()

    assertThat(stateResult.any { it.items?.contains(cachedItem) == true }).isTrue()
    assertThat(stateResult.last().items?.contains(remoteItem)).isTrue()
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  fun `Should post error message on error`() = runTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }
    val job2 = launch(UnconfinedTestDispatcher()) { SUT.messageFlow.toList(messagesResult) }

    coEvery { showsCase.loadCachedShows(any()) } throws Error()

    SUT.loadShows()

    assertThat(messagesResult.last().consume()).isEqualTo(com.michaldrabik.ui_base.R.string.errorCouldNotLoadDiscover)

    job.cancel()
    job2.cancel()
  }
}
