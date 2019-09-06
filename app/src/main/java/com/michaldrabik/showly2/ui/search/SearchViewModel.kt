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
      return
    }

    uiStream.value = SearchUiModel(isSearching = true)
    viewModelScope.launch {
      val shows = interactor.searchForShow(query)
      val items = shows.map { SearchListItem(it, Image.createUnknown(ImageType.POSTER)) }
      uiStream.value = SearchUiModel(items, isSearching = false)
    }
  }

}
