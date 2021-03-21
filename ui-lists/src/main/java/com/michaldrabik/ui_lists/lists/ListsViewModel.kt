package com.michaldrabik.ui_lists.lists

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_lists.lists.cases.MainListsCase
import com.michaldrabik.ui_lists.lists.cases.SortOrderListsCase
import com.michaldrabik.ui_model.SortOrder
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListsViewModel @Inject constructor(
  private val mainCase: MainListsCase,
  private val sortCase: SortOrderListsCase
) : BaseViewModel<ListsUiModel>() {

  var searchViewTranslation = 0F
  var tabsTranslation = 0F

  fun loadItems(resetScroll: Boolean) {
    viewModelScope.launch {
      val items = mainCase.loadLists()
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
}
