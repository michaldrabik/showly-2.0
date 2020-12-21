package com.michaldrabik.ui_progress_movies.calendar

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcDay
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.ProgressMoviesCalendarViewModel.Section.LATER
import com.michaldrabik.ui_progress_movies.calendar.ProgressMoviesCalendarViewModel.Section.NEXT_MONTH
import com.michaldrabik.ui_progress_movies.calendar.ProgressMoviesCalendarViewModel.Section.NEXT_WEEK
import com.michaldrabik.ui_progress_movies.calendar.ProgressMoviesCalendarViewModel.Section.NEXT_YEAR
import com.michaldrabik.ui_progress_movies.calendar.ProgressMoviesCalendarViewModel.Section.THIS_WEEK
import com.michaldrabik.ui_progress_movies.calendar.ProgressMoviesCalendarViewModel.Section.THIS_YEAR
import com.michaldrabik.ui_progress_movies.calendar.ProgressMoviesCalendarViewModel.Section.TODAY
import com.michaldrabik.ui_progress_movies.calendar.ProgressMoviesCalendarViewModel.Section.TOMORROW
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesUiModel
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import kotlinx.coroutines.launch
import org.threeten.bp.DayOfWeek.SUNDAY
import javax.inject.Inject

class ProgressMoviesCalendarViewModel @Inject constructor(
  private val imagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) : BaseViewModel<ProgressMoviesCalendarUiModel>() {

  private val language by lazy { settingsRepository.getLanguage() }

  enum class Section(@StringRes val headerRes: Int, val order: Int) {
    TODAY(R.string.textToday, 0),
    TOMORROW(R.string.textTomorrow, 1),
    THIS_WEEK(R.string.textThisWeek, 2),
    NEXT_WEEK(R.string.textNextWeek, 3),
    NEXT_MONTH(R.string.textNextMonth, 4),
    THIS_YEAR(R.string.textThisYear, 5),
    NEXT_YEAR(R.string.textNextYear, 6),
    LATER(R.string.textLater, 7)
  }

  fun handleParentAction(model: ProgressMoviesUiModel) {
    val allItems = model.items?.toMutableList() ?: mutableListOf()

    val items = allItems
      .filter { !it.movie.hasAired() || it.movie.isToday() }
      .sortedBy { it.movie.released?.toEpochDay() }
      .toMutableList()

    val groupedItems = groupByTime(items)

    uiState = ProgressMoviesCalendarUiModel(items = groupedItems)
  }

  private fun groupByTime(items: MutableList<ProgressMovieItem>): List<ProgressMovieItem> {
    val today = nowUtcDay()
    val nextWeekStart = today.plusDays(((SUNDAY.value - today.dayOfWeek.value) + 1L))

    val timeMap = mutableMapOf<Section, MutableList<ProgressMovieItem>>()
    val sectionsList = mutableListOf<ProgressMovieItem>()

    items
      .filter { it.movie.released != null }
      .forEach { item ->
        val time = item.movie.released!!
        val isSameYear = time.year == today.year
        when {
          isSameYear && time.dayOfYear == today.dayOfYear ->
            timeMap.getOrPut(TODAY, { mutableListOf() }).add(item)
          isSameYear && time.dayOfYear == today.plusDays(1).dayOfYear ->
            timeMap.getOrPut(TOMORROW, { mutableListOf() }).add(item)
          time.isBefore(nextWeekStart) ->
            timeMap.getOrPut(THIS_WEEK, { mutableListOf() }).add(item)
          time.isBefore(nextWeekStart.plusWeeks(1)) ->
            timeMap.getOrPut(NEXT_WEEK, { mutableListOf() }).add(item)
          isSameYear && time.month == today.plusMonths(1).month ->
            timeMap.getOrPut(NEXT_MONTH, { mutableListOf() }).add(item)
          isSameYear ->
            timeMap.getOrPut(THIS_YEAR, { mutableListOf() }).add(item)
          time.year == today.plusYears(1).year ->
            timeMap.getOrPut(NEXT_YEAR, { mutableListOf() }).add(item)
          else ->
            timeMap.getOrPut(LATER, { mutableListOf() }).add(item)
        }
      }

    timeMap.entries
      .sortedBy { it.key.order }
      .forEach { (section, items) ->
        sectionsList.run {
          add(ProgressMovieItem.EMPTY.copy(headerTextResId = section.headerRes))
          addAll(items.toList())
        }
      }

    return sectionsList
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
