package com.michaldrabik.ui_progress.calendar

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.calendar.cases.ProgressCalendarCase
import com.michaldrabik.ui_progress.main.ProgressUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressCalendarViewModel @Inject constructor(
  private val calendarCase: ProgressCalendarCase,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
) : BaseViewModel<ProgressCalendarUiModel>() {

  private val language by lazy { translationsRepository.getLanguage() }

  fun handleParentAction(model: ProgressUiModel) {
    val allItems = model.items?.toMutableList() ?: mutableListOf()
    val items = calendarCase.prepareItems(allItems)
    uiState = ProgressCalendarUiModel(items = items)
  }

  fun findMissingImage(item: ProgressItem, force: Boolean) {
    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(image = image, isLoading = false))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        updateItem(item.copy(image = unavailable, isLoading = false))
      }
    }
  }

  fun findMissingTranslation(item: ProgressItem) {
    if (item.translations?.show != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.show, language)
        val translations = item.translations?.copy(show = translation)
        updateItem(item.copy(translations = translations))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "${ProgressCalendarViewModel::class.simpleName}::findMissingTranslation()")
      }
    }
  }

  private fun updateItem(new: ProgressItem) {
    val currentItems = uiState?.items?.toMutableList()
    currentItems?.findReplace(new) { it.isSameAs(new) }
    uiState = ProgressCalendarUiModel(items = currentItems)
  }
}
