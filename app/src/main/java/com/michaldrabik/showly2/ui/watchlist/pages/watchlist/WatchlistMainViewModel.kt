package com.michaldrabik.showly2.ui.watchlist.pages.watchlist

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.watchlist.WatchlistInteractor
import com.michaldrabik.showly2.ui.watchlist.WatchlistUiModel
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import kotlinx.coroutines.launch
import javax.inject.Inject

class WatchlistMainViewModel @Inject constructor(
  private val interactor: WatchlistInteractor
) : BaseViewModel<WatchlistMainUiModel>() {

  fun handleParentAction(model: WatchlistUiModel) {
    val allItems = model.items?.toMutableList() ?: mutableListOf()

    val headerIndex = allItems.indexOfFirst {
      !it.isHeader() && !it.episode.hasAired(it.season) && !it.isPinned
    }
    if (headerIndex != -1) {
      val item = allItems[headerIndex]
      allItems.add(headerIndex, item.copy(headerTextResId = R.string.textWatchlistIncoming))
    }

    val pinnedItems = allItems
      .sortedByDescending { !it.isHeader() && it.isPinned }

    uiState = WatchlistMainUiModel(items = pinnedItems)
  }

  fun findMissingImage(item: WatchlistItem, force: Boolean) {

    fun updateItem(new: WatchlistItem) {
      val currentItems = uiState?.items?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = WatchlistMainUiModel(items = currentItems)
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
