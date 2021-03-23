package com.michaldrabik.ui_lists.create

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_lists.create.cases.CreateListCase
import com.michaldrabik.ui_lists.create.cases.ListDetailsCase
import com.michaldrabik.ui_model.CustomList
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateListViewModel @Inject constructor(
  private val createListCase: CreateListCase,
  private val listDetailsCase: ListDetailsCase
) : BaseViewModel<CreateListUiModel>() {

  fun loadDetails(id: Long) {
    viewModelScope.launch {
      uiState = CreateListUiModel(isLoading = true)
      val list = listDetailsCase.loadDetails(id)
      uiState = CreateListUiModel(listDetails = list, isLoading = false)
    }
  }

  fun createList(name: String, description: String?) {
    if (name.trim().isBlank()) return
    viewModelScope.launch {
      uiState = CreateListUiModel(isLoading = true)
      val list = createListCase.createList(name, description)
      uiState = CreateListUiModel(listUpdatedEvent = ActionEvent(list))
    }
  }

  fun updateList(list: CustomList, name: String, description: String?) {
    if (name.trim().isBlank()) return
    viewModelScope.launch {
      uiState = CreateListUiModel(isLoading = true)
      val updatedList = createListCase.updateList(list, name, description)
      uiState = CreateListUiModel(listUpdatedEvent = ActionEvent(updatedList))
    }
  }
}
