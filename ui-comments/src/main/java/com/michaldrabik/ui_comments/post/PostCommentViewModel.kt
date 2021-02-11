package com.michaldrabik.ui_comments.post

import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_comments.R
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.CommentsRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

class PostCommentViewModel @Inject constructor(
  private val commentsRepository: CommentsRepository
) : BaseViewModel<PostCommentUiModel>() {

  fun postShowComment(showId: IdTrakt, comment: String, isSpoiler: Boolean) {
    if (comment.trim().split(" ").count { it.length > 1 } < 5) return
    viewModelScope.launch {
      try {
        uiState = PostCommentUiModel(isLoading = true)
        val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = showId))
        commentsRepository.postComment(show, comment, isSpoiler)
        uiState = PostCommentUiModel(successEvent = ActionEvent(true))
      } catch (error: Throwable) {
        handleError(error)
        rethrowCancellation(error)
      }
    }
  }

  fun postMovieComment(movieId: IdTrakt, comment: String, isSpoiler: Boolean) {
    viewModelScope.launch {
      try {
        uiState = PostCommentUiModel(isLoading = true)
        val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = movieId))
        commentsRepository.postComment(movie, comment, isSpoiler)
        uiState = PostCommentUiModel(successEvent = ActionEvent(true))
      } catch (error: Throwable) {
        handleError(error)
        rethrowCancellation(error)
      }
    }
  }

  private fun handleError(error: Throwable) {
    if (error is HttpException && error.code() == 422) {
      _messageLiveData.value = MessageEvent.error(R.string.errorCommentFormat)
    } else {
      _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
    }
    uiState = PostCommentUiModel(isLoading = false)
  }
}
