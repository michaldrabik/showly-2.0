package com.michaldrabik.ui_search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_model.RecentSearch
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_search.cases.SearchFiltersCase
import com.michaldrabik.ui_search.cases.SearchQueryCase
import com.michaldrabik.ui_search.cases.SearchRecentsCase
import com.michaldrabik.ui_search.cases.SearchSortingCase
import com.michaldrabik.ui_search.cases.SearchSuggestionsCase
import com.michaldrabik.ui_search.cases.SearchTranslationsCase
import com.michaldrabik.ui_search.recycler.SearchListItem
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
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
class SearchViewModelTest : BaseMockTest() {

  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  @MockK lateinit var searchQueryCase: SearchQueryCase
  @MockK lateinit var searchFiltersCase: SearchFiltersCase
  @MockK lateinit var searchSortingCase: SearchSortingCase
  @MockK lateinit var searchTranslationsCase: SearchTranslationsCase
  @MockK lateinit var recentSearchesCase: SearchRecentsCase
  @MockK lateinit var suggestionsCase: SearchSuggestionsCase
  @MockK lateinit var showsImagesProvider: ShowImagesProvider
  @MockK lateinit var moviesImagesProvider: MovieImagesProvider

  private lateinit var SUT: SearchViewModel

  private val stateResult = mutableListOf<SearchUiState>()
  private val messagesResult = mutableListOf<MessageEvent>()

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { searchFiltersCase.isMoviesEnabled } returns true

    SUT = SearchViewModel(
      searchQueryCase,
      searchFiltersCase,
      searchSortingCase,
      searchTranslationsCase,
      recentSearchesCase,
      suggestionsCase,
      showsImagesProvider,
      moviesImagesProvider
    )
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
  fun `Should preload suggestions`() = runBlockingTest {
    SUT.preloadSuggestions()
    coVerify(exactly = 1) { suggestionsCase.preloadCache() }
  }

  @Test
  fun `Should load recent searches`() = runBlockingTest {
    val recentSearchItem = RecentSearch("text")
    coEvery { recentSearchesCase.getRecentSearches(any()) } returns listOf(recentSearchItem)
    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.loadRecentSearches()

    with(stateResult.last()) {
      assertThat(recentSearchItems).hasSize(1)
      assertThat(recentSearchItems).contains(recentSearchItem)
      assertThat(isInitial).isFalse()
    }

    coVerify(exactly = 1) { recentSearchesCase.getRecentSearches(5) }
    confirmVerified(recentSearchesCase)

    job.cancel()
  }

  @Test
  fun `Should clear recent searches`() = runBlockingTest {
    coEvery { recentSearchesCase.clearRecentSearches() } just Runs
    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.clearRecentSearches()

    with(stateResult.last()) {
      assertThat(recentSearchItems).isEmpty()
      assertThat(isInitial).isTrue()
    }

    coVerify(exactly = 1) { recentSearchesCase.clearRecentSearches() }
    confirmVerified(recentSearchesCase)

    job.cancel()
  }

  @Test
  fun `Should not run search if query is blank`() = runBlockingTest {
    SUT.search("  ")
    coVerify(exactly = 0) { searchQueryCase.searchByQuery(any()) }
  }

  @Test
  fun `Should not store recent search if query is blank`() = runBlockingTest {
    SUT.saveRecentSearch("   ")
    coVerify(exactly = 0) { recentSearchesCase.saveRecentSearch(any()) }
  }

  @Test
  fun `Should store recent search if query is not blank`() = runBlockingTest {
    coEvery { recentSearchesCase.saveRecentSearch(any()) } just Runs
    SUT.saveRecentSearch("test ")
    coVerify(exactly = 1) { recentSearchesCase.saveRecentSearch(any()) }
  }

  @Test
  fun `Should not update filters if they are the same`() = runBlockingTest {
    val item = mockk<SearchListItem>()
    coEvery { searchQueryCase.searchByQuery(any()) } returns listOf(item)
    coEvery { searchFiltersCase.filter(any(), any()) } returns true
    coEvery { searchSortingCase.sort(any()) } returns compareBy { it.id }

    SUT.search("test")
    SUT.setFilters(listOf(Mode.SHOWS))

    // Second call should not induce additional filtering and sorting
    SUT.setFilters(listOf(Mode.SHOWS))

    coVerify(exactly = 1) { searchFiltersCase.filter(any(), any()) }
    coVerify(exactly = 1) { searchSortingCase.sort(any()) }
  }

