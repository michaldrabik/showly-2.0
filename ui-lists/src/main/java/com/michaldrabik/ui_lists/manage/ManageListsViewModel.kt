package com.michaldrabik.ui_lists.manage

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_lists.manage.cases.ManageListsCase
import com.michaldrabik.ui_lists.manage.recycler.ManageListsItem
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
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
    context: Context,
    itemId: IdTrakt,
    itemType: String,
    listItem: ManageListsItem,
    isChecked: Boolean
  ) {
    viewModelScope.launch {
      if (isChecked) {
        updateItem(listItem.copy(isEnabled = false, isChecked = true))
        manageListsCase.addToList(context, itemId, itemType, listItem)
        updateItem(listItem.copy(isEnabled = true, isChecked = true))
      } else {
        updateItem(listItem.copy(isEnabled = false, isChecked = false))
        manageListsCase.removeFromList(context, itemId, itemType, listItem)
        updateItem(listItem.copy(isEnabled = true, isChecked = false))
      }
    }
  }

  private fun updateItem(listItem: ManageListsItem) {
    val currentItems = uiState?.items?.toMutableList()
    currentItems?.findReplace(listItem) { it.list.id == listItem.list.id }
    uiState = ManageListsUiModel(items = currentItems)
  }
}
