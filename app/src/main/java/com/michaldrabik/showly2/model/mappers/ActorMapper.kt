package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.network.tvdb.model.TvdbActor
import com.michaldrabik.showly2.model.Actor
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Actor as ActorDb

class ActorMapper @Inject constructor() {

  fun fromNetwork(actor: TvdbActor) = Actor(
    actor.id,
    actor.tvdbShowId,
    actor.name,
    actor.role,
    actor.sortOrder,
    actor.image
  )

  fun fromDatabase(actor: ActorDb) = Actor(
    actor.idTvdb,
    actor.idShowTvdb,
    actor.name,
    actor.role,
    actor.sortOrder,
    actor.image
  )

  fun toDatabase(actor: Actor) = ActorDb(
    0,
    actor.id,
    actor.tvdbShowId,
    actor.name,
    actor.role,
    actor.sortOrder,
    actor.image,
    nowUtcMillis(),
    nowUtcMillis()
  )
}
