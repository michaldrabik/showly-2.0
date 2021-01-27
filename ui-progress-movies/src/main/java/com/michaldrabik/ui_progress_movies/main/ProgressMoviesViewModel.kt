package com.michaldrabik.ui_progress_movies.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesLoadItemsCase
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesMainCase
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesPinnedItemsCase
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesSortOrderCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProgressMoviesViewModel @Inject constructor(
  private val myMoviesCase: ProgressMoviesMainCase,
  private val loadItemsCase: ProgressMoviesLoadItemsCase,
  private val pinnedItemsCase: ProgressMoviesPinnedItemsCase,
  private val sortOrderCase: ProgressMoviesSortOrderCase,
  private val imagesProvider: MovieImagesProvider
) : BaseViewModel<ProgressMoviesUiModel>() {

  private var searchQuery = ""

  fun loadProgress(resetScroll: Boolean = false) {
    viewModelScope.launch {
      val movies = loadItemsCase.loadWatchlistMovies()
      val dateFormat = loadItemsCase.loadDateFormat()
      val items = movies.map { movie ->
        async {
          val item = loadItemsCase.loadProgressItem(movie)
          val image = imagesProvider.findCachedImage(movie, POSTER)
          item.copy(image = image, dateFormat = dateFormat)
        }
      }.awaitAll()

      val sortOrder = sortOrderCase.loadSortOrder()
      val allItems = loadItemsCase.prepareItems(items, searchQuery, sortOrder)
      uiState =
        ProgressMoviesUiModel(
          items = allItems,
          isSearching = searchQuery.isNotBlank(),
          sortOrder = sortOrder,
          resetScroll = ActionEvent(resetScroll)
        )
    }
  }

  fun addWatchedMovie(context: Context, item: ProgressMovieItem) {
    viewModelScope.launch {
      myMoviesCase.addToMyMovies(context, item.movie)
      loadProgress()
    }
  }

  fun searchQuery(searchQuery: String) {
    this.searchQuery = searchQuery.trim()
    loadProgress()
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder)
      loadProgress(resetScroll = true)
    }
  }

  fun togglePinItem(item: ProgressMovieItem) {
    if (item.isPinned) {
      pinnedItemsCase.removePinnedItem(item)
    } else {
      pinnedItemsCase.addPinnedItem(item)
    }
    loadProgress()
  }
}
