package com.michaldrabik.showly2.ui.followedshows

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsSearchResult
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.EMPTY
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.NO_RESULTS
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.RESULTS
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FollowedShowsViewModel @Inject constructor(
  private val interactor: FollowedShowsInteractor
) : BaseViewModel<FollowedShowsUiModel>() {

  private var searchJob: Job? = null

  var searchViewTranslation = 0F
  var tabsTranslation = 0F

  fun searchFollowedShows(query: String) {
    if (query.trim().isBlank()) {
      searchJob?.cancel()
      val result = MyShowsSearchResult(emptyList(), EMPTY)
      postSearchResult(result)
      return
    }
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
      val results = interactor.searchFollowed(query)
        .map {
          val image = interactor.findCachedImage(it, ImageType.FANART)
          MyShowsListItem(it, image)
        }
      val type = if (results.isEmpty()) NO_RESULTS else RESULTS
      val searchResult = MyShowsSearchResult(results, type)
      postSearchResult(searchResult)
    }
  }

  private fun postSearchResult(searchResult: MyShowsSearchResult) {
    uiState = FollowedShowsUiModel(searchResult = searchResult)
    uiState = FollowedShowsUiModel()
  }

  fun clearCache() = interactor.clearCache()
}
