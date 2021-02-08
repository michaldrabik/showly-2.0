package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_repository.movies.MoviesRepository
import javax.inject.Inject

@AppScope
class MovieDetailsCommentsCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val userManager: UserTraktManager
) {

  suspend fun loadComments(movie: Movie, limit: Int = 75): List<Comment> {
    val username = userManager.getUsername()
    val comments = moviesRepository.movieDetails.loadComments(movie.ids.trakt, limit)
      .filter { it.parentId <= 0 }
      .map { it.copy(isMe = it.user.username == username) }
      .partition { it.isMe }

    return comments.first + comments.second
  }
}
