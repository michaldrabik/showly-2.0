package com.michaldrabik.ui_base.common.sheets.remove_trakt_hidden

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoveTraktHiddenViewModel @Inject constructor() : BaseViewModel() {

  private val loadingState = MutableStateFlow(false)

  fun loadDetails(id: Long) {
    viewModelScope.launch {
      loadingState.value = true
    }
  }

//  val uiState = combine(
//    detailsState,
//    loadingState,
//    listUpdateState
//  ) { s1, s2, s3 ->
//    CreateListUiState(
//      listDetails = s1,
//      isLoading = s2,
//      onListUpdated = s3
//    )
//  }.stateIn(
//    scope = viewModelScope,
//    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
//    initialValue = CreateListUiState()
//  )
}
