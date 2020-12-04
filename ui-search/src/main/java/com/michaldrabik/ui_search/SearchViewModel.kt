package com.michaldrabik.ui_search

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config.SEARCH_RECENTS_AMOUNT
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SearchResult
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_search.cases.SearchMainCase
import com.michaldrabik.ui_search.cases.SearchRecentsCase
import com.michaldrabik.ui_search.cases.SearchSuggestionsCase
import com.michaldrabik.ui_search.recycler.SearchListItem
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
  private val searchMainCase: SearchMainCase,
  private val recentSearchesCase: SearchRecentsCase,
  private val suggestionsCase: SearchSuggestionsCase,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider
) : BaseViewModel<SearchUiModel>() {

  private var isSearching = false
  private val lastSearchItems = mutableListOf<SearchListItem>()

  fun preloadCache() {
    viewModelScope.launch {
      suggestionsCase.preloadCache()
    }
  }

  fun loadLastSearch() {
    uiState = SearchUiModel(searchItems = lastSearchItems, searchItemsAnimate = true)
  }

  fun loadRecentSearches() {
    viewModelScope.launch {
      val searches = recentSearchesCase.getRecentSearches(SEARCH_RECENTS_AMOUNT)
      uiState = SearchUiModel(recentSearchItems = searches, isInitial = searches.isEmpty())
    }
  }

  fun loadSuggestions(query: String) {
    viewModelScope.launch {
      if (query.trim().length < 2 || isSearching) {
        uiState = SearchUiModel(suggestionsItems = emptyList())
        return@launch
      }

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
          it.show,
          image,
          movie = it.movie,
          isFollowed = false,
          isWatchlist = false,
          translation = translation
        )
      }

      val results = items.sortedByDescending { it.votes }
      uiState = SearchUiModel(suggestionsItems = results)
    }
  }

  fun clearRecentSearches() {
    viewModelScope.launch {
      recentSearchesCase.clearRecentSearches()
      uiState = SearchUiModel(recentSearchItems = emptyList(), isInitial = true)
    }
  }

  fun search(query: String) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return
    viewModelScope.launch {
      try {
        isSearching = true
        uiState = SearchUiModel.createLoading()

        val results = searchMainCase.searchByQuery(trimmed)
        val myShowsIds = searchMainCase.loadMyShowsIds()
        val watchlistShowsIds = searchMainCase.loadWatchlistShowsIds()
        val myMoviesIds = emptyList<Long>()
        val watchlistMoviesIds = emptyList<Long>()

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
            it.show,
            image,
            movie = it.movie,
            isFollowed = isFollowed,
            isWatchlist = isWatchlist,
            translation = translation
          )
        }

        lastSearchItems.replace(items)
        recentSearchesCase.saveRecentSearch(trimmed)
        uiState = SearchUiModel.createResults(items)
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

  fun loadMissingImage(item: SearchListItem, force: Boolean) {

    fun updateItem(new: SearchListItem) {
      val currentModel = uiState
      val currentItems = currentModel?.searchItems?.toMutableList()
      val currentSuggestions = currentModel?.suggestionsItems?.toMutableList()
      currentItems?.run {
        findReplace(new) { it.isSameAs(new) }
        lastSearchItems.replace(this)
      }
      currentSuggestions?.run {
        findReplace(new) { it.isSameAs(new) }
      }
      uiState = currentModel?.copy(
        searchItems = currentItems,
        suggestionsItems = currentSuggestions,
        searchItemsAnimate = false
      )
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

  private fun onError() {
    uiState = SearchUiModel(isSearching = false, isEmpty = false)
    _messageLiveData.value = MessageEvent.error(R.string.errorCouldNotLoadSearchResults)
  }

  override fun onCleared() {
    suggestionsCase.clearCache()
    super.onCleared()
  }
}
