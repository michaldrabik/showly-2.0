package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Season
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
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

  fun fromDatabase(seasonDb: SeasonDb) = Season(
    Ids.EMPTY.copy(trakt = seasonDb.idTrakt),
    seasonDb.seasonNumber,
    seasonDb.episodesCount,
    seasonDb.episodesAiredCount,
    seasonDb.seasonTitle,
    if (seasonDb.seasonFirstAired.isBlank()) null else ZonedDateTime.parse(seasonDb.seasonFirstAired),
    seasonDb.seasonOverview,
    emptyList()
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
      season.title,
      season.overview,
      season.firstAired?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) ?: "",
      season.episodeCount,
      season.airedEpisodes,
      isWatched
    )
  }
}