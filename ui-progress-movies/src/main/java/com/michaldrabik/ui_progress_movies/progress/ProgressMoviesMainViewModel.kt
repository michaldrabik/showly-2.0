package com.michaldrabik.ui_progress_movies.progress

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesUiModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProgressMoviesMainViewModel @Inject constructor(
  private val imagesProvider: MovieImagesProvider
) : BaseViewModel<ProgressMoviesMainUiModel>() {

  fun handleParentAction(model: ProgressMoviesUiModel) {
    val allItems = model.items
      ?.toMutableList()
      ?.filter { it.movie.released == null || it.movie.hasAired() }
      ?.sortedByDescending { !it.isHeader() && it.isPinned }
      ?: mutableListOf()

    uiState = ProgressMoviesMainUiModel(
      items = allItems,
      isSearching = model.isSearching,
      sortOrder = model.sortOrder,
      resetScroll = model.resetScroll
    )
  }

  fun findMissingImage(item: ProgressMovieItem, force: Boolean) {

    fun updateItem(new: ProgressMovieItem) {
      val currentItems = uiState?.items?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = ProgressMoviesMainUiModel(items = currentItems)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.movie, item.image.type, force)
        updateItem(item.copy(image = image, isLoading = false))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        updateItem(item.copy(image = unavailable, isLoading = false))
      }
    }
  }
}
