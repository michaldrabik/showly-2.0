package com.michaldrabik.ui_repository.mappers

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MovieStatus
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Movie as MovieNetwork

class MovieMapper @Inject constructor(
  private val idsMapper: IdsMapper
) {

  fun fromNetwork(movie: MovieNetwork) = Movie(
    idsMapper.fromNetwork(movie.ids),
    movie.title ?: "",
    movie.year ?: -1,
    movie.overview ?: "",
    movie.released ?: "",
    movie.runtime ?: -1,
    movie.country ?: "",
    movie.trailer ?: "",
    movie.homepage ?: "",
    movie.language ?: "",
    MovieStatus.fromKey(movie.status),
    movie.rating ?: -1F,
    movie.votes ?: -1,
    movie.comment_count ?: -1,
    movie.genres ?: emptyList(),
    nowUtcMillis()
  )

  fun toNetwork(movie: Movie) = MovieNetwork(
    idsMapper.toNetwork(movie.ids),
    movie.title,
    movie.year,
    movie.overview,
    movie.released,
    movie.runtime,
    movie.country,
    movie.trailer,
    movie.homepage,
    movie.status.key,
    movie.rating,
    movie.votes,
    movie.commentCount,
    movie.genres,
    movie.language
  )

  fun fromDatabase(show: com.michaldrabik.storage.database.model.Movie) = Movie(
    idsMapper.fromDatabase(show),
    show.title,
    show.year,
    show.overview,
    show.released,
    show.runtime,
    show.country,
    show.trailer,
    show.homepage,
    show.language,
    MovieStatus.fromKey(show.status),
    show.rating,
    show.votes,
    show.commentCount,
    show.genres.split(","),
    show.updatedAt
  )

  fun toDatabase(movie: Movie) = com.michaldrabik.storage.database.model.Movie(
    movie.ids.trakt.id,
    movie.ids.tmdb.id,
    movie.ids.imdb.id,
    movie.ids.slug.id,
    movie.title,
    movie.year,
    movie.overview,
    movie.released,
    movie.runtime,
    movie.country,
    movie.trailer,
    movie.language,
    movie.homepage,
    movie.status.key,
    movie.rating,
    movie.votes,
    movie.commentCount,
    movie.genres.joinToString(","),
    nowUtcMillis()
  )
}
