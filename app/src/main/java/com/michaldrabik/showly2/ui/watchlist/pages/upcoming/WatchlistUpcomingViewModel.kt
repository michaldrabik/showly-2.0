package com.michaldrabik.showly2.ui.watchlist.pages.upcoming

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.watchlist.WatchlistInteractor
import com.michaldrabik.showly2.ui.watchlist.WatchlistUiModel
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import kotlinx.coroutines.launch
import javax.inject.Inject

class WatchlistUpcomingViewModel @Inject constructor(
  private val interactor: WatchlistInteractor
) : BaseViewModel<WatchlistUpcomingUiModel>() {

  fun handleParentAction(model: WatchlistUiModel) {
    val allItems = model.items?.toMutableList() ?: mutableListOf()

    val items = allItems
      .filter { it.upcomingEpisode != Episode.EMPTY }
      .sortedBy { it.upcomingEpisode.firstAired }

    uiState = WatchlistUpcomingUiModel(items = items)
  }

  fun findMissingImage(item: WatchlistItem, force: Boolean) {

    fun updateItem(new: WatchlistItem) {
      val currentItems = uiState?.items?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = WatchlistUpcomingUiModel(items = currentItems)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        updateItem(item.copy(image = image, isLoading = false))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        updateItem(item.copy(image = unavailable, isLoading = false))
      }
    }
  }
}
