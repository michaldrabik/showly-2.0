package com.michaldrabik.showly2.ui.discover

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.DiscoverSortOrder
import com.michaldrabik.showly2.model.DiscoverSortOrder.HOT
import com.michaldrabik.showly2.model.DiscoverSortOrder.NEWEST
import com.michaldrabik.showly2.model.DiscoverSortOrder.RATING
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.FANART_WIDE
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
  private val interactor: DiscoverInteractor
) : BaseViewModel<DiscoverUiModel>() {

  private var lastPullToRefreshMs = 0L

  fun loadDiscoverShows(pullToRefresh: Boolean = false, scrollToTop: Boolean = false) {
    if (pullToRefresh && nowUtcMillis() - lastPullToRefreshMs < Config.PULL_TO_REFRESH_COOLDOWN_MS) {
      uiState = DiscoverUiModel(showLoading = false)
      return
    }

    uiState = DiscoverUiModel(showLoading = pullToRefresh)

    viewModelScope.launch {
      val progressJob = launch {
        delay(if (pullToRefresh) 0 else 750)
        uiState = DiscoverUiModel(showLoading = true)
      }

      try {
        val myShowsIds = interactor.loadMyShowsIds()
        val seeLaterShowsIds = interactor.loadSeeLaterShowsIds()

        if (!pullToRefresh) {
          val cachedShows = interactor.loadCachedShows()
          onShowsLoaded(cachedShows, myShowsIds, seeLaterShowsIds, scrollToTop)
        }

        if (pullToRefresh || !interactor.isCacheValid()) {
          checkTvdbAuth()
          val remoteShows = interactor.loadRemoteShows()
          onShowsLoaded(remoteShows, myShowsIds, seeLaterShowsIds, scrollToTop)
        }

        if (pullToRefresh) lastPullToRefreshMs = nowUtcMillis()
      } catch (t: Throwable) {
        onError()
      } finally {
        uiState = DiscoverUiModel(showLoading = false)
        progressJob.cancel()
      }
    }
  }

  private suspend fun onShowsLoaded(
    shows: List<Show>,
    followedShowsIds: List<Long>,
    seeLaterShowsIds: List<Long>,
    scrollToTop: Boolean
  ) {
    val items = shows
      .sortedBy(interactor.sortOrder)
      .mapIndexed { index, show ->
        val itemType = when (index) {
          in (0..500 step 14) -> FANART_WIDE
          in (5..500 step 14), in (9..500 step 14) -> FANART
          else -> POSTER
        }
        val image = interactor.findCachedImage(show, itemType)
        DiscoverListItem(
          show,
          image,
          isFollowed = show.ids.trakt.id in followedShowsIds,
          isSeeLater = show.ids.trakt.id in seeLaterShowsIds
        )
      }
    uiState = DiscoverUiModel(shows = items, sortOrder = interactor.sortOrder, scrollToTop = scrollToTop)
  }

  private fun List<Show>.sortedBy(order: DiscoverSortOrder) =
    when (order) {
      HOT -> this
      RATING -> this.sortedWith(compareByDescending<Show> { it.votes }.thenBy { it.rating })
      NEWEST -> this.sortedByDescending { it.year }
    }

  fun loadMissingImage(item: DiscoverListItem, force: Boolean) {

    fun updateShowsItem(newItem: DiscoverListItem) {
      val currentItems = uiState?.shows?.toMutableList()
      currentItems?.findReplace(newItem) { it.isSameAs(newItem) }
      uiState = DiscoverUiModel(shows = currentItems, scrollToTop = false)
    }

    viewModelScope.launch {
      updateShowsItem(item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        updateShowsItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateShowsItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun setSortOrder(order: DiscoverSortOrder) {
    interactor.sortOrder = order
  }

  private suspend fun checkTvdbAuth() {
    try {
      interactor.checkTvdbAuth()
    } catch (t: Throwable) {
      // Ignore at this moment
    }
  }

  private fun onError() {
    _errorLiveData.value = R.string.errorCouldNotLoadDiscover
  }
}