  @Test
  fun `Should update filters`() = runBlockingTest {
    val item = mockk<SearchListItem>()
    coEvery { searchQueryCase.searchByQuery(any()) } returns listOf(item)
    coEvery { searchFiltersCase.filter(any(), any()) } returns true
    coEvery { searchSortingCase.sort(any()) } returns compareBy { it.id }

    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.search("test")
    SUT.setFilters(listOf(Mode.SHOWS))

    with(stateResult.last()) {
      assertThat(searchItems).containsExactly(item)
      assertThat(searchOptions?.filters).containsExactly(Mode.SHOWS)
      assertThat(resetScroll?.consume()).isTrue()
    }
    coVerify(exactly = 1) { searchFiltersCase.filter(any(), any()) }
    coVerify(exactly = 1) { searchSortingCase.sort(any()) }

    job.cancel()
  }

  @Test
  fun `Should not update sort order if they are the same`() = runBlockingTest {
    val item = mockk<SearchListItem>()
    coEvery { searchQueryCase.searchByQuery(any()) } returns listOf(item)
    coEvery { searchFiltersCase.filter(any(), any()) } returns true
    coEvery { searchSortingCase.sort(any()) } returns compareBy { it.id }

    SUT.search("test")
    SUT.setSortOrder(SortOrder.NEWEST, SortType.DESCENDING)

    // Second call should not induce additional filtering and sorting
    SUT.setSortOrder(SortOrder.NEWEST, SortType.DESCENDING)

    coVerify(exactly = 1) { searchFiltersCase.filter(any(), any()) }
    coVerify(exactly = 1) { searchSortingCase.sort(any()) }
  }

  @Test
  fun `Should update sort order`() = runBlockingTest {
    val item = mockk<SearchListItem>()
    coEvery { searchQueryCase.searchByQuery(any()) } returns listOf(item)
    coEvery { searchFiltersCase.filter(any(), any()) } returns true
    coEvery { searchSortingCase.sort(any()) } returns compareBy { it.id }

    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.search("test")
    SUT.setSortOrder(SortOrder.NEWEST, SortType.DESCENDING)

    with(stateResult.last()) {
      assertThat(searchItems).containsExactly(item)
      assertThat(searchOptions?.sortOrder).isEqualTo(SortOrder.NEWEST)
      assertThat(searchOptions?.sortType).isEqualTo(SortType.DESCENDING)
      assertThat(resetScroll?.consume()).isTrue()
    }
    coVerify(exactly = 1) { searchFiltersCase.filter(any(), any()) }
    coVerify(exactly = 1) { searchSortingCase.sort(any()) }

    job.cancel()
  }

  @Test
  fun `Should load sort order properly`() = runBlockingTest {
    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.setSortOrder(SortOrder.NEWEST, SortType.DESCENDING)
    SUT.loadSortOrder()

    with(stateResult.last()) {
      val event = sortOrder?.consume()
      assertThat(event?.first).isEqualTo(SortOrder.NEWEST)
      assertThat(event?.second).isEqualTo(SortType.DESCENDING)
    }

    job.cancel()
  }

  @Test
  fun `Should clear suggestions properly`() = runBlockingTest {
    val item = mockk<SearchListItem>()
    coEvery { suggestionsCase.loadSuggestions(any()) } returns listOf(item)

    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.loadSuggestions("test")
    SUT.clearSuggestions()

    assertThat(stateResult[1].suggestionsItems).containsExactly(item)
    assertThat(stateResult[2].suggestionsItems).isEmpty()

    job.cancel()
  }

  @Test
  fun `Should not load suggestions if query length is less than 2`() = runBlockingTest {
    val item = mockk<SearchListItem>()
    coEvery { suggestionsCase.loadSuggestions(any()) } returns listOf(item)

    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.loadSuggestions("x")
    assertThat(stateResult[1].suggestionsItems).isEmpty()

    SUT.loadSuggestions("xx")
    assertThat(stateResult[2].suggestionsItems).containsExactly(item)

    job.cancel()
  }

  @Test
  fun `Should load suggestions properly`() = runBlockingTest {
    val item = mockk<SearchListItem>()
    coEvery { suggestionsCase.loadSuggestions(any()) } returns listOf(item, item)

    val job = launch { SUT.uiState.toList(stateResult) }

    SUT.loadSuggestions("xxxxx")
    assertThat(stateResult[1].suggestionsItems).hasSize(2)

    job.cancel()
  }
}
