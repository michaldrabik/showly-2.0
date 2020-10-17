package com.michaldrabik.ui_progress.progress

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.main.ProgressUiModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProgressMainViewModel @Inject constructor(
  private val imagesProvider: ShowImagesProvider
) : BaseViewModel<ProgressMainUiModel>() {

  fun handleParentAction(model: ProgressUiModel) {
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

    uiState = ProgressMainUiModel(
      items = pinnedItems,
      isSearching = model.isSearching,
      sortOrder = model.sortOrder,
      resetScroll = model.resetScroll
    )
  }

  fun findMissingImage(item: ProgressItem, force: Boolean) {

    fun updateItem(new: ProgressItem) {
      val currentItems = uiState?.items?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = ProgressMainUiModel(items = currentItems)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(image = image, isLoading = false))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        updateItem(item.copy(image = unavailable, isLoading = false))
      }
    }
  }
}
