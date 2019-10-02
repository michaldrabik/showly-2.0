package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.AirTime
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.ShowStatus
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Show as ShowNetwork
import com.michaldrabik.storage.database.model.Show as ShowDb

class ShowMapper @Inject constructor() {

  fun fromNetwork(show: ShowNetwork) = Show(
    Ids(show.ids.trakt, show.ids.slug, show.ids.tvdb, show.ids.imdb, show.ids.tmdb, show.ids.tvrage),
    show.title,
    show.year,
    show.overview,
    show.firstAired,
    show.runtime,
    AirTime(show.airTime.day, show.airTime.time, show.airTime.timezone),
    show.certification,
    show.network,
    show.country,
    show.trailer,
    show.homepage,
    ShowStatus.fromKey(show.status),
    show.rating,
    show.votes,
    show.commentCount,
    show.genres,
    show.airedEpisodes
  )

  fun fromDatabase(show: ShowDb) = Show(
    Ids(show.idTrakt, show.idSlug, show.idTvdb, show.idImdb, show.idTmdb, show.idTvrage),
    show.title,
    show.year,
    show.overview,
    show.firstAired,
    show.runtime,
    AirTime(show.airtimeDay, show.airtimeTime, show.airtimeTimezone),
    show.certification,
    show.network,
    show.country,
    show.trailer,
    show.homepage,
    ShowStatus.fromKey(show.status),
    show.rating,
    show.votes,
    show.commentCount,
    show.genres.split(","),
    show.airedEpisodes
  )

  fun toDatabase(show: Show) = ShowDb(
    show.id,
    show.ids.tvdb,
    show.ids.tmdb,
    show.ids.imdb,
    show.ids.slug,
    show.ids.tvrage,
    show.title,
    show.year,
    show.overview,
    show.firstAired,
    show.runtime,
    show.airTime.day,
    show.airTime.time,
    show.airTime.timezone,
    show.certification,
    show.network,
    show.country,
    show.trailer,
    show.homepage,
    show.status.key,
    show.rating,
    show.votes,
    show.commentCount,
    show.genres.joinToString(","),
    show.airedEpisodes,
    nowUtcMillis()
  )
}