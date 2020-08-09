package com.michaldrabik.showly2.ui.followedshows.seelater

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.seelater.cases.SeeLaterLoadShowsCase
import com.michaldrabik.showly2.ui.followedshows.seelater.cases.SeeLaterSortOrderCase
import com.michaldrabik.showly2.ui.followedshows.seelater.recycler.SeeLaterListItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import kotlinx.coroutines.launch
import javax.inject.Inject

class SeeLaterViewModel @Inject constructor(
  private val sortOrderCase: SeeLaterSortOrderCase,
  private val loadShowsCase: SeeLaterLoadShowsCase,
  private val imagesProvider: ShowImagesProvider
) : BaseViewModel<SeeLaterUiModel>() {

  fun loadShows() {
    viewModelScope.launch {
      val sortOrder = sortOrderCase.loadSortOrder()
      val items = loadShowsCase.loadShows().map {
        val image = imagesProvider.findCachedImage(it, POSTER)
        SeeLaterListItem(it, image, false)
      }
      uiState = SeeLaterUiModel(items = items, sortOrder = sortOrder)
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
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder)
      loadShows()
    }
  }
}
