package com.michaldrabik.ui_repository.mappers

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.tvdb.model.TvdbActor
import com.michaldrabik.ui_model.Actor
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Actor as ActorDb

class ActorMapper @Inject constructor() {

  fun fromNetwork(actor: TvdbActor) = Actor(
    id = actor.id ?: -1,
    imdbId = null,
    tvdbShowId = actor.seriesId ?: -1,
    name = actor.name ?: "",
    role = actor.role ?: "",
    sortOrder = actor.sortOrder ?: -1,
    image = actor.image ?: ""
  )

  fun fromDatabase(actor: ActorDb) = Actor(
    id = actor.idTvdb,
    imdbId = actor.idImdb,
    tvdbShowId = actor.idShowTvdb,
    name = actor.name,
    role = actor.role,
    sortOrder = actor.sortOrder,
    image = actor.image
  )

  fun toDatabase(actor: Actor) = ActorDb(
    0,
    idTvdb = actor.id,
    idImdb = actor.imdbId,
    idShowTvdb = actor.tvdbShowId,
    name = actor.name,
    role = actor.role,
    sortOrder = actor.sortOrder,
    image = actor.image,
    createdAt = nowUtcMillis(),
    updatedAt = nowUtcMillis()
  )
}
