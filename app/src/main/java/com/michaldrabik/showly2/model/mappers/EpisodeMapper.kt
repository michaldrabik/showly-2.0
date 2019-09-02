package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Ids
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Episode as EpisodeNetwork

class EpisodeMapper @Inject constructor() {

  fun fromNetwork(show: EpisodeNetwork) = Episode(
    show.season,
    show.number,
    show.title,
    Ids(
      show.ids.trakt,
      show.ids.slug,
      show.ids.tvdb,
      show.ids.imdb,
      show.ids.tmdb,
      show.ids.tvrage
    ),
    show.overview,
    show.rating,
    show.votes,
    show.commentCount,
    if (show.firstAired.isEmpty()) null else ZonedDateTime.parse(show.firstAired),
    if (show.updatedAt.isEmpty()) null else ZonedDateTime.parse(show.updatedAt),
    show.runtime
  )
}