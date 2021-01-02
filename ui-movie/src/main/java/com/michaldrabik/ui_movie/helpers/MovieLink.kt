package com.michaldrabik.ui_movie.helpers

enum class MovieLink {
  IMDB,
  TRAKT,
  TMDB,
  JUST_WATCH;

  fun getUri(id: String, country: String? = "us") = when (this) {
    IMDB -> "https://www.imdb.com/title/$id"
    TRAKT -> "https://trakt.tv/search/trakt/$id?id_type=movie"
    TMDB -> "https://www.themoviedb.org/movie/$id"
    JUST_WATCH -> "https://www.justwatch.com/$country/search?q=$id"
  }
}
