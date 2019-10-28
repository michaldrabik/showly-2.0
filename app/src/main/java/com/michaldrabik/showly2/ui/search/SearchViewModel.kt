package com.michaldrabik.showly2.ui.search

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.Config.SEARCH_RECENTS_AMOUNT
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
  private val interactor: SearchInteractor
) : BaseViewModel<SearchUiModel>() {

  private val lastItems = mutableListOf<SearchListItem>()

  fun loadLastSearch() {
    _uiStream.value = SearchUiModel(lastItems)
  }

  fun loadRecentSearches() {
    viewModelScope.launch {
      val searches = interactor.getRecentSearches(SEARCH_RECENTS_AMOUNT)
      _uiStream.value = SearchUiModel(recentSearchItems = searches, isInitial = searches.isEmpty())
    }
  }

  fun clearRecentSearches() {
    viewModelScope.launch {
      interactor.clearRecentSearches()
      _uiStream.value = SearchUiModel(recentSearchItems = emptyList(), isInitial = true)
    }
  }

  fun searchForShow(query: String) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return
    viewModelScope.launch {
      try {
        _uiStream.value = SearchUiModel(emptyList(), emptyList(), isSearching = true, isEmpty = false, isInitial = false)
        val shows = interactor.searchShows(trimmed)
        val items = shows.map {
          val image = interactor.findCachedImage(it, ImageType.POSTER)
          SearchListItem(it, image)
        }
        lastItems.clear()
        lastItems.addAll(items)
        _uiStream.value = SearchUiModel(items, isSearching = false, isEmpty = items.isEmpty(), isInitial = false)
      } catch (t: Throwable) {
        onError(t)
      }
    }
  }

  fun loadMissingImage(item: SearchListItem, force: Boolean) {
    viewModelScope.launch {
      _uiStream.value = SearchUiModel(updateListItem = item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        _uiStream.value =
          SearchUiModel(updateListItem = item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        _uiStream.value =
          SearchUiModel(updateListItem = item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  private fun onError(t: Throwable) {
    _uiStream.value = SearchUiModel(error = Error(t), isSearching = false, isEmpty = false)
    Log.e("SearchViewModel", t.message ?: "")
  }
}
