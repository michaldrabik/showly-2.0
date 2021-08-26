package com.michaldrabik.ui_progress.progress

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
import com.michaldrabik.ui_progress.main.ProgressMainUiState
import com.michaldrabik.ui_progress.progress.cases.ProgressItemsCase
import com.michaldrabik.ui_progress.progress.cases.ProgressPinnedItemsCase
import com.michaldrabik.ui_progress.progress.cases.ProgressSortOrderCase
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
) : BaseViewModel() {

  private val itemsState = MutableStateFlow<List<ProgressListItem>?>(null)
  private val loadingState = MutableStateFlow(false)
  private val scrollState = MutableStateFlow(ActionEvent(false))
  private val sortOrderState = MutableStateFlow<ActionEvent<SortOrder>?>(null)

  val uiState = combine(
    itemsState,
    scrollState,
    sortOrderState,
    loadingState
  ) { s1, s2, s3, s4 ->
    ProgressUiState(
      items = s1,
      scrollReset = s2,
      sortOrder = s3,
      isLoading = s4
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ProgressUiState()
  )

  private val language by lazy { translationsRepository.getLanguage() }
  private var searchQuery: String? = null
  private var timestamp = 0L
  var isQuickRateEnabled = false

  fun onParentState(state: ProgressMainUiState) {
    when {
      this.timestamp != state.timestamp && state.timestamp != 0L -> {
        this.timestamp = state.timestamp ?: 0L
        loadItems(resetScroll = state.resetScroll?.consume() == true)
      }
      this.searchQuery != state.searchQuery -> {
        this.searchQuery = state.searchQuery
        loadItems(resetScroll = state.searchQuery.isNullOrBlank())
      }
    }
  }

  private fun loadItems(resetScroll: Boolean = false) {
    viewModelScope.launch {
      loadingState.value = true
      val items = itemsCase.loadItems(searchQuery ?: "")
      itemsState.value = items
      loadingState.value = false
      scrollState.value = ActionEvent(resetScroll)
    }
  }

  fun loadSortOrder() {
    if (itemsState.value?.isEmpty() == true) return
    viewModelScope.launch {
      val sortOrder = sortOrderCase.loadSortOrder()
      sortOrderState.value = ActionEvent(sortOrder)
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
        _messageState.emit(MessageEvent.info(R.string.textRateSaved))
        Analytics.logEpisodeRated(showTraktId.id, episode, rating)
      } catch (error: Throwable) {
        _messageState.emit(MessageEvent.error(R.string.errorGeneral))
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
    val currentItems = itemsState.value?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it.isSameAs(new) }
    itemsState.value = currentItems
    scrollState.value = ActionEvent(false)
  }
}
