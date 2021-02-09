package com.michaldrabik.ui_repository

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class CommentsRepository @Inject constructor(
  val cloud: Cloud,
  val mappers: Mappers
) {

  suspend fun loadComments(show: Show, limit: Int = 100) =
    cloud.traktApi.fetchShowComments(show.traktId, limit)
      .map { mappers.comment.fromNetwork(it) }
      .filter { it.parentId <= 0 }

  suspend fun loadComments(movie: Movie, limit: Int = 100) =
    cloud.traktApi.fetchMovieComments(movie.traktId, limit)
      .map { mappers.comment.fromNetwork(it) }
      .filter { it.parentId <= 0 }

  suspend fun loadEpisodeComments(idTrakt: IdTrakt, season: Int, episode: Int, limit: Int = 100) =
    cloud.traktApi.fetchEpisodeComments(idTrakt.id, season, episode)
      .map { mappers.comment.fromNetwork(it) }
      .filter { it.parentId <= 0 }

  suspend fun loadReplies(commentId: Long) =
    cloud.traktApi.fetchCommentReplies(commentId)
      .map { mappers.comment.fromNetwork(it).copy(replies = 0) }
      .sortedBy { it.createdAt?.toEpochSecond() }
}
