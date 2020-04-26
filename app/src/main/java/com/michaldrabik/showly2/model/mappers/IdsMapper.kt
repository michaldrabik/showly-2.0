package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.IdImdb
import com.michaldrabik.showly2.model.IdSlug
import com.michaldrabik.showly2.model.IdTmdb
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.IdTvRage
import com.michaldrabik.showly2.model.IdTvdb
import com.michaldrabik.showly2.model.Ids
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Ids as IdsNetwork
import com.michaldrabik.storage.database.model.Show as ShowDb

class IdsMapper @Inject constructor() {

  fun fromNetwork(ids: IdsNetwork?) =
    Ids(
      IdTrakt(ids?.trakt ?: -1),
      IdSlug(ids?.slug ?: ""),
      IdTvdb(ids?.tvdb ?: -1),
      IdImdb(ids?.imdb ?: ""),
      IdTmdb(ids?.tmdb ?: -1),
      IdTvRage(ids?.tvrage ?: -1)
    )

  fun toNetwork(ids: Ids?) =
    IdsNetwork(
      trakt = ids?.trakt?.id,
      slug = ids?.slug?.id,
      tvdb = ids?.tvdb?.id,
      imdb = ids?.imdb?.id,
      tmdb = ids?.tmdb?.id,
      tvrage = ids?.tvrage?.id
    )

  fun fromDatabase(show: ShowDb?) = Ids(
    IdTrakt(show?.idTrakt ?: -1),
    IdSlug(show?.idSlug ?: ""),
    IdTvdb(show?.idTvdb ?: -1),
    IdImdb(show?.idImdb ?: ""),
    IdTmdb(show?.idTmdb ?: -1),
    IdTvRage(show?.idTvrage ?: -1)
  )
}
