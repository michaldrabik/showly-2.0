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
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.main.ProgressUiModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProgressMainViewModel @Inject constructor(
  private val imagesProvider: ShowImagesProvider,
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
  private val translationsRepository: TranslationsRepository,
) : BaseViewModel<ProgressMainUiModel>() {

  private val language by lazy { translationsRepository.getLanguage() }
  var isQuickRateEnabled = false

  fun handleParentAction(model: ProgressUiModel) {
    val allItems = model.items?.toMutableList() ?: mutableListOf()

    val headerIndex = allItems.indexOfFirst {
      !it.isHeader() && !it.episode.hasAired(it.season) && !it.isPinned
    }
    if (headerIndex != -1) {
      val item = allItems[headerIndex]
      allItems.add(headerIndex, item.copy(headerTextResId = R.string.textWatchlistIncoming))
    }

    val pinnedItems = allItems
      .filter {
        if (model.isUpcomingEnabled == true) {
          true
        } else {
          !it.isHeader() && (it.isPinned || it.episode.hasAired(it.season))
        }
      }
      .sortedByDescending { !it.isHeader() && it.isPinned }

    uiState = ProgressMainUiModel(
      items = pinnedItems,
      isSearching = model.isSearching,
      sortOrder = model.sortOrder,
      resetScroll = model.resetScroll
    )
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
    if (item.showTranslation != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.show, language)
        updateItem(item.copy(showTranslation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "${ProgressMainViewModel::class.simpleName}::findMissingTranslation()")
      }
    }
  }

  fun addRating(rating: Int, episode: Episode, showTraktId: IdTrakt) {
    viewModelScope.launch {
      try {
        val token = userTraktManager.checkAuthorization().token
        uiState = ProgressMainUiModel(ratingState = RatingState(rateLoading = true))
        ratingsRepository.shows.addRating(token, episode, rating)
        _messageLiveData.value = MessageEvent.info(R.string.textRateSaved)
        uiState = ProgressMainUiModel(ratingState = RatingState(userRating = TraktRating(episode.ids.trakt, rating)))
        Analytics.logEpisodeRated(showTraktId.id, episode, rating)
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      } finally {
        uiState = ProgressMainUiModel(ratingState = RatingState(rateLoading = false))
      }
    }
  }

  fun checkQuickRateEnabled() {
    viewModelScope.launch {
      val isSignedIn = userTraktManager.isAuthorized()
      val isPremium = settingsRepository.isPremium
      val isQuickRate = settingsRepository.load().traktQuickRateEnabled
      isQuickRateEnabled = isPremium && isSignedIn && isQuickRate
    }
  }

  private fun updateItem(new: ProgressItem) {
    val currentItems = uiState?.items?.toMutableList()
    currentItems?.findReplace(new) { it.isSameAs(new) }
    uiState = ProgressMainUiModel(items = currentItems)
  }
}
