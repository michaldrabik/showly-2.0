package com.michaldrabik.ui_lists.details

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_lists.details.cases.MainListDetailsCase
import com.michaldrabik.ui_lists.details.cases.SortOrderListDetailsCase
import com.michaldrabik.ui_lists.details.cases.TranslationsListDetailsCase
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.SortOrderList
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListDetailsViewModel @Inject constructor(
  private val mainCase: MainListDetailsCase,
  private val translationsCase: TranslationsListDetailsCase,
  private val sortCase: SortOrderListDetailsCase,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider
) : BaseViewModel<ListDetailsUiModel>() {

  fun loadDetails(id: Long) {
    viewModelScope.launch {
      val list = mainCase.loadDetails(id)
      val listItems = mainCase.loadItems(list)
      uiState = ListDetailsUiModel(details = list, items = listItems)
    }
  }

  fun loadMissingImage(item: ListDetailsItem, force: Boolean) {
    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image =
          when {
            item.isShow() -> showImagesProvider.loadRemoteImage(item.requireShow(), item.image.type, force)
            item.isMovie() -> movieImagesProvider.loadRemoteImage(item.requireMovie(), item.image.type, force)
            else -> throw IllegalStateException()
          }
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun loadMissingTranslation(item: ListDetailsItem) {
    if (item.translation != null || translationsCase.language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsCase.loadTranslation(item, false)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ListDetailsViewModel::loadMissingTranslation()")
      }
    }
  }

  fun setSortOrder(id: Long, sortOrder: SortOrderList) {
    viewModelScope.launch {
      val list = sortCase.setSortOrder(id, sortOrder)

      val currentItems = uiState?.items?.toList() ?: emptyList()
      val sortedItems = mainCase.sortItems(currentItems, sortOrder)

      uiState = ListDetailsUiModel(
        details = list,
        items = sortedItems,
        resetScroll = ActionEvent(true)
      )
    }
  }

  fun deleteList(listId: Long) {
    viewModelScope.launch {
      mainCase.deleteList(listId)
      uiState = ListDetailsUiModel(deleteEvent = ActionEvent(true))
    }
  }

  private fun updateItem(newItem: ListDetailsItem) {
    val currentItems = uiState?.items?.toMutableList()
    currentItems?.findReplace(newItem) { it.getId() == newItem.getId() }
    uiState = uiState?.copy(items = currentItems)
  }
}
