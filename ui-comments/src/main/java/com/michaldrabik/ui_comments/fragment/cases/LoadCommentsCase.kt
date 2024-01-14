package com.michaldrabik.ui_comments.fragment.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.CommentsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class LoadCommentsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val commentsRepository: CommentsRepository,
  private val userManager: UserTraktManager
) {

  suspend fun loadComments(id: IdTrakt, mode: Mode): List<Comment> =
    withContext(dispatchers.IO) {
      val isSignedIn = userManager.isAuthorized()
      val username = userManager.getUsername()
      val comments = commentsRepository.loadComments(id, mode)
        .map {
          it.copy(
            isSignedIn = isSignedIn,
            isMe = it.user.username == username
          )
        }
        .partition { it.isMe }

      comments.first + comments.second
    }
}
