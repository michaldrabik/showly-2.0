package com.michaldrabik.ui_show.helpers

enum class ShowLink {
  IMDB,
  TRAKT,
  TVDB,
  TMDB;

  fun getUri(id: String) = when (this) {
    IMDB -> "https://www.imdb.com/title/$id"
    TRAKT -> "https://trakt.tv/search/trakt/$id?id_type=show"
    TVDB -> "https://www.thetvdb.com/?id=$id&tab=series"
    TMDB -> "https://www.themoviedb.org/tv/$id"
  }
}
