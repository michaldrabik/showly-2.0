package com.michaldrabik.ui_my_movies.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.ReloadData
import com.michaldrabik.ui_base.trakt.TraktSyncWorker
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowedMoviesViewModel @Inject constructor(
  private val eventsManager: EventsManager,
  workManager: WorkManager
) : ViewModel() {

  private val searchQueryState = MutableStateFlow<String?>(null)
  private val syncingState = MutableStateFlow(false)

  init {
    workManager.getWorkInfosByTagLiveData(TraktSyncWorker.TAG_ID).observeForever { work ->
      syncingState.value = work.any { it.state == WorkInfo.State.RUNNING }
    }
  }

  fun onSearchQuery(searchQuery: String?) {
    searchQueryState.value = searchQuery
  }

  fun refreshData() {
    viewModelScope.launch {
      eventsManager.sendEvent(ReloadData)
    }
  }

  val uiState = combine(
    searchQueryState,
    syncingState
  ) { s1, s2 ->
    FollowedMoviesUiState(
      searchQuery = s1,
      isSyncing = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = FollowedMoviesUiState()
  )
}
