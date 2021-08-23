package com.michaldrabik.ui_my_shows.main

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_my_shows.main.cases.FollowedShowsSearchCase
import com.michaldrabik.ui_my_shows.myshows.helpers.MyShowsSearchResult
import com.michaldrabik.ui_my_shows.myshows.helpers.ResultType.EMPTY
import com.michaldrabik.ui_my_shows.myshows.helpers.ResultType.NO_RESULTS
import com.michaldrabik.ui_my_shows.myshows.helpers.ResultType.RESULTS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowedShowsViewModel @Inject constructor(
  private val searchCase: FollowedShowsSearchCase,
) : BaseViewModel() {

  private val searchResultState = MutableStateFlow<MyShowsSearchResult?>(null)

  val uiState = combine(searchResultState) { s1 -> FollowedShowsUiState(searchResult = s1[0]) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
      initialValue = FollowedShowsUiState()
    )

  private var searchJob: Job? = null

  var searchViewTranslation = 0F
  var tabsTranslation = 0F

  fun searchFollowedShows(query: String) {
    if (query.trim().isBlank()) {
      searchJob?.cancel()
      val result = MyShowsSearchResult(emptyList(), EMPTY)
      searchResultState.value = result
      return
    }
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
      val results = searchCase.searchFollowed(query)
      val type = if (results.isEmpty()) NO_RESULTS else RESULTS
      val searchResult = MyShowsSearchResult(results, type)
      searchResultState.value = searchResult
    }
  }

  fun clearCache() = searchCase.clearCache()
}
