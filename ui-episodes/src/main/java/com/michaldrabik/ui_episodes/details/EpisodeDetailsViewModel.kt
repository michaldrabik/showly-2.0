package com.michaldrabik.ui_episodes.details

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.CommentsRepository
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.EpisodeImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_episodes.R
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.TraktRating
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailsViewModel @Inject constructor(
  private val imagesProvider: EpisodeImagesProvider,
  private val dateFormatProvider: DateFormatProvider,
  private val ratingsRepository: RatingsRepository,
  private val translationsRepository: TranslationsRepository,
  private val commentsRepository: CommentsRepository,
  private val userTraktManager: UserTraktManager,
) : BaseViewModel<EpisodeDetailsUiModel>() {

  init {
    uiState = EpisodeDetailsUiModel(dateFormat = dateFormatProvider.loadFullHourFormat())
  }

  fun loadImage(showId: IdTmdb, episode: Episode) {
    viewModelScope.launch {
      try {
        uiState = EpisodeDetailsUiModel(imageLoading = true)
        val episodeImage = imagesProvider.loadRemoteImage(showId, episode)
        uiState = EpisodeDetailsUiModel(image = episodeImage, imageLoading = false)
      } catch (t: Throwable) {
        uiState = EpisodeDetailsUiModel(imageLoading = false)
      }
    }
  }

  fun loadTranslation(showTraktId: IdTrakt, episode: Episode) {
    viewModelScope.launch {
      try {
        val language = translationsRepository.getLanguage()
        if (language == Config.DEFAULT_LANGUAGE) return@launch
        val translation = translationsRepository.loadTranslation(episode, showTraktId, language)
        translation?.let {
          uiState = EpisodeDetailsUiModel(translation = ActionEvent(it))
        }
      } catch (error: Throwable) {
        Timber.e(error)
      }
    }
  }

  fun loadComments(idTrakt: IdTrakt, season: Int, episode: Int) {
    viewModelScope.launch {
      try {
        uiState = EpisodeDetailsUiModel(commentsLoading = true)

        val isSignedIn = userTraktManager.isAuthorized()
        val username = userTraktManager.getUsername()
        val comments = commentsRepository.loadEpisodeComments(idTrakt, season, episode)
          .map {
            it.copy(
              isMe = it.user.username == username,
              isSignedIn = isSignedIn
            )
          }
          .partition { it.isMe }

        uiState = EpisodeDetailsUiModel(
          isSignedIn = isSignedIn,
          comments = comments.first + comments.second,
          commentsLoading = false,
          commentsDateFormat = dateFormatProvider.loadFullHourFormat()
        )
      } catch (t: Throwable) {
        Timber.w("Failed to load comments. ${t.message}")
        uiState = EpisodeDetailsUiModel(commentsLoading = false)
      }
    }
  }

  fun loadCommentReplies(comment: Comment) {
    var current = uiState?.comments?.toMutableList() ?: mutableListOf()
    if (current.any { it.parentId == comment.id }) return

    viewModelScope.launch {
      try {
        val parent = current.find { it.id == comment.id }
        parent?.let { p ->
          val copy = p.copy(isLoading = true)
          current.findReplace(copy) { it.id == p.id }
          uiState = EpisodeDetailsUiModel(comments = current)
        }

        val isSignedIn = userTraktManager.isAuthorized()
        val username = userTraktManager.getUsername()
        val replies = commentsRepository.loadReplies(comment.id)
          .map {
            it.copy(
              isSignedIn = isSignedIn,
              isMe = it.user.username == username
            )
          }

        current = uiState?.comments?.toMutableList() ?: mutableListOf()
        val parentIndex = current.indexOfFirst { it.id == comment.id }
        if (parentIndex > -1) current.addAll(parentIndex + 1, replies)
        parent?.let {
          current.findReplace(parent.copy(isLoading = false, replies = 0)) { it.id == comment.id }
        }

        uiState = EpisodeDetailsUiModel(comments = current)
      } catch (t: Throwable) {
        uiState = EpisodeDetailsUiModel(comments = current)
      }
    }
  }

  fun addNewComment(comment: Comment) {
    val current = uiState?.comments?.toMutableList() ?: mutableListOf()
    if (!comment.isReply()) {
      current.add(0, comment)
    } else {
      val parentIndex = current.indexOfLast { it.id == comment.parentId }
      if (parentIndex > -1) {
        val parent = current[parentIndex]
        current.add(parentIndex + 1, comment)
        val repliesCount = current.count { it.parentId == parent.id }.toLong()
        current.findReplace(parent.copy(replies = repliesCount)) { it.id == comment.parentId }
      }
    }
    uiState = EpisodeDetailsUiModel(comments = current)
  }

  fun deleteComment(comment: Comment) {
    var current = uiState?.comments?.toMutableList() ?: mutableListOf()
    val target = current.find { it.id == comment.id } ?: return

    viewModelScope.launch {
      try {
        val copy = target.copy(isLoading = true)
        current.findReplace(copy) { it.id == target.id }
        uiState = EpisodeDetailsUiModel(comments = current)

        commentsRepository.deleteComment(target.id)

        current = uiState?.comments?.toMutableList() ?: mutableListOf()
        val targetIndex = current.indexOfFirst { it.id == target.id }
        if (targetIndex > -1) {
          current.removeAt(targetIndex)
          if (target.isReply()) {
            val parent = current.first { it.id == target.parentId }
            val repliesCount = current.count { it.parentId == parent.id }.toLong()
            current.findReplace(parent.copy(replies = repliesCount)) { it.id == target.parentId }
          }
        }

        uiState = EpisodeDetailsUiModel(comments = current)
        _messageLiveData.value = MessageEvent.info(R.string.textCommentDeleted)
      } catch (t: Throwable) {
        if (t is HttpException && t.code() == 409) {
          _messageLiveData.value = MessageEvent.error(R.string.errorCommentDelete)
        } else {
          _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
        }
        uiState = EpisodeDetailsUiModel(comments = current)
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
        val rating = ratingsRepository.shows.loadRating(token.token, episode)
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(userRating = rating, rateLoading = false))
      } catch (error: Throwable) {
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(rateLoading = false))
      }
    }
  }

  fun addRating(rating: Int, episode: Episode, showTraktId: IdTrakt) {
    viewModelScope.launch {
      try {
        val token = userTraktManager.checkAuthorization().token
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(rateLoading = true))
        ratingsRepository.shows.addRating(token, episode, rating)
        _messageLiveData.value = MessageEvent.info(R.string.textRateSaved)
        uiState = EpisodeDetailsUiModel(
          ratingState = RatingState(userRating = TraktRating(episode.ids.trakt, rating)),
          ratingChanged = ActionEvent(true)
        )
        Analytics.logEpisodeRated(showTraktId.id, episode, rating)
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      } finally {
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(rateLoading = false))
      }
    }
  }

  fun deleteRating(episode: Episode) {
    viewModelScope.launch {
      try {
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(rateLoading = true))
        val token = userTraktManager.checkAuthorization().token
        ratingsRepository.shows.deleteRating(token, episode)
        uiState = EpisodeDetailsUiModel(
          ratingState = RatingState(userRating = TraktRating.EMPTY, rateLoading = false),
          ratingChanged = ActionEvent(true)

        )
        _messageLiveData.value = MessageEvent.info(R.string.textShowRatingDeleted)
      } catch (error: Throwable) {
        uiState = EpisodeDetailsUiModel(ratingState = RatingState(rateLoading = false))
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }
}
