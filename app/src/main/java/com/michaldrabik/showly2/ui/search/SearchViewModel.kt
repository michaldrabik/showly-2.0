package com.michaldrabik.showly2.ui.search

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.Config.SEARCH_RECENTS_AMOUNT
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import com.michaldrabik.showly2.utilities.extensions.replace
import com.michaldrabik.showly2.utilities.extensions.replaceItem
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
  private val interactor: SearchInteractor
) : BaseViewModel<SearchUiModel>() {

  private val lastSearchItems = mutableListOf<SearchListItem>()

  fun loadLastSearch() {
    _uiStream.value = SearchUiModel(searchItems = lastSearchItems, searchItemsAnimate = true)
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
        _uiStream.value = SearchUiModel.createLoading()
        val shows = interactor.searchShows(trimmed)
        val myShowsIds = interactor.loadMyShowsIds()
        val items = shows.map {
          val image = interactor.findCachedImage(it, POSTER)
          SearchListItem(it, image, isFollowed = it.ids.trakt.id in myShowsIds)
        }
        lastSearchItems.replace(items)
        _uiStream.value = SearchUiModel.createResults(items)
      } catch (t: Throwable) {
        onError(t)
      }
    }
  }

  fun loadMissingImage(item: SearchListItem, force: Boolean) {

    fun updateItem(new: SearchListItem) {
      val currentModel = _uiStream.value
      val currentItems = currentModel?.searchItems?.toMutableList()
      currentItems?.let { items ->
        items.find { it.show.ids.trakt == new.show.ids.trakt }?.let {
          items.replaceItem(it, new)
        }
        lastSearchItems.replace(currentItems)
      }
      _uiStream.value = currentModel?.copy(searchItems = currentItems, searchItemsAnimate = false)
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

  private fun onError(t: Throwable) {
    _uiStream.value = SearchUiModel(error = Error(t), isSearching = false, isEmpty = false)
  }
}
