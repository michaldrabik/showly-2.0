package com.michaldrabik.ui_lists.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_lists.manage.cases.ManageListsCase
import com.michaldrabik.ui_lists.manage.recycler.ManageListsItem
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageListsViewModel @Inject constructor(
  private val manageListsCase: ManageListsCase,
) : ViewModel() {

  private val loadingState = MutableStateFlow(false)
  private val itemsState = MutableStateFlow<List<ManageListsItem>?>(null)

  fun loadLists(itemId: IdTrakt, itemType: String) {
    viewModelScope.launch {
      val loadingJob = launch {
        delay(500)
        loadingState.value = true
      }
      val items = manageListsCase.loadLists(itemId, itemType)
      itemsState.value = items
      loadingState.value = false
      loadingJob.cancel()
    }
  }

  fun onListItemChecked(
    itemId: IdTrakt,
    itemType: String,
    listItem: ManageListsItem,
    isChecked: Boolean,
  ) {
    viewModelScope.launch {
      if (isChecked) {
        updateItem(listItem.copy(isEnabled = false, isChecked = true))
        manageListsCase.addToList(itemId, itemType, listItem)
        updateItem(listItem.copy(isEnabled = true, isChecked = true))
      } else {
        updateItem(listItem.copy(isEnabled = false, isChecked = false))
        manageListsCase.removeFromList(itemId, itemType, listItem)
        updateItem(listItem.copy(isEnabled = true, isChecked = false))
      }
    }
  }

  private fun updateItem(listItem: ManageListsItem) {
    val currentItems = uiState.value.items?.toMutableList()
    currentItems?.findReplace(listItem) { it.list.id == listItem.list.id }
    itemsState.value = currentItems
  }

  val uiState = combine(
    loadingState,
    itemsState
  ) { _, itemsState ->
    ManageListsUiState(
      items = itemsState
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ManageListsUiState()
  )
}
