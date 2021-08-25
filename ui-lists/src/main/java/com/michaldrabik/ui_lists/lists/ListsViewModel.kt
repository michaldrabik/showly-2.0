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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(
  private val mainCase: MainListsCase,
  private val sortCase: SortOrderListsCase,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
) : BaseViewModel() {

  private val itemsState = MutableStateFlow<List<ListsItem>?>(null)
  private val scrollState = MutableStateFlow(ActionEvent(false))
  private val sortOrderState = MutableStateFlow<ActionEvent<SortOrder>?>(null)

  fun loadItems(resetScroll: Boolean, searchQuery: String? = null) {
    viewModelScope.launch {
      val items = mainCase.loadLists(searchQuery)
      itemsState.value = items
      scrollState.value = ActionEvent(resetScroll)
    }
  }

  fun loadSortOrder() {
    viewModelScope.launch {
      val sortOrder = sortCase.loadSortOrder()
      sortOrderState.value = ActionEvent(sortOrder)
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
    val currentItems = uiState.value.items?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(newItem) { it.list.id == newItem.list.id }
    itemsState.value = currentItems
  }

  val uiState = combine(
    itemsState,
    scrollState,
    sortOrderState
  ) { itemsState, scrollState, sortOrderState ->
    ListsUiState(
      items = itemsState,
      resetScroll = scrollState,
      sortOrder = sortOrderState
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ListsUiState()
  )
}
