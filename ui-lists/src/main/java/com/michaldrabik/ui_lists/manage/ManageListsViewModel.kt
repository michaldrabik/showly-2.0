package com.michaldrabik.ui_lists.manage

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_lists.manage.cases.ManageListsCase
import com.michaldrabik.ui_lists.manage.recycler.ManageListsItem
import com.michaldrabik.ui_model.IdTrakt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ManageListsViewModel @Inject constructor(
  private val manageListsCase: ManageListsCase
) : BaseViewModel<ManageListsUiModel>() {

  fun loadLists(itemId: IdTrakt, itemType: String) {
    viewModelScope.launch {
      val loadingJob = launch {
        delay(500)
        uiState = ManageListsUiModel(isLoading = true)
      }
      uiState = ManageListsUiModel(isLoading = true)
      val items = manageListsCase.loadLists(itemId, itemType)
      uiState = ManageListsUiModel(items = items, isLoading = false)
      loadingJob.cancel()
    }
  }

  fun onListItemChecked(
    itemId: IdTrakt,
    itemType: String,
    listItem: ManageListsItem,
    isChecked: Boolean
  ) {
    viewModelScope.launch {
      if (isChecked) {
        manageListsCase.addToList(itemId, itemType, listItem)
      } else {
        manageListsCase.removeFromList(itemId, itemType, listItem)
      }
    }
  }
}
