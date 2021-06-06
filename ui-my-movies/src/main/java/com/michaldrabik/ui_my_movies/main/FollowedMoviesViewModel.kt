package com.michaldrabik.ui_my_movies.main

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_my_movies.main.cases.FollowedMoviesSearchCase
import com.michaldrabik.ui_my_movies.mymovies.helpers.MyMoviesSearchResult
import com.michaldrabik.ui_my_movies.mymovies.helpers.ResultType.EMPTY
import com.michaldrabik.ui_my_movies.mymovies.helpers.ResultType.NO_RESULTS
import com.michaldrabik.ui_my_movies.mymovies.helpers.ResultType.RESULTS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowedMoviesViewModel @Inject constructor(
  private val searchCase: FollowedMoviesSearchCase
) : BaseViewModel<FollowedMoviesUiModel>() {

  private var searchJob: Job? = null

  var searchViewTranslation = 0F
  var tabsTranslation = 0F

  fun searchMovies(query: String) {
    if (query.trim().isBlank()) {
      searchJob?.cancel()
      val result = MyMoviesSearchResult(emptyList(), EMPTY)
      postSearchResult(result)
      return
    }
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
      val results = searchCase.searchFollowed(query)
      val type = if (results.isEmpty()) NO_RESULTS else RESULTS
      val searchResult = MyMoviesSearchResult(results, type)
      postSearchResult(searchResult)
    }
  }

  private fun postSearchResult(searchResult: MyMoviesSearchResult) {
    uiState = FollowedMoviesUiModel(searchResult = searchResult)
    uiState = FollowedMoviesUiModel()
  }

  fun clearCache() = searchCase.clearCache()
}
