package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.CommentsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import javax.inject.Inject

@AppScope
class MovieDetailsCommentsCase @Inject constructor(
  private val commentsRepository: CommentsRepository,
  private val userManager: UserTraktManager
) {

  suspend fun loadComments(movie: Movie, limit: Int = 75): List<Comment> {
    val username = userManager.getUsername()
    val comments = commentsRepository.loadComments(movie, limit)
      .map { it.copy(isMe = it.user.username == username) }
      .partition { it.isMe }

    return comments.first + comments.second
  }

  suspend fun loadReplies(comment: Comment) =
    commentsRepository.loadReplies(comment.id)
}
