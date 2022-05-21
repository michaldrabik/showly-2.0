package com.michaldrabik.ui_comments.fragment.cases

import com.michaldrabik.repository.CommentsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_model.Comment
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class LoadRepliesCase @Inject constructor(
  private val commentsRepository: CommentsRepository,
  private val userManager: UserTraktManager
) {

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
}
