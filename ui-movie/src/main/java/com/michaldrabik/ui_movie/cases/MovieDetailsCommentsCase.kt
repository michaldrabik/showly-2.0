package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.repository.CommentsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import okhttp3.internal.EMPTY_RESPONSE
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsCommentsCase @Inject constructor(
  private val commentsRepository: CommentsRepository,
  private val userManager: UserTraktManager
) {

  suspend fun loadComments(movie: Movie): List<Comment> {
    val isSignedIn = userManager.isAuthorized()
    val username = userManager.getUsername()
    val comments = commentsRepository.loadComments(movie)
      .map {
        it.copy(
          isSignedIn = isSignedIn,
          isMe = it.user.username == username
        )
      }
      .partition { it.isMe }

    return comments.first + comments.second
  }

  suspend fun loadReplies(comment: Comment): List<Comment> {
    val isSignedIn = userManager.isAuthorized()
    val username = userManager.getUsername()
    return commentsRepository.loadReplies(comment.id)
      .map {
        it.copy(
          isSignedIn = isSignedIn,
          isMe = it.user.username == username
        )
      }
  }

  suspend fun delete(comment: Comment) {
    val dateMillis = comment.createdAt?.toMillis()
    dateMillis?.let {
      if (nowUtcMillis() - it >= TimeUnit.DAYS.toMillis(13)) {
        throw HttpException(Response.error<Any>(409, EMPTY_RESPONSE))
      }
    }
    commentsRepository.deleteComment(comment.id)
  }
}
