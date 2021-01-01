package com.michaldrabik.ui_show.helpers

enum class ShowLink {
  IMDB,
  TRAKT,
  TVDB,
  TMDB,
  JUST_WATCH;

  fun getUri(id: String, country: String? = "us") = when (this) {
    IMDB -> "https://www.imdb.com/title/$id"
    TRAKT -> "https://trakt.tv/search/trakt/$id?id_type=show"
    TVDB -> "https://www.thetvdb.com/?id=$id&tab=series"
    TMDB -> "https://www.themoviedb.org/tv/$id"
    JUST_WATCH -> "https://www.justwatch.com/$country/search?q=$id"
  }
}
