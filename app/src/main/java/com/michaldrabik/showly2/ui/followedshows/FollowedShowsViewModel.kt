package com.michaldrabik.showly2.ui.followedshows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsSearchResult
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class FollowedShowsViewModel @Inject constructor(
  private val interactor: FollowedShowsInteractor,
  private val uiCache: UiCache
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<FollowedShowsUiModel>() }
  private lateinit var searchJob: Job

  fun searchMyShows(query: String) {
    if (query.trim().isBlank()) {
      if (this::searchJob.isInitialized) searchJob.cancel()
      val result = MyShowsSearchResult(emptyList(), ResultType.EMPTY)
      uiStream.value = FollowedShowsUiModel(searchResult = result)
      return
    }
    if (this::searchJob.isInitialized) searchJob.cancel()
    searchJob = viewModelScope.launch {
      val results = interactor.searchMyShows(query)
        .map {
          val image = interactor.findCachedImage(it, ImageType.FANART)
          MyShowsListItem(it, image)
        }
      val type = if (results.isEmpty()) ResultType.NO_RESULTS else ResultType.RESULTS
      val searchResult = MyShowsSearchResult(results, type)
      uiStream.value = FollowedShowsUiModel(searchResult = searchResult)
    }
  }

  fun clearCache() = interactor.clearCache()
}
