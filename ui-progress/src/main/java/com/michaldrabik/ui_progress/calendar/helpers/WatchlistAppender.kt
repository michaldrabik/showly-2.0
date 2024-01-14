package com.michaldrabik.ui_progress.calendar.helpers

import com.michaldrabik.common.extensions.toZonedDateTime
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.ui_model.Show
import javax.inject.Inject

class WatchlistAppender @Inject constructor() {

  fun appendWatchlistShows(
    shows: List<Show>,
    seasons: MutableList<Season>,
    episodes: MutableList<Episode>,
  ) {
    if (shows.isEmpty() || seasons.isEmpty() || episodes.isEmpty()) {
      return
    }

    val seasonId = seasons.maxOf { it.idTrakt }
    val episodeId = episodes.maxOf { it.idTrakt }

    shows
      .filter { it.firstAired.isNotBlank() }
      .forEachIndexed { index, show ->
        val season = createWatchlistSeason(
          show = show,
          seasonId = seasonId + index + 1
        )

        val episode = createWatchlistEpisode(
          show = show,
          season = season,
          episodeId = episodeId + index + 1
        )

        seasons.add(season)
        episodes.add(episode)
      }
  }

  private fun createWatchlistSeason(
    show: Show,
    seasonId: Long,
  ) = Season(
    idTrakt = seasonId,
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

  private fun createWatchlistEpisode(
    show: Show,
    season: Season,
    episodeId: Long,
  ) = Episode(
    idTrakt = episodeId,
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
}
