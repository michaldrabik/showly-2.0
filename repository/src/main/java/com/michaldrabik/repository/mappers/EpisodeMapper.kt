package com.michaldrabik.repository.mappers

import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.IdTvdb
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Season
import java.time.ZonedDateTime
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Episode as EpisodeDb
import com.michaldrabik.data_remote.trakt.model.Episode as EpisodeNetwork

class EpisodeMapper @Inject constructor(
  private val idsMapper: IdsMapper
) {

  fun fromNetwork(episode: EpisodeNetwork) = Episode(
    episode.season ?: -1,
    episode.number ?: -1,
    episode.title ?: "",
    idsMapper.fromNetwork(episode.ids),
    episode.overview ?: "",
    episode.rating ?: 0F,
    episode.votes ?: 0,
    episode.comment_count ?: 0,
    if (episode.first_aired.isNullOrBlank()) null else ZonedDateTime.parse(episode.first_aired),
    episode.runtime ?: -1
  )

  fun toNetwork(episode: Episode) = EpisodeNetwork(
    season = episode.season,
    number = episode.number,
    title = episode.title,
    ids = idsMapper.toNetwork(episode.ids),
    overview = episode.overview,
    rating = episode.rating,
    votes = episode.votes,
    comment_count = episode.commentCount,
    first_aired = episode.firstAired.toString(),
    runtime = episode.runtime
  )

  fun toDatabase(
    episode: Episode,
    season: Season,
    showId: IdTrakt,
    isWatched: Boolean
  ) = EpisodeDb(
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
