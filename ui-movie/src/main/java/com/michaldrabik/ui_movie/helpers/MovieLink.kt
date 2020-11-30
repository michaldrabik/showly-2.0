package com.michaldrabik.ui_movie.helpers

enum class MovieLink {
  IMDB,
  TRAKT,
  TMDB;

  fun getUri(id: String) = when (this) {
    IMDB -> "https://www.imdb.com/title/$id"
    TRAKT -> "https://trakt.tv/search/trakt/$id?id_type=movie"
    TMDB -> "https://www.themoviedb.org/movie/$id"
  }
}
