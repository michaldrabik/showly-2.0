package com.michaldrabik.ui_my_movies.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FollowedMoviesViewModel @Inject constructor() : ViewModel() {

  private val searchQueryState = MutableStateFlow<String?>(null)

  fun onSearchQuery(searchQuery: String?) {
    searchQueryState.value = searchQuery
  }

  val uiState = combine(
    searchQueryState
  ) { s1 ->
    FollowedMoviesUiState(
      searchQuery = s1[0]
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = FollowedMoviesUiState()
  )
}
