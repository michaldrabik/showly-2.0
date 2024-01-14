package com.michaldrabik.ui_lists.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError.AccountLimitsError
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
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
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val detailsState = MutableStateFlow<CustomList?>(null)
  private val loadingState = MutableStateFlow(false)
  private val listUpdateState = MutableStateFlow<Event<CustomList>?>(null)

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
        listUpdateState.value = Event(list)
      } catch (error: Throwable) {
        loadingState.value = false
        handleError(error, R.string.errorCouldNotCreateList)
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
        listUpdateState.value = Event(updatedList)
      } catch (error: Throwable) {
        detailsState.value = list
        loadingState.value = false
        handleError(error, R.string.errorCouldNotUpdateList)
      }
    }
  }

  private suspend fun handleError(error: Throwable, defaultErrorMessage: Int) {
    when (ErrorHelper.parse(error)) {
      AccountLimitsError ->
        messageChannel.send(MessageEvent.Error(R.string.errorAccountListsLimitsReached))
      else ->
        messageChannel.send(MessageEvent.Error(defaultErrorMessage))
    }
  }

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
}
