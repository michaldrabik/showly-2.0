package com.michaldrabik.ui_lists.lists

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_lists.lists.cases.MainListsCase
import com.michaldrabik.ui_lists.lists.cases.SortOrderListsCase
import com.michaldrabik.ui_lists.lists.helpers.ListsItemImage
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.SortOrder
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListsViewModel @Inject constructor(
  private val mainCase: MainListsCase,
  private val sortCase: SortOrderListsCase,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
) : BaseViewModel<ListsUiModel>() {

  fun loadItems(resetScroll: Boolean, searchQuery: String? = null) {
    viewModelScope.launch {
      val items = mainCase.loadLists(searchQuery)
      uiState = ListsUiModel(items = items, resetScroll = ActionEvent(resetScroll))
    }
  }

  fun loadSortOrder() {
    viewModelScope.launch {
      val sortOrder = sortCase.loadSortOrder()
      uiState = ListsUiModel(sortOrderEvent = ActionEvent(sortOrder))
    }
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortCase.setSortOrder(sortOrder)
      loadItems(resetScroll = true)
    }
  }

  fun loadMissingImage(item: ListsItem, itemImage: ListsItemImage, force: Boolean) {
    viewModelScope.launch {
      try {
        val imageType = itemImage.image.type

        val image =
          when {
            itemImage.isShow() -> showImagesProvider.loadRemoteImage(itemImage.show!!, imageType, force)
            itemImage.isMovie() -> movieImagesProvider.loadRemoteImage(itemImage.movie!!, imageType, force)
            else -> throw IllegalStateException()
          }

        val updateItemImage = itemImage.copy(image = image)
        val updateImages = item.images.toMutableList()
        updateImages.findReplace(updateItemImage) { it.getIds()?.trakt == updateItemImage.getIds()?.trakt }
        updateItem(item.copy(images = updateImages))
      } catch (t: Throwable) {
        val updateItemImage = itemImage.copy(image = Image.createUnavailable(itemImage.image.type))
        val updateImages = item.images.toMutableList()
        updateImages.findReplace(updateItemImage) { it.getIds()?.trakt == updateItemImage.getIds()?.trakt }
        updateItem(item.copy(images = updateImages))
      }
    }
  }

  private fun updateItem(newItem: ListsItem) {
    val currentItems = uiState?.items?.toMutableList()
    currentItems?.findReplace(newItem) { it.list.id == newItem.list.id }
    uiState = uiState?.copy(items = currentItems)
  }
}
