package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Season
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
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
    if (episode.firstAired.isEmpty()) null else ZonedDateTime.parse(episode.firstAired),
    if (episode.updatedAt.isEmpty()) null else ZonedDateTime.parse(episode.updatedAt),
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
      season.number,
      episode.number,
      episode.overview,
      episode.title,
      episode.firstAired?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) ?: "",
      isWatched
    )
  }
}