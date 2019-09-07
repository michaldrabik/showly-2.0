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

  fun searchForShow(query: String) {
    uiStream.value = SearchUiModel(emptyList())

    if (query.trim().isEmpty()) {
      //TODO
      return
    }

    uiStream.value = SearchUiModel(isSearching = true)
    viewModelScope.launch {
      val shows = interactor.searchShows(query)
      val items = shows.map {
        val image = interactor.findCachedImage(it, ImageType.POSTER)
        SearchListItem(it, image)
      }
      uiStream.value = SearchUiModel(items, isSearching = false)
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

}
