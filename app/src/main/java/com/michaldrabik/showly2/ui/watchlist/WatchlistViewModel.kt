package com.michaldrabik.showly2.ui.watchlist

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.watchlist.main.WatchlistMainUiModel
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import kotlinx.coroutines.launch
import javax.inject.Inject

class WatchlistViewModel @Inject constructor(
  private val interactor: WatchlistInteractor
) : BaseViewModel<WatchlistUiModel>() {

  fun handleParentAction(model: WatchlistMainUiModel) {
    uiState = WatchlistUiModel(items = model.items?.toList())
  }

  fun findMissingImage(item: WatchlistItem, force: Boolean) {

    fun updateItem(new: WatchlistItem) {
      val currentItems = uiState?.items?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = WatchlistUiModel(items = currentItems)
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
