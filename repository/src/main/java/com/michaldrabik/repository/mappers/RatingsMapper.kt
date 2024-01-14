package com.michaldrabik.repository.mappers

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.model.MovieRatings
import com.michaldrabik.data_local.database.model.ShowRatings
import com.michaldrabik.data_remote.omdb.model.OmdbResult
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ratings
import javax.inject.Inject

class RatingsMapper @Inject constructor() {

  fun fromNetwork(omdbResult: OmdbResult) =
    Ratings(
      imdb = if (omdbResult.imdbRating == "N/A") null else Ratings.Value(omdbResult.imdbRating, false),
      metascore = if (omdbResult.Metascore == "N/A") null else Ratings.Value(omdbResult.Metascore, false),
      rottenTomatoes = Ratings.Value(omdbResult.Ratings?.find { it.Source == "Rotten Tomatoes" }?.Value, false),
      rottenTomatoesUrl = if (omdbResult.tomatoURL == "N/A") null else omdbResult.tomatoURL
    )

  fun fromDatabase(entity: MovieRatings) =
    Ratings(
      trakt = Ratings.Value(entity.trakt, false),
      imdb = Ratings.Value(entity.imdb, false),
      rottenTomatoes = Ratings.Value(entity.rottenTomatoes, false),
      rottenTomatoesUrl = entity.rottenTomatoesUrl,
      metascore = Ratings.Value(entity.metascore, false)
    )

  fun fromDatabase(entity: ShowRatings) =
    Ratings(
      trakt = Ratings.Value(entity.trakt, false),
      imdb = Ratings.Value(entity.imdb, false),
      rottenTomatoes = Ratings.Value(entity.rottenTomatoes, false),
      rottenTomatoesUrl = entity.rottenTomatoesUrl,
      metascore = Ratings.Value(entity.metascore, false)
    )

  fun toMovieDatabase(
    idTrakt: IdTrakt,
    ratings: Ratings,
  ) = MovieRatings(
    id = 0,
    idTrakt = idTrakt.id,
    trakt = ratings.trakt?.value,
    imdb = ratings.imdb?.value,
    metascore = ratings.metascore?.value,
    rottenTomatoes = ratings.rottenTomatoes?.value,
    rottenTomatoesUrl = ratings.rottenTomatoesUrl,
    createdAt = nowUtcMillis(),
    updatedAt = nowUtcMillis(),
  )

  fun toShowDatabase(
    idTrakt: IdTrakt,
    ratings: Ratings,
  ) = ShowRatings(
    id = 0,
    idTrakt = idTrakt.id,
    trakt = ratings.trakt?.value,
    imdb = ratings.imdb?.value,
    metascore = ratings.metascore?.value,
    rottenTomatoes = ratings.rottenTomatoes?.value,
    rottenTomatoesUrl = ratings.rottenTomatoesUrl,
    createdAt = nowUtcMillis(),
    updatedAt = nowUtcMillis(),
  )
}
