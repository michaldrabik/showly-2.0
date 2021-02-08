package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsCommentsCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val userManager: UserTraktManager
) {

  suspend fun loadComments(show: Show, limit: Int = 75): List<Comment> {
    val username = userManager.getUsername()
    val comments = showsRepository.detailsShow.loadComments(show.ids.trakt, limit)
      .filter { it.parentId <= 0 }
      .map { it.copy(isMe = it.user.username == username) }
      .partition { it.isMe }

    return comments.first + comments.second
  }
}
