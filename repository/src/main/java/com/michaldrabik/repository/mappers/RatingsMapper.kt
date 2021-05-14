package com.michaldrabik.repository.mappers

import com.michaldrabik.data_remote.omdb.model.OmdbResult
import com.michaldrabik.ui_model.Ratings
import javax.inject.Inject

class RatingsMapper @Inject constructor() {

  fun fromNetwork(omdbResult: OmdbResult) =
    Ratings(
      imdb =
      if (omdbResult.imdbRating == "N/A") null
      else Ratings.Value(omdbResult.imdbRating, false),
      metascore =
      if (omdbResult.Metascore == "N/A") null
      else Ratings.Value(omdbResult.Metascore, false),
      rottenTomatoes =
      Ratings.Value(omdbResult.Ratings?.find { it.Source == "Rotten Tomatoes" }?.Value, false),
    )
}
