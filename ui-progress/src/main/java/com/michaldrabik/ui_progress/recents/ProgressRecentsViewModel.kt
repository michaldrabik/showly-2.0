package com.michaldrabik.ui_progress.recents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress.recents.cases.ProgressRecentsCase
import com.michaldrabik.ui_progress.recents.recycler.RecentsListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressRecentsViewModel @Inject constructor(
  private val recentsCase: ProgressRecentsCase,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
) : BaseViewModel<ProgressRecentsUiModel>() {

  private val language by lazy { translationsRepository.getLanguage() }

  private val _itemsLiveData = MutableLiveData<List<RecentsListItem>>()
  val itemsLiveData: LiveData<List<RecentsListItem>> get() = _itemsLiveData

  init {
    loadItems()
  }

  private fun loadItems() {
    viewModelScope.launch {
      val items = recentsCase.loadRecentItems()
      _itemsLiveData.postValue(items)
    }
  }

  fun findMissingImage(item: RecentsListItem, force: Boolean) {
    check(item is RecentsListItem.Episode)
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

  fun findMissingTranslation(item: RecentsListItem) {
    check(item is RecentsListItem.Episode)
    if (item.translations?.show != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.show, language)
        val translations = item.translations?.copy(show = translation)
        updateItem(item.copy(translations = translations))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ProgressRecentsViewModel::findMissingTranslation()")
      }
    }
  }

  private fun updateItem(new: RecentsListItem.Episode) {
    val currentItems = _itemsLiveData.value?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it.isSameAs(new) }
    _itemsLiveData.postValue(currentItems)
  }
}
