package com.michaldrabik.ui_search

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config.SEARCH_RECENTS_AMOUNT
import com.michaldrabik.showly2.ui.search.cases.SearchMainCase
import com.michaldrabik.showly2.ui.search.cases.SearchRecentsCase
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
  private val searchMainCase: SearchMainCase,
  private val recentSearchesCase: SearchRecentsCase,
  private val imagesProvider: ShowImagesProvider
) : BaseViewModel<SearchUiModel>() {

  private val lastSearchItems = mutableListOf<SearchListItem>()

  fun loadLastSearch() {
    uiState = SearchUiModel(searchItems = lastSearchItems, searchItemsAnimate = true)
  }

  fun loadRecentSearches() {
    viewModelScope.launch {
      val searches = recentSearchesCase.getRecentSearches(SEARCH_RECENTS_AMOUNT)
      uiState = SearchUiModel(recentSearchItems = searches, isInitial = searches.isEmpty())
    }
  }

  fun clearRecentSearches() {
    viewModelScope.launch {
      recentSearchesCase.clearRecentSearches()
      uiState = SearchUiModel(recentSearchItems = emptyList(), isInitial = true)
    }
  }

  fun searchForShow(query: String) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return
    viewModelScope.launch {
      try {
        uiState = SearchUiModel.createLoading()

        val shows = searchMainCase.searchShows(trimmed)
        val myShowsIds = searchMainCase.loadMyShowsIds()
        val seeLaterShowsIds = searchMainCase.loadSeeLaterShowsIds()

        val items = shows.map {
          val image = imagesProvider.findCachedImage(it, POSTER)
          SearchListItem(
            it,
            image,
            isFollowed = it.ids.trakt.id in myShowsIds,
            isSeeLater = it.ids.trakt.id in seeLaterShowsIds
          )
        }

        lastSearchItems.replace(items)
        recentSearchesCase.saveRecentSearch(trimmed)
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
        findReplace(new) { it.isSameAs(new) }
        lastSearchItems.replace(this)
      }
      uiState = currentModel?.copy(searchItems = currentItems, searchItemsAnimate = false)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
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
}
