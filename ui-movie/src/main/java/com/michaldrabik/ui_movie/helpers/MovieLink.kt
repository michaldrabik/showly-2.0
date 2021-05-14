package com.michaldrabik.ui_movie.helpers

import android.net.Uri
import com.michaldrabik.ui_base.common.AppCountry

enum class MovieLink {
  IMDB,
  TRAKT,
  TMDB,
  METACRITIC,
  ROTTEN,
  JUST_WATCH;

  fun getUri(
    id: String,
    country: AppCountry,
  ) = when (this) {
    IMDB -> "https://www.imdb.com/title/$id"
    TRAKT -> "https://trakt.tv/search/trakt/$id?id_type=movie"
    TMDB -> "https://www.themoviedb.org/movie/$id"
    METACRITIC -> "https://www.metacritic.com/search/movie/$id/results"
    ROTTEN -> "https://www.rottentomatoes.com/search?search=$id"
    JUST_WATCH -> "https://www.justwatch.com/${country.code}/${country.justWatchQuery}?content_type=movie&q=${Uri.encode(id)}"
  }
}
