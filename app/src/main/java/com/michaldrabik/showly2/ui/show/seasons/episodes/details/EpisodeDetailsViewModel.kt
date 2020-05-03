package com.michaldrabik.showly2.ui.show.seasons.episodes.details

import androidx.lifecycle.viewModelScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.images.EpisodeImagesProvider
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.IdTvdb
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.TraktRating
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.repository.rating.RatingsRepository
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.show.RatingState
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class EpisodeDetailsViewModel @Inject constructor(
  private val imagesProvider: EpisodeImagesProvider,
  private val ratingsRepository: RatingsRepository,
  private val userTraktManager: UserTraktManager,
  private val mappers: Mappers,
  private val cloud: Cloud
) : BaseViewModel<EpisodeDetailsUiModel>() {

  fun loadImage(tvdb: IdTvdb) {
    viewModelScope.launch {
      try {
        uiState = EpisodeDetailsUiModel(imageLoading = true)
        val ids = Ids.EMPTY.copy(tvdb = tvdb)
        val episode = Episode.EMPTY.copy(ids = ids)
        val episodeImage = imagesProvider.loadRemoteImage(episode)
        uiState = EpisodeDetailsUiModel(image = episodeImage, imageLoading = false)
      } catch (t: Throwable) {
        uiState = EpisodeDetailsUiModel(imageLoading = false)
      }
    }
  }

  fun loadComments(idTrakt: IdTrakt, season: Int, episode: Int) {
    viewModelScope.launch {
      try {
        uiState = EpisodeDetailsUiModel(commentsLoading = true)
        val comments = cloud.traktApi.fetchEpisodeComments(idTrakt.id, season, episode)
          .map { mappers.comment.fromNetwork(it) }
          .filter { it.parentId <= 0 }
          .sortedByDescending { it.id }
        uiState = EpisodeDetailsUiModel(comments = comments, commentsLoading = false)
      } catch (t: Throwable) {
        Timber.w("Failed to load comments. ${t.message}")
        uiState = EpisodeDetailsUiModel(commentsLoading = false)
      }
    }
  }

  fun loadRatings(episode: Episode) {
    viewModelScope.launch {
      try {
        if (!userTraktManager.isAuthorized()) {
          uiState = EpisodeDetailsUiModel(ratingState = RatingState(rateAllowed = false, rateLoading = false))
          return@launch
        }
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(rateAllowed = true, rateLoading = true))
        val token = userTraktManager.checkAuthorization()
        val rating = ratingsRepository.loadRating(token.token, episode)
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(userRating = rating, rateLoading = false))
      } catch (error: Throwable) {
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(rateLoading = false))
      }
    }
  }

  fun addRating(rating: Int, episode: Episode) {
    viewModelScope.launch {
      try {
        val token = userTraktManager.checkAuthorization().token
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(rateLoading = true))
        ratingsRepository.addRating(token, episode, rating)
        _messageLiveData.value = R.string.textShowRated
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(userRating = TraktRating(episode.ids.trakt, rating)))
      } catch (error: Throwable) {
        _errorLiveData.value = R.string.errorGeneral
      } finally {
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(rateLoading = false))
      }
    }
  }
}
