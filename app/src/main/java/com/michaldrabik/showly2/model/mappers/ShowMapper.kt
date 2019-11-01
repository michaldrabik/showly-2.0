package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.AirTime
import com.michaldrabik.showly2.model.IdImdb
import com.michaldrabik.showly2.model.IdSlug
import com.michaldrabik.showly2.model.IdTmdb
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.IdTvRage
import com.michaldrabik.showly2.model.IdTvdb
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.ShowStatus
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Show as ShowNetwork
import com.michaldrabik.storage.database.model.Show as ShowDb

class ShowMapper @Inject constructor() {

  fun fromNetwork(show: ShowNetwork) = Show(
    Ids(
      IdTrakt(show.ids.trakt),
      IdSlug(show.ids.slug),
      IdTvdb(show.ids.tvdb),
      IdImdb(show.ids.imdb),
      IdTmdb(show.ids.tmdb),
      IdTvRage(show.ids.tvrage)
    ),
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
    Ids(
      IdTrakt(show.idTrakt),
      IdSlug(show.idSlug),
      IdTvdb(show.idTvdb),
      IdImdb(show.idImdb),
      IdTmdb(show.idTmdb),
      IdTvRage(show.idTvrage)
    ),
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
    show.ids.trakt.id,
    show.ids.tvdb.id,
    show.ids.tmdb.id,
    show.ids.imdb.id,
    show.ids.slug.id,
    show.ids.tvrage.id,
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