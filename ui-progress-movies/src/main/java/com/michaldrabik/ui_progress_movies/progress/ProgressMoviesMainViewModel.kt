package com.michaldrabik.ui_progress_movies.progress

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesUiModel
import com.michaldrabik.ui_repository.TranslationsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProgressMoviesMainViewModel @Inject constructor(
  private val imagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository
) : BaseViewModel<ProgressMoviesMainUiModel>() {

  private val language by lazy { translationsRepository.getLanguage() }

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

  fun findMissingTranslation(item: ProgressMovieItem) {
    if (item.movieTranslation != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.movie, language)
        updateItem(item.copy(movieTranslation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "${ProgressMoviesMainViewModel::class.simpleName}::findMissingTranslation()")
      }
    }
  }

  fun findMissingImage(item: ProgressMovieItem, force: Boolean) {
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

  private fun updateItem(new: ProgressMovieItem) {
    val currentItems = uiState?.items?.toMutableList()
    currentItems?.findReplace(new) { it.isSameAs(new) }
    uiState = ProgressMoviesMainUiModel(items = currentItems)
  }
}
