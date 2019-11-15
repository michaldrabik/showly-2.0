package com.michaldrabik.showly2.ui.search

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.Config.SEARCH_RECENTS_AMOUNT
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import com.michaldrabik.showly2.utilities.extensions.replace
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
  private val interactor: SearchInteractor
) : BaseViewModel<SearchUiModel>() {

  private val lastSearchItems = mutableListOf<SearchListItem>()

  fun loadLastSearch() {
    uiState = SearchUiModel(searchItems = lastSearchItems, searchItemsAnimate = true)
  }

  fun loadRecentSearches() {
    viewModelScope.launch {
      val searches = interactor.getRecentSearches(SEARCH_RECENTS_AMOUNT)
      uiState = SearchUiModel(recentSearchItems = searches, isInitial = searches.isEmpty())
    }
  }

  fun clearRecentSearches() {
    viewModelScope.launch {
      interactor.clearRecentSearches()
      uiState = SearchUiModel(recentSearchItems = emptyList(), isInitial = true)
    }
  }

  fun searchForShow(query: String) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return
    viewModelScope.launch {
      try {
        uiState = SearchUiModel.createLoading()
        val shows = interactor.searchShows(trimmed)
        val myShowsIds = interactor.loadMyShowsIds()
        val items = shows.map {
          val image = interactor.findCachedImage(it, POSTER)
          SearchListItem(it, image, isFollowed = it.ids.trakt.id in myShowsIds)
        }
        lastSearchItems.replace(items)
        uiState = SearchUiModel.createResults(items)
      } catch (t: Throwable) {
        onError()
      }
    }
  }

  fun loadMissingImage(item: SearchListItem, force: Boolean) {

    fun updateItem(new: SearchListItem) {
      val currentModel = uiState
      val currentItems = currentModel?.searchItems?.toMutableList()
      currentItems?.run {
        findReplace(new) { it.show.ids.trakt == new.show.ids.trakt }
        lastSearchItems.replace(this)
      }
      uiState = currentModel?.copy(searchItems = currentItems, searchItemsAnimate = false)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  private fun onError() {
    uiState = SearchUiModel(isSearching = false, isEmpty = false)
    _errorStream.value = R.string.errorCouldNotLoadSearchResults
  }
}
