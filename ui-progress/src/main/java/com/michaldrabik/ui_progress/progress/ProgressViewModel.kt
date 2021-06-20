package com.michaldrabik.ui_progress.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.main.ProgressMainUiModel
import com.michaldrabik.ui_progress.progress.cases.ProgressItemsCase
import com.michaldrabik.ui_progress.progress.cases.ProgressPinnedItemsCase
import com.michaldrabik.ui_progress.progress.cases.ProgressSortOrderCase
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
  private val itemsCase: ProgressItemsCase,
  private val pinnedItemsCase: ProgressPinnedItemsCase,
  private val sortOrderCase: ProgressSortOrderCase,
  private val imagesProvider: ShowImagesProvider,
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
  private val translationsRepository: TranslationsRepository,
) : BaseViewModel<ProgressUiModel>() {

  private val language by lazy { translationsRepository.getLanguage() }
  private var searchQuery: String? = null
  private var timestamp = 0L
  var isQuickRateEnabled = false

  private val _itemsLiveData = MutableLiveData<Pair<List<ProgressListItem>, ActionEvent<Boolean>>>()
  val itemsLiveData: LiveData<Pair<List<ProgressListItem>, ActionEvent<Boolean>>> get() = _itemsLiveData

  private val _sortLiveData = MutableLiveData<ActionEvent<SortOrder>>()
  val sortLiveData: LiveData<ActionEvent<SortOrder>> get() = _sortLiveData

  fun handleParentAction(model: ProgressMainUiModel) {
    if (this.timestamp != model.timestamp && model.timestamp != 0L) {
      this.timestamp = model.timestamp ?: 0L
      loadItems()
    }
    if (this.searchQuery != model.searchQuery) {
      this.searchQuery = model.searchQuery
      loadItems(resetScroll = model.searchQuery.isNullOrBlank())
    }
  }

  private fun loadItems(resetScroll: Boolean = false) {
    viewModelScope.launch {
      val items = itemsCase.loadItems(searchQuery ?: "")
      _itemsLiveData.value = items to ActionEvent(resetScroll)
    }
  }

  fun loadSortOrder() {
    if (_itemsLiveData.value?.first?.isEmpty() == true) return
    viewModelScope.launch {
      val sortOrder = sortOrderCase.loadSortOrder()
      _sortLiveData.value = ActionEvent(sortOrder)
    }
  }

  fun findMissingImage(item: ProgressListItem, force: Boolean) {
    check(item is ProgressListItem.Episode)
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

  fun findMissingTranslation(item: ProgressListItem) {
    check(item is ProgressListItem.Episode)
    if (item.translations?.show != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.show, language)
        val translations = item.translations?.copy(show = translation)
        updateItem(item.copy(translations = translations))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "ProgressViewModel::findMissingTranslation()")
      }
    }
  }

  fun addRating(rating: Int, episode: Episode, showTraktId: IdTrakt) {
    viewModelScope.launch {
      try {
        val token = userTraktManager.checkAuthorization().token
        ratingsRepository.shows.addRating(token, episode, rating)
        _messageLiveData.value = MessageEvent.info(R.string.textRateSaved)
        Analytics.logEpisodeRated(showTraktId.id, episode, rating)
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder)
      loadItems(resetScroll = true)
    }
  }

  fun togglePinItem(item: ProgressListItem.Episode) {
    if (item.isPinned) {
      pinnedItemsCase.removePinnedItem(item.show)
    } else {
      pinnedItemsCase.addPinnedItem(item.show)
    }
    loadItems(resetScroll = item.isPinned)
  }

  fun checkQuickRateEnabled() {
    viewModelScope.launch {
      val isSignedIn = userTraktManager.isAuthorized()
      val isPremium = settingsRepository.isPremium
      val isQuickRate = settingsRepository.load().traktQuickRateEnabled
      isQuickRateEnabled = isPremium && isSignedIn && isQuickRate
    }
  }

  private fun updateItem(new: ProgressListItem) {
    val currentItems = _itemsLiveData.value?.first?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it.isSameAs(new) }
    _itemsLiveData.value = currentItems to ActionEvent(false)
  }
}
