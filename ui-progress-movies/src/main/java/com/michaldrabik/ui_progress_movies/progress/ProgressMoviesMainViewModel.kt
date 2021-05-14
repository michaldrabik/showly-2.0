package com.michaldrabik.ui_progress_movies.progress

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.ratings.RatingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesUiModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProgressMoviesMainViewModel @Inject constructor(
  private val imagesProvider: MovieImagesProvider,
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
  private val translationsRepository: TranslationsRepository
) : BaseViewModel<ProgressMoviesMainUiModel>() {

  private val language by lazy { translationsRepository.getLanguage() }
  var isQuickRateEnabled = false

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

  fun addRating(rating: Int, movie: Movie) {
    viewModelScope.launch {
      try {
        val token = userTraktManager.checkAuthorization().token
        uiState = ProgressMoviesMainUiModel(ratingState = RatingState(rateLoading = true))
        ratingsRepository.movies.addRating(token, movie, rating)
        _messageLiveData.value = MessageEvent.info(R.string.textRateSaved)
        uiState = ProgressMoviesMainUiModel(ratingState = RatingState(userRating = TraktRating(movie.ids.trakt, rating)))
        Analytics.logMovieRated(movie, rating)
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      } finally {
        uiState = ProgressMoviesMainUiModel(ratingState = RatingState(rateLoading = false))
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

  private fun updateItem(new: ProgressMovieItem) {
    val currentItems = uiState?.items?.toMutableList()
    currentItems?.findReplace(new) { it.isSameAs(new) }
    uiState = ProgressMoviesMainUiModel(items = currentItems)
  }
}
