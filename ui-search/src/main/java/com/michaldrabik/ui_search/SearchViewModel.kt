package com.michaldrabik.ui_search

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.SEARCH_RECENTS_AMOUNT
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RecentSearch
import com.michaldrabik.ui_model.SearchResult
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_search.cases.SearchMainCase
import com.michaldrabik.ui_search.cases.SearchRecentsCase
import com.michaldrabik.ui_search.cases.SearchSuggestionsCase
import com.michaldrabik.ui_search.recycler.SearchListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
  private val searchMainCase: SearchMainCase,
  private val recentSearchesCase: SearchRecentsCase,
  private val suggestionsCase: SearchSuggestionsCase,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
) : BaseViewModel() {

  private val searchItemsState = MutableStateFlow<List<SearchListItem>?>(null)
  private val searchItemsAnimateEvent = MutableStateFlow<ActionEvent<Boolean>?>(null)
  private val recentSearchItemsState = MutableStateFlow<List<RecentSearch>?>(null)
  private val suggestionsItemsState = MutableStateFlow<List<SearchListItem>?>(null)
  private val searchingState = MutableStateFlow(false)
  private val emptyState = MutableStateFlow(false)
  private val initialState = MutableStateFlow(false)

  val uiState = combine(
    searchItemsState,
    searchItemsAnimateEvent,
    recentSearchItemsState,
    suggestionsItemsState,
    searchingState,
    emptyState,
    initialState
  ) { s1, s2, s3, s4, s5, s6, s7 ->
    SearchUiState(
      searchItems = s1,
      searchItemsAnimate = s2,
      recentSearchItems = s3,
      suggestionsItems = s4,
      isSearching = s5,
      isEmpty = s6,
      isInitial = s7
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = SearchUiState()
  )

  private var isSearching = false
  private var suggestionsJob: Job? = null

  fun preloadSuggestions() {
    viewModelScope.launch {
      suggestionsCase.preloadCache()
    }
  }

  fun loadRecentSearches() {
    viewModelScope.launch {
      val searches = recentSearchesCase.getRecentSearches(SEARCH_RECENTS_AMOUNT)
      recentSearchItemsState.value = searches
      initialState.value = searches.isEmpty()
    }
  }

  fun clearRecentSearches() {
    viewModelScope.launch {
      recentSearchesCase.clearRecentSearches()
      recentSearchItemsState.value = emptyList()
      initialState.value = true
    }
  }

  fun search(query: String) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return
    viewModelScope.launch {
      try {
        isSearching = true

        searchItemsState.value = emptyList()
        searchItemsAnimateEvent.value = ActionEvent(false)
        recentSearchItemsState.value = emptyList()
        searchingState.value = true
        emptyState.value = false
        initialState.value = false
        suggestionsItemsState.value = emptyList()

        val results = searchMainCase.searchByQuery(trimmed)
        val myShowsIds = searchMainCase.loadMyShowsIds()
        val watchlistShowsIds = searchMainCase.loadWatchlistShowsIds()
        val myMoviesIds = searchMainCase.loadMyMoviesIds()
        val watchlistMoviesIds = searchMainCase.loadWatchlistMoviesIds()

        val items = results.map {
          val translation = searchMainCase.loadTranslation(it)

          val image =
            if (it.isShow) showsImagesProvider.findCachedImage(it.show, POSTER)
            else moviesImagesProvider.findCachedImage(it.movie, POSTER)

          val isFollowed =
            if (it.isShow) it.traktId in myShowsIds
            else it.traktId in myMoviesIds

          val isWatchlist =
            if (it.isShow) it.traktId in watchlistShowsIds
            else it.traktId in watchlistMoviesIds

          SearchListItem(
            UUID.randomUUID(),
            it.show,
            image,
            movie = it.movie,
            isFollowed = isFollowed,
            isWatchlist = isWatchlist,
            translation = translation
          )
        }

        recentSearchesCase.saveRecentSearch(trimmed)

        searchItemsState.value = items
        searchItemsAnimateEvent.value = ActionEvent(true)
        searchingState.value = false
        emptyState.value = items.isEmpty()
        initialState.value = false
        suggestionsItemsState.value = emptyList()
      } catch (t: Throwable) {
        onError()
      } finally {
        isSearching = false
      }
    }
  }

  fun saveRecentSearch(query: String) {
    if (query.trim().isBlank()) return
    viewModelScope.launch {
      recentSearchesCase.saveRecentSearch(query)
    }
  }

  fun clearSuggestions() {
    suggestionsItemsState.value = emptyList()
  }

  fun loadSuggestions(query: String) {
    suggestionsJob?.cancel()

    if (query.trim().length < 2 || isSearching) {
      clearSuggestions()
      return
    }

    suggestionsJob = viewModelScope.launch {
      val showsDef = async { suggestionsCase.loadShows(query.trim(), 5) }
      val moviesDef = async { suggestionsCase.loadMovies(query.trim(), 5) }
      val suggestions = (showsDef.await() + moviesDef.await()).map {
        when (it) {
          is Show -> SearchResult(0F, it, Movie.EMPTY)
          is Movie -> SearchResult(0F, Show.EMPTY, it)
          else -> throw IllegalStateException()
        }
      }

      val items = suggestions.map {
        val image =
          if (it.isShow) showsImagesProvider.findCachedImage(it.show, POSTER)
          else moviesImagesProvider.findCachedImage(it.movie, POSTER)
        val translation = searchMainCase.loadTranslation(it)
        SearchListItem(
          UUID.randomUUID(),
          it.show,
          image,
          movie = it.movie,
          isFollowed = false,
          isWatchlist = false,
          translation = translation
        )
      }

      val results = items.sortedByDescending { it.votes }
      suggestionsItemsState.value = results
    }
  }

  fun loadMissingImage(item: SearchListItem, force: Boolean) {

    fun updateItem(new: SearchListItem) {
      val currentItems = uiState.value.searchItems?.toMutableList()
      currentItems?.run {
        findReplace(new) { it.isSameAs(new) }
      }
      searchItemsState.value = currentItems
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image =
          if (item.isShow) showsImagesProvider.loadRemoteImage(item.show, item.image.type, force)
          else moviesImagesProvider.loadRemoteImage(item.movie, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun loadMissingSuggestionImage(item: SearchListItem, force: Boolean) {
    viewModelScope.launch {
      updateSuggestionsItem(item.copy(isLoading = true))
      try {
        val image =
          if (item.isShow) showsImagesProvider.loadRemoteImage(item.show, item.image.type, force)
          else moviesImagesProvider.loadRemoteImage(item.movie, item.image.type, force)
        updateSuggestionsItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateSuggestionsItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun loadMissingSuggestionTranslation(item: SearchListItem) {
    if (item.translation != null || searchMainCase.language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation =
          if (item.isShow) searchMainCase.loadTranslation(item.show)
          else searchMainCase.loadTranslation(item.movie)
        updateSuggestionsItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "SearchViewModel::loadMissingTranslation()")
      }
    }
  }

  private fun updateSuggestionsItem(new: SearchListItem) {
    val currentState = uiState
    val currentItems = currentState.value.suggestionsItems?.toMutableList()
    currentItems?.run { findReplace(new) { it.isSameAs(new) } }
    suggestionsItemsState.value = currentItems
  }

  private suspend fun onError() {
    searchingState.value = false
    emptyState.value = false
    _messageState.emit(MessageEvent.error(R.string.errorCouldNotLoadSearchResults))
  }

  override fun onCleared() {
    suggestionsCase.clearCache()
    super.onCleared()
  }
}
