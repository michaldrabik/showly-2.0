package com.michaldrabik.ui_progress_movies.calendar

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.calendar.cases.ProgressMoviesCalendarCase
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressMoviesCalendarViewModel @Inject constructor(
  private val calendarCase: ProgressMoviesCalendarCase,
  private val imagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository
) : BaseViewModel<ProgressMoviesCalendarUiModel>() {

  private val language by lazy { translationsRepository.getLanguage() }

  fun handleParentAction(model: ProgressMoviesUiModel) {
    val allItems = model.items?.toMutableList() ?: mutableListOf()
    val items = calendarCase.prepareItems(allItems)
    uiState = ProgressMoviesCalendarUiModel(items = items)
  }

  fun findMissingTranslation(item: ProgressMovieItem) {
    if (item.movieTranslation != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.movie, language)
        updateItem(item.copy(movieTranslation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "${ProgressMoviesCalendarViewModel::class.simpleName}::findMissingTranslation()")
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
    uiState = ProgressMoviesCalendarUiModel(items = currentItems)
  }
}
