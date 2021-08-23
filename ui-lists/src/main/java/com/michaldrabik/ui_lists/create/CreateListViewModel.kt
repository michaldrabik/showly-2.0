package com.michaldrabik.ui_lists.create

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.create.cases.CreateListCase
import com.michaldrabik.ui_lists.create.cases.ListDetailsCase
import com.michaldrabik.ui_model.CustomList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateListViewModel @Inject constructor(
  private val createListCase: CreateListCase,
  private val listDetailsCase: ListDetailsCase,
) : BaseViewModel() {

  private val detailsState = MutableStateFlow<CustomList?>(null)
  private val loadingState = MutableStateFlow(false)
  private val listUpdateState = MutableStateFlow<ActionEvent<CustomList>?>(null)

  val uiState = combine(
    detailsState,
    loadingState,
    listUpdateState
  ) { s1, s2, s3 ->
    CreateListUiState(
      listDetails = s1,
      isLoading = s2,
      onListUpdated = s3
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = CreateListUiState()
  )

  fun loadDetails(id: Long) {
    viewModelScope.launch {
      loadingState.value = true
      detailsState.value = listDetailsCase.loadDetails(id)
      loadingState.value = false
    }
  }

  fun createList(name: String, description: String?) {
    if (name.trim().isBlank()) return
    viewModelScope.launch {
      try {
        loadingState.value = true
        val list = createListCase.createList(name, description)
        listUpdateState.value = ActionEvent(list)
      } catch (error: Throwable) {
        _messageState.emit(MessageEvent.error(R.string.errorCouldNotCreateList))
        loadingState.value = false
      }
    }
  }

  fun updateList(list: CustomList) {
    if (list.name.trim().isBlank()) return
    viewModelScope.launch {
      try {
        loadingState.value = true
        detailsState.value = list
        val updatedList = createListCase.updateList(list)
        listUpdateState.value = ActionEvent(updatedList)
      } catch (error: Throwable) {
        _messageState.emit(MessageEvent.error(R.string.errorCouldNotUpdateList))
        detailsState.value = list
        loadingState.value = false
      }
    }
  }
}
