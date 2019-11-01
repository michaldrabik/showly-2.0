package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdImdb
import com.michaldrabik.showly2.model.IdSlug
import com.michaldrabik.showly2.model.IdTmdb
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.IdTvRage
import com.michaldrabik.showly2.model.IdTvdb
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
      IdTrakt(episode.ids.trakt),
      IdSlug(episode.ids.slug),
      IdTvdb(episode.ids.tvdb),
      IdImdb(episode.ids.imdb),
      IdTmdb(episode.ids.tmdb),
      IdTvRage(episode.ids.tvrage)
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
    showId: IdTrakt,
    isWatched: Boolean
  ): EpisodeDb {
    return EpisodeDb(
      episode.ids.trakt.id,
      season.ids.trakt.id,
      showId.id,
      episode.ids.tvdb.id,
      episode.ids.imdb.id,
      episode.ids.tmdb.id,
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
        trakt = IdTrakt(episodeDb.idTrakt),
        tvdb = IdTvdb(episodeDb.idShowTvdb),
        imdb = IdImdb(episodeDb.idShowImdb),
        tmdb = IdTmdb(episodeDb.idShowTmdb)
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