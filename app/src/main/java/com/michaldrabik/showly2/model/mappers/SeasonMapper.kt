package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Season
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Season as SeasonNetwork

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
}