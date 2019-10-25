package com.michaldrabik.showly2.ui.followedshows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsSearchResult
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.EMPTY
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.NO_RESULTS
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.RESULTS
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class FollowedShowsViewModel @Inject constructor(
  private val interactor: FollowedShowsInteractor
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<FollowedShowsUiModel>() }
  private var searchJob: Job? = null

  fun searchMyShows(query: String) {
    if (query.trim().isBlank()) {
      searchJob?.cancel()
      val result = MyShowsSearchResult(emptyList(), EMPTY)
      uiStream.value = FollowedShowsUiModel(searchResult = result)
      return
    }
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
      val results = interactor.searchMyShows(query)
        .map {
          val image = interactor.findCachedImage(it, ImageType.FANART)
          MyShowsListItem(it, image)
        }
      val type = if (results.isEmpty()) NO_RESULTS else RESULTS
      val searchResult = MyShowsSearchResult(results, type)
      uiStream.value = FollowedShowsUiModel(searchResult = searchResult)
    }
  }

  fun clearCache() = interactor.clearCache()
}
