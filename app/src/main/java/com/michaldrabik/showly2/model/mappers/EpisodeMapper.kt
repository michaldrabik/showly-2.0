package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Season
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Episode as EpisodeNetwork
import com.michaldrabik.storage.database.model.Episode as EpisodeDb

class EpisodeMapper @Inject constructor() {

  fun fromNetwork(episode: EpisodeNetwork) = Episode(
    episode.season,
    episode.number,
    episode.title,
    Ids(
      episode.ids.trakt,
      episode.ids.slug,
      episode.ids.tvdb,
      episode.ids.imdb,
      episode.ids.tmdb,
      episode.ids.tvrage
    ),
    episode.overview,
    episode.rating,
    episode.votes,
    episode.commentCount,
    episode.firstAired,
    episode.runtime
  )

  fun toDatabase(
    episode: Episode,
    season: Season,
    showId: Long,
    isWatched: Boolean
  ): EpisodeDb {
    return EpisodeDb(
      episode.ids.trakt,
      season.ids.trakt,
      showId,
      episode.ids.tvdb,
      episode.ids.imdb,
      episode.ids.tmdb,
      season.number,
      episode.number,
      episode.overview,
      episode.title,
      episode.firstAired,
      episode.commentCount,
      episode.rating,
      episode.runtime,
      episode.votes,
      isWatched
    )
  }

  fun fromDatabase(episodeDb: EpisodeDb) =
    Episode(
      ids = Ids.EMPTY.copy(
        trakt = episodeDb.idTrakt,
        tvdb = episodeDb.idShowTvdb,
        imdb = episodeDb.idShowImdb,
        tmdb = episodeDb.idShowTmdb
      ),
      title = episodeDb.title,
      number = episodeDb.episodeNumber,
      season = episodeDb.seasonNumber,
      overview = episodeDb.episodeOverview,
      commentCount = episodeDb.commentsCount,
      firstAired = episodeDb.firstAired,
      rating = episodeDb.rating,
      runtime = episodeDb.runtime,
      votes = episodeDb.votesCount
    )
}