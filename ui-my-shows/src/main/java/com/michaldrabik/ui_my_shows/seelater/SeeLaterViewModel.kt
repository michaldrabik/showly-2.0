package com.michaldrabik.ui_my_shows.seelater

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.seelater.cases.SeeLaterLoadShowsCase
import com.michaldrabik.ui_my_shows.seelater.cases.SeeLaterSortOrderCase
import com.michaldrabik.ui_my_shows.seelater.recycler.SeeLaterListItem
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
        val translation = loadShowsCase.loadTranslation(it)
        SeeLaterListItem(it, image, false, translation)
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
