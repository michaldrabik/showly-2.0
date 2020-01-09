package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.IdImdb
import com.michaldrabik.showly2.model.IdSlug
import com.michaldrabik.showly2.model.IdTmdb
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.IdTvRage
import com.michaldrabik.showly2.model.IdTvdb
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Season
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Season as SeasonNetwork
import com.michaldrabik.storage.database.model.Season as SeasonDb

class SeasonMapper @Inject constructor(
  private val episodeMapper: EpisodeMapper
) {

  fun fromNetwork(season: SeasonNetwork) = Season(
    Ids(
      IdTrakt(season.ids.trakt),
      IdSlug(season.ids.slug),
      IdTvdb(season.ids.tvdb),
      IdImdb(season.ids.imdb),
      IdTmdb(season.ids.tmdb),
      IdTvRage(season.ids.tvrage)
    ),
    season.number,
    season.episodeCount,
    season.airedEpisodes,
    season.title,
    season.firstAired,
    season.overview,
    season.episodes.map { episodeMapper.fromNetwork(it) }
  )

  fun fromDatabase(seasonDb: SeasonDb) = Season(
    Ids.EMPTY.copy(trakt = IdTrakt(seasonDb.idTrakt)),
    seasonDb.seasonNumber,
    seasonDb.episodesCount,
    seasonDb.episodesAiredCount,
    seasonDb.seasonTitle,
    seasonDb.seasonFirstAired,
    seasonDb.seasonOverview,
    emptyList()
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
