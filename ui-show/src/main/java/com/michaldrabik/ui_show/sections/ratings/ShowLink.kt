package com.michaldrabik.ui_show.sections.ratings

import android.net.Uri
import com.michaldrabik.ui_base.common.AppCountry

enum class ShowLink {
  IMDB,
  TRAKT,
  TVDB,
  TMDB,
  METACRITIC,
  ROTTEN,
  JUST_WATCH;

  fun getUri(
    id: String,
    country: AppCountry,
  ) = when (this) {
    IMDB -> "https://www.imdb.com/title/$id"
    TRAKT -> "https://trakt.tv/search/trakt/$id?id_type=show"
    TVDB -> "https://www.thetvdb.com/?id=$id&tab=series"
    TMDB -> "https://www.themoviedb.org/tv/$id"
    METACRITIC -> "https://www.metacritic.com/search/tv/$id/results"
    ROTTEN -> "https://www.rottentomatoes.com/search?search=$id"
    JUST_WATCH -> "https://www.justwatch.com/${country.code}/${country.justWatchQuery}?content_type=show&q=${Uri.encode(id)}"
  }
}
