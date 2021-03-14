package com.michaldrabik.ui_lists.details

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_lists.details.cases.MainListDetailsCase
import com.michaldrabik.ui_model.SortOrder
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListDetailsViewModel @Inject constructor(
  private val mainCase: MainListDetailsCase
) : BaseViewModel<ListDetailsUiModel>() {

  fun loadItems() {
    viewModelScope.launch {
      uiState = ListDetailsUiModel(items = emptyList())
    }
  }

  fun loadSortOrder() {
    viewModelScope.launch {
    }
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
    }
  }

  fun deleteList(listId: Long) {
    viewModelScope.launch {
      mainCase.deleteList(listId)
      uiState = ListDetailsUiModel(deleteEvent = ActionEvent(true))
    }
  }
}
