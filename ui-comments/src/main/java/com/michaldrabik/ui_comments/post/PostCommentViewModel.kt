package com.michaldrabik.ui_comments.post

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_comments.R
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_NEW_COMMENT
import com.michaldrabik.ui_repository.CommentsRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

class PostCommentViewModel @Inject constructor(
  private val commentsRepository: CommentsRepository
) : BaseViewModel<PostCommentUiModel>() {

  fun postShowComment(showId: IdTrakt, commentText: String, isSpoiler: Boolean) {
    if (!isValid(commentText)) return
    viewModelScope.launch {
      try {
        uiState = PostCommentUiModel(isLoading = true)
        val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = showId))
        val comment = commentsRepository
          .postComment(show, commentText, isSpoiler)
          .copy(isMe = true, isSignedIn = true)
        uiState = PostCommentUiModel(successEvent = ActionEvent(Pair(ACTION_NEW_COMMENT, comment)))
      } catch (error: Throwable) {
        handleError(error)
        rethrowCancellation(error)
      }
    }
  }

  fun postMovieComment(movieId: IdTrakt, commentText: String, isSpoiler: Boolean) {
    if (!isValid(commentText)) return
    viewModelScope.launch {
      try {
        uiState = PostCommentUiModel(isLoading = true)
        val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = movieId))
        val comment = commentsRepository
          .postComment(movie, commentText, isSpoiler)
          .copy(isMe = true, isSignedIn = true)
        uiState = PostCommentUiModel(successEvent = ActionEvent(Pair(ACTION_NEW_COMMENT, comment)))
      } catch (error: Throwable) {
        handleError(error)
        rethrowCancellation(error)
      }
    }
  }

  fun postEpisodeComment(episodeId: IdTrakt, commentText: String, isSpoiler: Boolean) {
    if (!isValid(commentText)) return
    viewModelScope.launch {
      try {
        uiState = PostCommentUiModel(isLoading = true)
        val episode = Episode.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = episodeId))
        val comment = commentsRepository
          .postComment(episode, commentText, isSpoiler)
          .copy(isMe = true, isSignedIn = true)
        uiState = PostCommentUiModel(successEvent = ActionEvent(Pair(ACTION_NEW_COMMENT, comment)))
      } catch (error: Throwable) {
        handleError(error)
        rethrowCancellation(error)
      }
    }
  }

  fun postReply(commentId: IdTrakt, commentText: String, isSpoiler: Boolean) {
    if (!isValid(commentText)) return
    viewModelScope.launch {
      try {
        uiState = PostCommentUiModel(isLoading = true)
        val comment = commentsRepository
          .postReply(commentId.id, commentText, isSpoiler)
          .copy(isMe = true, isSignedIn = true)
        uiState = PostCommentUiModel(successEvent = ActionEvent(Pair(ACTION_NEW_COMMENT, comment)))
      } catch (error: Throwable) {
        handleError(error)
        rethrowCancellation(error)
      }
    }
  }

  private fun isValid(commentText: String) = commentText
    .trim().split(" ")
    .filter { !it.startsWith("@") }
    .count { it.length > 1 } >= 5

  private fun handleError(error: Throwable) {
    if (error is HttpException && error.code() == 422) {
      _messageLiveData.value = MessageEvent.error(R.string.errorCommentFormat)
    } else {
      _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
    }
    uiState = PostCommentUiModel(isLoading = false)
  }
}
