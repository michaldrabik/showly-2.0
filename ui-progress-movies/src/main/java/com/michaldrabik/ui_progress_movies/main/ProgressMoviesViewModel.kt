package com.michaldrabik.ui_progress_movies.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesMainCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressMoviesViewModel @Inject constructor(
  private val moviesCase: ProgressMoviesMainCase,
) : BaseViewModel<ProgressMoviesUiModel>() {

  fun loadProgress() {
    viewModelScope.launch {
      uiState = ProgressMoviesUiModel(searchQuery = "")
    }
  }

  fun addWatchedMovie(context: Context, movie: Movie) {
    viewModelScope.launch {
      moviesCase.addToMyMovies(context, movie)
      loadProgress()
    }
  }

  fun onSearchQuery(searchQuery: String) {
    uiState = ProgressMoviesUiModel(searchQuery = searchQuery)
  }
}
