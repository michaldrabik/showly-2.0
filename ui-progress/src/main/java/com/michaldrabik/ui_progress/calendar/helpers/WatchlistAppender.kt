package com.michaldrabik.ui_progress.calendar.helpers

import com.michaldrabik.common.extensions.toZonedDateTime
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.ui_model.Show
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistAppender @Inject constructor() {

  fun appendWatchlistShows(
    shows: List<Show>,
    seasons: MutableList<Season>,
    episodes: MutableList<Episode>,
  ) {
    if (shows.isEmpty() || seasons.isEmpty() || episodes.isEmpty()) return

    val seasonId = seasons.maxOf { it.idTrakt }
    val episodeId = episodes.maxOf { it.idTrakt }

    shows
      .filter { it.firstAired.isNotBlank() }
      .forEachIndexed { index, show ->
        val season = Season(
          idTrakt = seasonId + index + 1,
          idShowTrakt = show.traktId,
          seasonNumber = 1,
          seasonTitle = "",
          seasonOverview = "",
          seasonFirstAired = show.firstAired.toZonedDateTime(),
          episodesCount = 1,
          episodesAiredCount = 0,
          rating = null,
          isWatched = false
        )
        val episode = Episode(
          idTrakt = episodeId + index + 1,
          idSeason = season.idTrakt,
          idShowTrakt = show.traktId,
          idShowTvdb = show.ids.tvdb.id,
          idShowImdb = show.ids.imdb.id,
          idShowTmdb = show.ids.tmdb.id,
          seasonNumber = 1,
          episodeNumber = 1,
          episodeNumberAbs = null,
          episodeOverview = "",
          title = "",
          firstAired = show.firstAired.toZonedDateTime(),
          commentsCount = 0,
          rating = 0.0f,
          runtime = 0,
          votesCount = 0,
          isWatched = false,
          lastWatchedAt = null
        )
        seasons.add(season)
        episodes.add(episode)
      }

  }
}
