package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Season
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Season as SeasonNetwork
import com.michaldrabik.storage.database.model.Season as SeasonDb

class SeasonMapper @Inject constructor(
  private val episodeMapper: EpisodeMapper
) {

  fun fromNetwork(season: SeasonNetwork) = Season(
    Ids(
      season.ids.trakt,
      season.ids.slug,
      season.ids.tvdb,
      season.ids.imdb,
      season.ids.tmdb,
      season.ids.tvrage
    ),
    season.number,
    season.episodeCount,
    season.airedEpisodes,
    season.title,
    if (season.firstAired.isBlank()) null else ZonedDateTime.parse(season.firstAired),
    season.overview,
    season.episodes.map { episodeMapper.fromNetwork(it) }
  )

  fun toDatabase(
    season: Season,
    showId: Long,
    isWatched: Boolean
  ): SeasonDb {
    return SeasonDb(
      season.ids.trakt,
      showId,
      season.number,
      season.overview,
      season.episodeCount,
      season.airedEpisodes,
      isWatched
    )
  }
}