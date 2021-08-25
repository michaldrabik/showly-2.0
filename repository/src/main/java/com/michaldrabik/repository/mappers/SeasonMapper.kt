package com.michaldrabik.repository.mappers

import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Season
import java.time.ZonedDateTime
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Season as SeasonDb
import com.michaldrabik.data_remote.trakt.model.Season as SeasonNetwork

class SeasonMapper @Inject constructor(
  private val idsMapper: IdsMapper,
  private val episodeMapper: EpisodeMapper
) {

  fun fromNetwork(season: SeasonNetwork) = Season(
    idsMapper.fromNetwork(season.ids),
    season.number ?: -1,
    season.episode_count ?: -1,
    season.aired_episodes ?: -1,
    season.title ?: "",
    if (season.first_aired.isNullOrBlank()) null else ZonedDateTime.parse(season.first_aired),
    season.overview ?: "",
    season.episodes?.map { episodeMapper.fromNetwork(it) } ?: emptyList()
  )

  fun fromDatabase(seasonDb: SeasonDb, episodes: List<Episode> = emptyList()) = Season(
    Ids.EMPTY.copy(trakt = IdTrakt(seasonDb.idTrakt)),
    seasonDb.seasonNumber,
    seasonDb.episodesCount,
    seasonDb.episodesAiredCount,
    seasonDb.seasonTitle,
    seasonDb.seasonFirstAired,
    seasonDb.seasonOverview,
    episodes.map { episodeMapper.fromDatabase(it) }
  )

  fun toDatabase(
    season: Season,
    showId: IdTrakt,
    isWatched: Boolean
  ): SeasonDb {
    return SeasonDb(
      season.ids.trakt.id,
      showId.id,
      season.number,
      season.title,
      season.overview,
      season.firstAired,
      season.episodeCount,
      season.airedEpisodes,
      isWatched
    )
  }
}
