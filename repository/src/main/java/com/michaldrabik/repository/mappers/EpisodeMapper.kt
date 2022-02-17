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
    season = episode.season ?: -1,
    number = episode.number ?: -1,
    title = episode.title ?: "",
    ids = idsMapper.fromNetwork(episode.ids),
    overview = episode.overview ?: "",
    rating = episode.rating ?: 0F,
    votes = episode.votes ?: 0,
    commentCount = episode.comment_count ?: 0,
    firstAired = if (episode.first_aired.isNullOrBlank()) null else ZonedDateTime.parse(episode.first_aired),
    runtime = episode.runtime ?: -1,
    numberAbs = episode.number_abs
  )

  fun toNetwork(episode: Episode) = EpisodeNetwork(
    ids = idsMapper.toNetwork(episode.ids),
    season = episode.season,
    number = episode.number,
    number_abs = episode.numberAbs,
    title = episode.title,
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
  ): EpisodeDb = EpisodeDb(
    idTrakt = episode.ids.trakt.id,
    idSeason = season.ids.trakt.id,
    idShowTrakt = showId.id,
    idShowTvdb = episode.ids.tvdb.id,
    idShowImdb = episode.ids.imdb.id,
    idShowTmdb = episode.ids.tmdb.id,
    seasonNumber = season.number,
    episodeNumber = episode.number,
    episodeNumberAbs = episode.numberAbs,
    episodeOverview = episode.overview,
    title = episode.title,
    firstAired = episode.firstAired,
    commentsCount = episode.commentCount,
    rating = episode.rating,
    runtime = episode.runtime,
    votesCount = episode.votes,
    isWatched = isWatched
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
      numberAbs = episodeDb.episodeNumberAbs,
      season = episodeDb.seasonNumber,
      overview = episodeDb.episodeOverview,
      commentCount = episodeDb.commentsCount,
      firstAired = episodeDb.firstAired,
      rating = episodeDb.rating,
      runtime = episodeDb.runtime,
      votes = episodeDb.votesCount,
    )
}
