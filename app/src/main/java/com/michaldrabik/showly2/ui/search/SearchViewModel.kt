package com.michaldrabik.showly2.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
  private val interactor: SearchInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<SearchUiModel>() }

  private val lastItems = mutableListOf<SearchListItem>()

  fun loadLastSearch() {
    uiStream.value = SearchUiModel(lastItems)
  }

  fun loadRecentSearches() {
    viewModelScope.launch {
      val searches = interactor.getRecentSearches(5)
      uiStream.value = SearchUiModel(recentSearchItems = searches)
    }
  }

  fun clearRecentSearches() {
    viewModelScope.launch {
      interactor.clearRecentSearches()
      uiStream.value = SearchUiModel(recentSearchItems = emptyList())
    }
  }

  fun searchForShow(query: String) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return
    viewModelScope.launch {
      try {
        uiStream.value = SearchUiModel(emptyList(), emptyList(), isSearching = true, isEmpty = false)
        val shows = interactor.searchShows(trimmed)
        val items = shows.map {
          val image = interactor.findCachedImage(it, ImageType.POSTER)
          SearchListItem(it, image)
        }
        lastItems.clear()
        lastItems.addAll(items)
        uiStream.value = SearchUiModel(items, isSearching = false, isEmpty = items.isEmpty())
      } catch (t: Throwable) {
        onError(t)
      }
    }
  }

  fun loadMissingImage(item: SearchListItem, force: Boolean) {
    viewModelScope.launch {
      uiStream.value = SearchUiModel(updateListItem = item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        uiStream.value =
          SearchUiModel(updateListItem = item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        uiStream.value =
          SearchUiModel(updateListItem = item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  private fun onError(t: Throwable) {
    uiStream.value = SearchUiModel(error = Error(t), isSearching = false, isEmpty = false)
  }
}
