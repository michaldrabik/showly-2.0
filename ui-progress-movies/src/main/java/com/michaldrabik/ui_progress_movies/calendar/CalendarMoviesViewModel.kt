package com.michaldrabik.ui_progress_movies.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress_movies.calendar.cases.CalendarMoviesRatingsCase
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesFutureCase
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesRecentsCase
import com.michaldrabik.ui_progress_movies.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarMoviesViewModel @Inject constructor(
  private val recentsCase: CalendarMoviesRecentsCase,
  private val futureCase: CalendarMoviesFutureCase,
  private val ratingsCase: CalendarMoviesRatingsCase,
  private val imagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
) : BaseViewModel<CalendarMoviesUiModel>() {

  private val language by lazy { translationsRepository.getLanguage() }
  private var mode = CalendarMode.PRESENT_FUTURE
  private var searchQuery: String? = null
  var isQuickRateEnabled = false

  private val _itemsLiveData = MutableLiveData<Pair<CalendarMode, List<CalendarMovieListItem>>>()
  val itemsLiveData: LiveData<Pair<CalendarMode, List<CalendarMovieListItem>>> get() = _itemsLiveData

  fun handleParentAction(model: ProgressMoviesMainUiModel) {
    if (this.searchQuery != model.searchQuery) {
      this.searchQuery = model.searchQuery
      loadItems()
    }
  }

  private fun loadItems() {
    viewModelScope.launch {
      val items = when (mode) {
        CalendarMode.PRESENT_FUTURE -> futureCase.loadItems(searchQuery ?: "")
        CalendarMode.RECENTS -> recentsCase.loadItems(searchQuery ?: "")
      }
      _itemsLiveData.postValue(mode to items)
    }
  }

  fun findMissingImage(item: CalendarMovieListItem, force: Boolean) {
    check(item is CalendarMovieListItem.MovieItem)
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

  fun findMissingTranslation(item: CalendarMovieListItem) {
    check(item is CalendarMovieListItem.MovieItem)
    if (item.translation != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.movie, language)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "CalendarMoviesViewModel::findMissingTranslation()")
      }
    }
  }

  private fun updateItem(new: CalendarMovieListItem.MovieItem) {
    val currentItems = _itemsLiveData.value?.second?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it.isSameAs(new) }
    _itemsLiveData.postValue(mode to currentItems)
  }

  fun checkQuickRateEnabled() {
    viewModelScope.launch {
      isQuickRateEnabled = ratingsCase.isQuickRateEnabled()
    }
  }
}
