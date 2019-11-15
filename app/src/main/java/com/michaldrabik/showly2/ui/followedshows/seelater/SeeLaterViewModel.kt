package com.michaldrabik.showly2.ui.followedshows.seelater

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.seelater.recycler.SeeLaterListItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import kotlinx.coroutines.launch
import javax.inject.Inject

class SeeLaterViewModel @Inject constructor(
  private val interactor: SeeLaterInteractor
) : BaseViewModel<SeeLaterUiModel>() {

  fun loadShows() {
    viewModelScope.launch {
      val items = interactor.loadShows().map {
        val image = interactor.findCachedImage(it, POSTER)
        SeeLaterListItem(it, image, false)
      }
      uiState = SeeLaterUiModel(items = items)
    }
  }

  fun loadMissingImage(item: SeeLaterListItem, force: Boolean) {

    fun updateItem(new: SeeLaterListItem) {
      val currentItems = uiState?.items?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = uiState?.copy(items = currentItems)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }
}
