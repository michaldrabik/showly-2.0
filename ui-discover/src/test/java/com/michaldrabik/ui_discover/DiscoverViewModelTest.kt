package com.michaldrabik.ui_discover

import BaseMockTest
import TestData
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_discover.cases.DiscoverFiltersCase
import com.michaldrabik.ui_discover.cases.DiscoverShowsCase
import com.michaldrabik.ui_model.DiscoverFilters
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

  private lateinit var SUT: DiscoverViewModel

  @Before
  override fun setUp() {
    super.setUp()
    Dispatchers.setMain(testDispatcher)

    coEvery { filtersCase.loadFilters() } returns DiscoverFilters()
    coEvery { filtersCase.saveFilters(any()) } just Runs
    coEvery { showsCase.loadCachedShows(any()) } returns emptyList()
    coEvery { showsCase.loadRemoteShows(any()) } returns emptyList()

    SUT = DiscoverViewModel(showsCase, filtersCase, imagesProvider)
  }

  @After
  fun tearDown() {
    SUT.viewModelScope.cancel()
    Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  internal fun `Should not pull to refresh data too often`() = runBlockingTest {
    SUT.lastPullToRefreshMs = nowUtcMillis() - TimeUnit.SECONDS.toMillis(5)
    SUT.loadItems(pullToRefresh = true)

    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 0) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  internal fun `Should load cached data and not load remote data if cache is valid`() {
    coEvery { showsCase.isCacheValid() } returns true

    SUT.loadItems()

    coVerify(exactly = 1) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 0) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  internal fun `Should load cached data and load remote data if cache is no longer valid`() {
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadItems()

    coVerify(exactly = 1) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 1) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  internal fun `Should load remote data only if pull to refresh`() {
    coEvery { showsCase.isCacheValid() } returns true

    SUT.loadItems(pullToRefresh = true)

    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 1) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  internal fun `Should load remote data only if skipping cache`() {
    coEvery { showsCase.isCacheValid() } returns true

    SUT.loadItems(skipCache = true)

    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
    coVerify(exactly = 1) { showsCase.loadRemoteShows(any()) }
  }

  @Test
  internal fun `Should not load cached data if skipping cache`() {
    SUT.loadItems(skipCache = true)
    coVerify(exactly = 0) { showsCase.loadCachedShows(any()) }
  }

  @Test
  internal fun `Should update last PTR stamp if PTR`() {
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadItems(pullToRefresh = true)
    assertThat(SUT.lastPullToRefreshMs).isGreaterThan(0)
  }

  @Test
  internal fun `Should not update last PTR stamp if was not PTR`() {
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadItems(pullToRefresh = false)
    assertThat(SUT.lastPullToRefreshMs).isEqualTo(0)
  }

  @Test
  internal fun `Should hide loading state when PTR is run too often`() = runBlockingTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.lastPullToRefreshMs = nowUtcMillis() - TimeUnit.SECONDS.toMillis(5)
    SUT.loadItems(pullToRefresh = true)

    assertThat(stateResult[0].isLoading).isFalse()
    assertThat(stateResult[1].isLoading).isTrue()
    assertThat(stateResult[2].isLoading).isFalse()
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should show loading state instantly if pull to refresh`() = runBlockingTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    SUT.loadItems(pullToRefresh = true)

    assertThat(stateResult[0].isLoading).isFalse()
    assertThat(stateResult[1].isLoading).isTrue()
    assertThat(stateResult[2].isLoading).isTrue()
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should not emit cached results if pull to refresh`() = runBlockingTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    val cachedItem = TestData.DISCOVER_LIST_ITEM
    coEvery { showsCase.loadCachedShows(any()) } returns listOf(cachedItem)

    SUT.loadItems(pullToRefresh = true)

    stateResult.forEach {
      assertThat(it.items.isNullOrEmpty()).isTrue()
    }
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should not post cached results if skipping cache`() = runBlockingTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    val cachedItem = TestData.DISCOVER_LIST_ITEM
    coEvery { showsCase.loadCachedShows(any()) } returns listOf(cachedItem)

    SUT.loadItems(skipCache = true)

    stateResult.forEach {
      assertThat(it.items.isNullOrEmpty()).isTrue()
    }
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should post cached results and then fresh remote results`() = runBlockingTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    val cachedItem = TestData.DISCOVER_LIST_ITEM
    val remoteItem = cachedItem.copy(isFollowed = true)
    coEvery { showsCase.loadCachedShows(any()) } returns listOf(cachedItem)
    coEvery { showsCase.loadRemoteShows(any()) } returns listOf(remoteItem)
    coEvery { showsCase.isCacheValid() } returns false

    SUT.loadItems()

    assertThat(stateResult.any { it.items?.contains(cachedItem) == true }).isTrue()
    assertThat(stateResult.last().items?.contains(remoteItem)).isTrue()
    assertThat(messagesResult).isEmpty()

    job.cancel()
    job2.cancel()
  }

  @Test
  internal fun `Should post error message on error`() = runBlockingTest {
    val stateResult = mutableListOf<DiscoverUiState>()
    val messagesResult = mutableListOf<MessageEvent>()

    val job = launch { SUT.uiState.toList(stateResult) }
    val job2 = launch { SUT.messageState.toList(messagesResult) }

    coEvery { showsCase.loadCachedShows(any()) } throws Error()

    SUT.loadItems()

    assertThat(messagesResult.last().consume()).isEqualTo(R.string.errorCouldNotLoadDiscover)

    job.cancel()
    job2.cancel()
  }
}
