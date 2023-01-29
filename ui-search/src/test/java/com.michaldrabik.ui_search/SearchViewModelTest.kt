package com.michaldrabik.ui_search

import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.common.Mode
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RecentSearch
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_search.cases.SearchFiltersCase
import com.michaldrabik.ui_search.cases.SearchInvalidateItemCase
import com.michaldrabik.ui_search.cases.SearchQueryCase
import com.michaldrabik.ui_search.cases.SearchRecentsCase
import com.michaldrabik.ui_search.cases.SearchSortingCase
import com.michaldrabik.ui_search.cases.SearchSuggestionsCase
import com.michaldrabik.ui_search.cases.SearchTranslationsCase
import com.michaldrabik.ui_search.helpers.TestData
import com.michaldrabik.ui_search.recycler.SearchListItem
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
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
@Suppress("EXPERIMENTAL_API_USAGE")
class SearchViewModelTest : BaseMockTest() {

  @MockK lateinit var searchQueryCase: SearchQueryCase
  @MockK lateinit var searchFiltersCase: SearchFiltersCase
  @MockK lateinit var searchSortingCase: SearchSortingCase
  @MockK lateinit var searchInvalidateCase: SearchInvalidateItemCase
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
      searchInvalidateCase,
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
  }

  @Test
  fun `Should preload suggestions`() = runTest {
    coEvery { suggestionsCase.preloadCache() } just Runs
    SUT.preloadSuggestions()
    coVerify(exactly = 1) { suggestionsCase.preloadCache() }
  }

  @Test
  fun `Should load recent searches`() = runTest {
    val recentSearchItem = RecentSearch("text")
    coEvery { recentSearchesCase.getRecentSearches(any()) } returns listOf(recentSearchItem)
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

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
  fun `Should clear recent searches`() = runTest {
    coEvery { recentSearchesCase.clearRecentSearches() } just Runs
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

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
  fun `Should not run search if query is blank`() = runTest {
    SUT.search("  ")
    coVerify(exactly = 0) { searchQueryCase.searchByQuery(any()) }
  }

  @Test
  fun `Should not store recent search if query is blank`() = runTest {
    SUT.saveRecentSearch("   ")
    coVerify(exactly = 0) { recentSearchesCase.saveRecentSearch(any()) }
  }

  @Test
  fun `Should store recent search if query is not blank`() = runTest {
    coEvery { recentSearchesCase.saveRecentSearch(any()) } just Runs
    SUT.saveRecentSearch("test ")
    coVerify(exactly = 1) { recentSearchesCase.saveRecentSearch(any()) }
  }

  @Test
  fun `Should not update filters if they are the same`() = runTest {
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
  fun `Should update filters`() = runTest {
    val item = mockk<SearchListItem>()
    coEvery { searchQueryCase.searchByQuery(any()) } returns listOf(item)
    coEvery { searchFiltersCase.filter(any(), any()) } returns true
    coEvery { searchSortingCase.sort(any()) } returns compareBy { it.id }

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

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
  fun `Should not update sort order if they are the same`() = runTest {
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
  fun `Should update sort order`() = runTest {
    val item = mockk<SearchListItem>()
    coEvery { searchQueryCase.searchByQuery(any()) } returns listOf(item)
    coEvery { searchFiltersCase.filter(any(), any()) } returns true
    coEvery { searchSortingCase.sort(any()) } returns compareBy { it.id }

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

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
  fun `Should clear suggestions properly`() = runTest {
    val item = mockk<SearchListItem>()
    coEvery { suggestionsCase.loadSuggestions(any()) } returns listOf(item)

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadSuggestions("test")
    SUT.clearSuggestions()

    assertThat(stateResult[2].suggestionsItems).containsExactly(item)
    assertThat(stateResult[3].suggestionsItems).isEmpty()

    job.cancel()
  }

  @Test
  fun `Should not load suggestions if query length is less than 2`() = runTest {
    val item = mockk<SearchListItem>()
    coEvery { suggestionsCase.loadSuggestions(any()) } returns listOf(item)

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadSuggestions("x")
    assertThat(stateResult[2].suggestionsItems).isEmpty()

    SUT.loadSuggestions("xx")
    assertThat(stateResult[3].suggestionsItems).containsExactly(item)

    job.cancel()
  }

  @Test
  fun `Should load suggestions properly`() = runTest {
    val item = mockk<SearchListItem>()

    coEvery { suggestionsCase.loadSuggestions(any()) } returns listOf(item, item)

    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadSuggestions("xxxxx")

    assertThat(stateResult.last().suggestionsItems).hasSize(2)

    job.cancel()
  }

  @Test
  fun `Should update missing image for show properly`() = runTest {
    val item = TestData.SEARCH_LIST_ITEM.copy(
      show = Show.EMPTY.copy(title = "test")
    )
    coEvery { showsImagesProvider.loadRemoteImage(any(), any(), any()) } returns Image.createUnavailable(ImageType.POSTER)
    coEvery { searchQueryCase.searchByQuery(any()) } returns listOf(item)
    coEvery { recentSearchesCase.saveRecentSearch(any()) } just Runs
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.search("test")
    SUT.loadMissingImage(item, true)

    with(stateResult.last()) {
      assertThat(searchItems).hasSize(1)
      assertThat(searchItems?.last()?.image?.status).isEqualTo(ImageStatus.UNAVAILABLE)
    }

    coVerify(exactly = 1) { showsImagesProvider.loadRemoteImage(any(), any(), any()) }
    coVerify { moviesImagesProvider wasNot Called }

    job.cancel()
  }

  @Test
  fun `Should update missing image for movie properly`() = runTest {
    val item = TestData.SEARCH_LIST_ITEM.copy(
      movie = Movie.EMPTY.copy(title = "test")
    )
    coEvery { moviesImagesProvider.loadRemoteImage(any(), any(), any()) } returns Image.createUnavailable(ImageType.POSTER)
    coEvery { searchQueryCase.searchByQuery(any()) } returns listOf(item)
    coEvery { recentSearchesCase.saveRecentSearch(any()) } just Runs
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.search("test")
    SUT.loadMissingImage(item, true)

    with(stateResult.last()) {
      assertThat(searchItems).hasSize(1)
      assertThat(searchItems?.last()?.image?.status).isEqualTo(ImageStatus.UNAVAILABLE)
    }

    coVerify(exactly = 1) { moviesImagesProvider.loadRemoteImage(any(), any(), any()) }
    coVerify { showsImagesProvider wasNot Called }

    job.cancel()
  }

  @Test
  fun `Should update missing suggestion image for show properly`() = runTest {
    val item = TestData.SEARCH_LIST_ITEM.copy(
      show = Show.EMPTY.copy(title = "test")
    )
    coEvery { showsImagesProvider.loadRemoteImage(any(), any(), any()) } returns Image.createUnavailable(ImageType.POSTER)
    coEvery { suggestionsCase.loadSuggestions((any())) } returns listOf(item)
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadSuggestions("test")
    SUT.loadMissingSuggestionImage(item, true)

    with(stateResult.last()) {
      assertThat(suggestionsItems).hasSize(1)
      assertThat(suggestionsItems?.last()?.image?.status).isEqualTo(ImageStatus.UNAVAILABLE)
    }

    coVerify(exactly = 1) { showsImagesProvider.loadRemoteImage(any(), any(), any()) }
    coVerify { moviesImagesProvider wasNot Called }

    job.cancel()
  }

  @Test
  fun `Should update missing suggestion image for movie properly`() = runTest {
    val item = TestData.SEARCH_LIST_ITEM.copy(
      movie = Movie.EMPTY.copy(title = "test")
    )
    coEvery { showsImagesProvider.loadRemoteImage(any(), any(), any()) } returns Image.createUnavailable(ImageType.POSTER)
    coEvery { suggestionsCase.loadSuggestions((any())) } returns listOf(item)
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadSuggestions("test")
    SUT.loadMissingSuggestionImage(item, true)

    with(stateResult.last()) {
      assertThat(suggestionsItems).hasSize(1)
      assertThat(suggestionsItems?.last()?.image?.status).isEqualTo(ImageStatus.UNAVAILABLE)
    }

    coVerify(exactly = 1) { moviesImagesProvider.loadRemoteImage(any(), any(), any()) }
    coVerify { showsImagesProvider wasNot Called }

    job.cancel()
  }

  @Test
  fun `Should update missing suggestion translation for show properly`() = runTest {
    val item = TestData.SEARCH_LIST_ITEM.copy(
      show = Show.EMPTY.copy(title = "test")
    )
    coEvery { searchTranslationsCase.getLanguage() } returns "pl"
    coEvery { searchTranslationsCase.loadTranslation(any<Show>()) } returns Translation.EMPTY
    coEvery { suggestionsCase.loadSuggestions((any())) } returns listOf(item)
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadSuggestions("test")
    SUT.loadMissingSuggestionTranslation(item)

    with(stateResult.last()) {
      assertThat(suggestionsItems).hasSize(1)
      assertThat(suggestionsItems?.last()?.translation).isEqualTo(Translation.EMPTY)
    }

    coVerify(exactly = 1) { searchTranslationsCase.loadTranslation(any<Show>()) }
    coVerify(exactly = 0) { searchTranslationsCase.loadTranslation(any<Movie>()) }

    job.cancel()
  }

  @Test
  fun `Should update missing suggestion translation for movie properly`() = runTest {
    val item = TestData.SEARCH_LIST_ITEM.copy(
      movie = Movie.EMPTY.copy(title = "test")
    )
    coEvery { searchTranslationsCase.getLanguage() } returns "pl"
    coEvery { searchTranslationsCase.loadTranslation(any<Movie>()) } returns Translation.EMPTY
    coEvery { suggestionsCase.loadSuggestions((any())) } returns listOf(item)
    val job = launch(UnconfinedTestDispatcher()) { SUT.uiState.toList(stateResult) }

    SUT.loadSuggestions("test")
    SUT.loadMissingSuggestionTranslation(item)

    with(stateResult.last()) {
      assertThat(suggestionsItems).hasSize(1)
      assertThat(suggestionsItems?.last()?.translation).isEqualTo(Translation.EMPTY)
    }

    coVerify(exactly = 1) { searchTranslationsCase.loadTranslation(any<Movie>()) }
    coVerify(exactly = 0) { searchTranslationsCase.loadTranslation(any<Show>()) }

    job.cancel()
  }

  @Test
  fun `Should not update missing suggestion translation if default language`() = runTest {
    coEvery { searchTranslationsCase.getLanguage() } returns "en"
    val item = TestData.SEARCH_LIST_ITEM

    SUT.loadMissingSuggestionTranslation(item)

    coVerify(exactly = 0) { searchTranslationsCase.loadTranslation(any<Movie>()) }
    coVerify(exactly = 0) { searchTranslationsCase.loadTranslation(any<Show>()) }
  }

  @Test
  fun `Should not update missing suggestion translation if already has translation`() = runTest {
    coEvery { searchTranslationsCase.getLanguage() } returns "pl"
    val item = TestData.SEARCH_LIST_ITEM.copy(translation = Translation.EMPTY)

    SUT.loadMissingSuggestionTranslation(item)

    coVerify(exactly = 0) { searchTranslationsCase.loadTranslation(any<Movie>()) }
    coVerify(exactly = 0) { searchTranslationsCase.loadTranslation(any<Show>()) }
  }
}
